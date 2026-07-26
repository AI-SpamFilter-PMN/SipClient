package com.sipclient.sip.builder;

import com.sipclient.sip.config.SipConfig;
import com.sipclient.sip.model.SipAccount;

public class SdpBuilder {

    public String build(SipAccount account) {

        StringBuilder sdp = new StringBuilder();

        sdp.append("v=0\r\n");
        sdp.append("o=")
                .append(account.getUsername())
                .append(" 12345 12345 IN IP4 ")
                .append(SipConfig.LOCAL_IP)
                .append("\r\n");

        sdp.append("s=SipClient\r\n");

        sdp.append("c=IN IP4 ")
                .append(SipConfig.LOCAL_IP)
                .append("\r\n");

        sdp.append("t=0 0\r\n");

        sdp.append("m=audio 4000 RTP/AVP 0\r\n");

        sdp.append("a=rtpmap:0 PCMU/8000\r\n");

        return sdp.toString();

    }

}