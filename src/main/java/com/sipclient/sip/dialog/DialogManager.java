package com.sipclient.sip.dialog;

import com.sipclient.sip.media.RtpMediaEngine;
import com.sipclient.sip.media.SdpParser;
import com.sipclient.sip.model.IncomingCallSession;

public class DialogManager {

    private final CallSession currentSession;
    private IncomingCallSession incomingCallSession;

    private final RtpMediaEngine rtpMediaEngine = new RtpMediaEngine();
    private final int localRtpPort = 4000; 

    public DialogManager() {
        currentSession = new CallSession();
    }

    public CallSession getCurrentSession() {
        return currentSession;
    }

    public void handleIncomingSdp(String sdpBody) {
        SdpParser sdp = SdpParser.parse(sdpBody);

        if (sdp.isValid()) {
            System.out.println("Connecting Audio Stream to " + sdp.getRemoteIp() + ":" + sdp.getRemotePort());
            rtpMediaEngine.startStream(sdp.getRemoteIp(), sdp.getRemotePort(), localRtpPort);
        } else {
            System.err.println("Failed to parse SDP or SDP is invalid!");
        }
    }

    public void setState(CallState state) {
        currentSession.setState(state);
        System.out.println("Call State -> " + state);

        if (state == CallState.DISCONNECTED || state == CallState.IDLE) {
            rtpMediaEngine.stopStream();
        }
    }

    public CallState getState() {
        return currentSession.getState();
    }

    public CallState getCallState() {
        return getState();
    }

    public void reset() {
        rtpMediaEngine.stopStream(); 
        currentSession.clear();
    }

    public IncomingCallSession getIncomingCallSession() {
        return incomingCallSession;
    }

    public void setIncomingCallSession(IncomingCallSession incomingCallSession) {
        this.incomingCallSession = incomingCallSession;
    }

    public RtpMediaEngine getRtpMediaEngine() {
        return rtpMediaEngine;
    }
}