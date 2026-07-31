package com.sipclient.sip.media;

import javax.sound.sampled.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class RtpMediaEngine {

    private DatagramSocket socket;
    private TargetDataLine microphone;
    private SourceDataLine speaker;

    private Thread senderThread;
    private Thread receiverThread;
    private volatile boolean isRunning = false;

    private static final byte[] LINEAR_TO_ULAW = new byte[65536];
    private static final short[] ULAW_TO_LINEAR = new short[256];

    static {
        for (int i = 0; i < 256; i++) {
            ULAW_TO_LINEAR[i] = decodeUlaw((byte) i);
        }
        for (int i = -32768; i <= 32767; i++) {
            LINEAR_TO_ULAW[i & 0xFFFF] = encodeUlaw((short) i);
        }
    }

    public synchronized void startStream(String remoteIp, int remotePort, int localPort) {
        if (isRunning) {
            stopStream();
        }

        try {
            System.out.println("Starting RTP Stream to " + remoteIp + ":" + remotePort + " via local port " + localPort);

            socket = new DatagramSocket(localPort);
            InetAddress remoteAddress = InetAddress.getByName(remoteIp);

            AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, false);

            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(micInfo);
            microphone.open(format, 3200); 
            microphone.start();

            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);
            speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
            speaker.open(format, 3200); 
            speaker.start();

            isRunning = true;

            senderThread = new Thread(() -> runSender(remoteAddress, remotePort));
            senderThread.setName("RTP-Sender-Thread");
            senderThread.start();

            receiverThread = new Thread(this::runReceiver);
            receiverThread.setName("RTP-Receiver-Thread");
            receiverThread.start();

            System.out.println("RTP Media Engine Started Successfully (G.711u Codec Enabled)");

        } catch (Exception e) {
            System.err.println("Failed to start RTP Media Engine:");
            e.printStackTrace();
            stopStream();
        }
    }

    private void runSender(InetAddress remoteAddress, int remotePort) {
        byte[] pcmBuffer = new byte[320];
        byte[] ulawPayload = new byte[160];
        int seqNum = 0;
        long timeStamp = 0;

        while (isRunning && !Thread.currentThread().isInterrupted()) {
            int read = microphone.read(pcmBuffer, 0, pcmBuffer.length);
            if (read > 0) {
                try {
                    int pcmSamples = read / 2;
                    for (int i = 0; i < pcmSamples; i++) {
                        short sample = (short) ((pcmBuffer[i * 2 + 1] << 8) | (pcmBuffer[i * 2] & 0xFF));
                        ulawPayload[i] = LINEAR_TO_ULAW[sample & 0xFFFF];
                    }

                    byte[] rtpPacket = new byte[12 + pcmSamples];
                    rtpPacket[0] = (byte) 0x80; 
                    rtpPacket[1] = (byte) 0x00; 

                    rtpPacket[2] = (byte) ((seqNum >> 8) & 0xFF);
                    rtpPacket[3] = (byte) (seqNum & 0xFF);
                    seqNum++;

                    rtpPacket[4] = (byte) ((timeStamp >> 24) & 0xFF);
                    rtpPacket[5] = (byte) ((timeStamp >> 16) & 0xFF);
                    rtpPacket[6] = (byte) ((timeStamp >> 8) & 0xFF);
                    rtpPacket[7] = (byte) (timeStamp & 0xFF);
                    timeStamp += pcmSamples;

                    System.arraycopy(ulawPayload, 0, rtpPacket, 12, pcmSamples);

                    DatagramPacket packet = new DatagramPacket(rtpPacket, rtpPacket.length, remoteAddress, remotePort);
                    socket.send(packet);

                } catch (Exception e) {
                    if (isRunning) System.err.println("RTP Send Error: " + e.getMessage());
                }
            }
        }
    }

    private void runReceiver() {
        byte[] socketBuffer = new byte[1024];

        while (isRunning && !Thread.currentThread().isInterrupted()) {
            try {
                DatagramPacket packet = new DatagramPacket(socketBuffer, socketBuffer.length);
                socket.receive(packet);

                int length = packet.getLength();
                if (length > 12) { 
                    int payloadLength = length - 12;
                    byte[] pcmOut = new byte[payloadLength * 2];

                    // فك تشفير u-law إلى PCM 16-bit
                    for (int i = 0; i < payloadLength; i++) {
                        byte ulawSample = socketBuffer[12 + i];
                        short pcmSample = ULAW_TO_LINEAR[ulawSample & 0xFF];

                        pcmOut[i * 2] = (byte) (pcmSample & 0xFF);
                        pcmOut[i * 2 + 1] = (byte) ((pcmSample >> 8) & 0xFF);
                    }

                    // تشغيل الصوت النقي على السماعة
                    speaker.write(pcmOut, 0, pcmOut.length);
                }
            } catch (Exception e) {
                if (isRunning) System.err.println("RTP Receive Error: " + e.getMessage());
            }
        }
    }

    public synchronized void stopStream() {
        if (!isRunning && socket == null) {
            return;
        }

        isRunning = false;

        if (socket != null && !socket.isClosed()) {
            socket.close();
            socket = null;
        }

        if (microphone != null) {
            microphone.stop();
            microphone.close();
            microphone = null;
        }

        if (speaker != null) {
            speaker.stop();
            speaker.close();
            speaker = null;
        }

        if (senderThread != null) senderThread.interrupt();
        if (receiverThread != null) receiverThread.interrupt();

        System.out.println("RTP Media Engine Stopped Cleanly");
    }


    private static short decodeUlaw(byte ulaw) {
        ulaw = (byte) ~ulaw;
        int sign = (ulaw & 0x80);
        int exponent = (ulaw >> 4) & 0x07;
        int mantissa = ulaw & 0x0F;
        int sample = (mantissa << (exponent + 3)) + (0x84 << exponent) - 0x84;
        return (short) (sign != 0 ? -sample : sample);
    }

    private static byte encodeUlaw(short pcm) {
        int sign = (pcm >> 8) & 0x80;
        if (sign != 0) pcm = (short) -pcm;

        if (pcm > 32635) pcm = 32635;
        pcm += 0x84;

        int exponent = 7;
        for (int expMask = 0x4000; (pcm & expMask) == 0 && exponent > 0; exponent--, expMask >>= 1) ;

        int mantissa = (pcm >> (exponent + 3)) & 0x0F;
        byte ulaw = (byte) (sign | (exponent << 4) | mantissa);
        return (byte) ~ulaw;
    }
}