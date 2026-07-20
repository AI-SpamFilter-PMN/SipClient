package com.sipclient.service;

import com.sipclient.sip.config.SipConfig;
import com.sipclient.sip.core.SipManager;

public class CallService {

    private final SipManager sipManager;

    public CallService() {
        sipManager = new SipManager();
    }

    public void register(SipConfig config) {
        sipManager.initialize();
    }

    public SipManager getSipManager() {
        return sipManager;
    }
}