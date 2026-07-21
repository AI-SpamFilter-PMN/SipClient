package com.sipclient.service;

import com.sipclient.sip.core.SipManager;
import com.sipclient.sip.model.SipAccount;

public class CallService {

    private final SipManager sipManager;

    public CallService() {
        sipManager = new SipManager();
    }

    public void register(SipAccount account) {
        sipManager.initialize(account);
    }

    public SipManager getSipManager() {
        return sipManager;
    }

}