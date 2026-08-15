package com.sipclient.service;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Post-call processing: uploads the recording to MVNO ASR, writes the call
 * record (with transcript + recording_url) to Neon, and sends the transcript
 * to Filteration-System for spam classification.
 *
 * All three steps are best-effort: a failure in any one does not block the
 * others. The call has already ended; this is post-call analytics only.
 */
public class PostCallService {

    private final HttpClient httpClient;
    private final String mvnoAsrUrl;
    private final String filterationUrl;
    private final String dbUrl;

    public PostCallService(String mvnoAsrUrl, String filterationUrl, String dbUrl) {
        this.mvnoAsrUrl = mvnoAsrUrl;
        this.filterationUrl = filterationUrl;
        this.dbUrl = dbUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Processes a completed call: upload recording → get transcript → write to
     * DB → classify. Returns the transcript (or empty string on failure).
     */
    public String processCall(String callerId, String calleeId, byte[] recordingWav) {
        String transcript = "";

        // Step 1: Upload recording to MVNO ASR for transcription
        if (recordingWav != null && recordingWav.length > 0) {
            transcript = uploadForAsr(recordingWav);
            System.out.println("[PostCall] ASR transcript: " +
                    (transcript.length() > 80 ? transcript.substring(0, 80) + "..." : transcript));
        }

        // Step 2: Write call record to Neon (calls table)
        String recordingUrl = writeCallToDb(callerId, calleeId, transcript);

        // Step 3: Send transcript to Filteration-System for classification
        if (!transcript.isBlank()) {
            classifyTranscript(callerId, calleeId, transcript, recordingUrl);
        }

        return transcript;
    }

    /**
     * Uploads a WAV recording to the MVNO ASR endpoint and returns the transcript.
     */
    private String uploadForAsr(byte[] wavBytes) {
        try {
            String boundary = "----SipClientBoundary" + System.currentTimeMillis();
            String body = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"recording.wav\"\r\n" +
                    "Content-Type: audio/wav\r\n\r\n";
            String footer = "\r\n--" + boundary + "--\r\n";

            ByteArrayOutputStream reqBody = new ByteArrayOutputStream();
            reqBody.write(body.getBytes("ASCII"));
            reqBody.write(wavBytes);
            reqBody.write(footer.getBytes("ASCII"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mvnoAsrUrl))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(reqBody.toByteArray()))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                // Parse JSON response: {"filename":"...","transcript":"..."}
                String body2 = response.body();
                int idx = body2.indexOf("\"transcript\"");
                if (idx >= 0) {
                    int start = body2.indexOf("\"", idx + 12) + 1;
                    int end = body2.indexOf("\"", start);
                    if (start > 0 && end > start) {
                        return body2.substring(start, end);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[PostCall] ASR upload failed: " + e.getMessage());
        }
        return "";
    }

    /**
     * Writes a call record to the Neon calls table with transcript + recording_url.
     * Returns the recording URL (or null if DB write failed).
     */
    private String writeCallToDb(String callerId, String calleeId, String transcript) {
        if (dbUrl == null || dbUrl.isBlank()) {
            return null;
        }
        String recordingUrl = "recording-" + System.currentTimeMillis() + ".wav";
        String sql = "INSERT INTO calls (id, source, destination, started_at, ended_at, " +
                "status, transcript, recording_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, UUID.randomUUID());
            ps.setString(2, callerId != null ? callerId : "unknown");
            ps.setString(3, calleeId != null ? calleeId : "unknown");
            ps.setObject(4, OffsetDateTime.now().minusMinutes(5));
            ps.setObject(5, OffsetDateTime.now());
            ps.setString(6, "COMPLETED");
            ps.setString(7, transcript.isBlank() ? null : transcript);
            ps.setString(8, recordingUrl);
            ps.executeUpdate();
            System.out.println("[PostCall] Call record written to Neon: " + recordingUrl);
            return recordingUrl;
        } catch (Exception e) {
            System.err.println("[PostCall] DB write failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Sends the transcript to Filteration-System for spam classification.
     */
    private void classifyTranscript(String callerId, String calleeId, String transcript, String recordingUrl) {
        try {
            String json = String.format(
                    "{\"callerId\":\"%s\",\"receiverId\":\"%s\",\"transcript\":%s,\"recordingUrl\":\"%s\"}",
                    callerId != null ? callerId : "",
                    calleeId != null ? calleeId : "",
                    quoteJsonString(transcript),
                    recordingUrl != null ? recordingUrl : ""
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(filterationUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[PostCall] Filteration-System classification: " + response.body());
        } catch (Exception e) {
            System.err.println("[PostCall] Classification failed (fail-open): " + e.getMessage());
        }
    }

    private static String quoteJsonString(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
}
