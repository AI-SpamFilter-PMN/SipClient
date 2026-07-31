package com.sipclient.sip.media;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SdpParser {

    private String remoteIp;
    private int remotePort = -1;

    public static SdpParser parse(String sdpBody) {
        SdpParser parser = new SdpParser();
        if (sdpBody == null || sdpBody.isEmpty()) {
            return parser;
        }

        Pattern ipPattern = Pattern.compile("c=IN IP4 (\\S+)");
        Matcher ipMatcher = ipPattern.matcher(sdpBody);
        if (ipMatcher.find()) {
            parser.remoteIp = ipMatcher.group(1).trim();
        }

        Pattern mediaPattern = Pattern.compile("m=audio (\\d+)");
        Matcher mediaMatcher = mediaPattern.matcher(sdpBody);
        if (mediaMatcher.find()) {
            parser.remotePort = Integer.parseInt(mediaMatcher.group(1));
        }

        return parser;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public boolean isValid() {
        return remoteIp != null && !remoteIp.isEmpty() && remotePort > 0;
    }

    @Override
    public String toString() {
        return "SdpParser{remoteIp='" + remoteIp + "', remotePort=" + remotePort + '}';
    }
}