package com.sipclient.sip.core;

import com.sipclient.sip.service.RegisterService;

public class SipManager {

    private final SipInitializer initializer;

    public SipManager() {
        initializer = new SipInitializer();
    }

    public void initialize() {

        try {

            initializer.initialize();

            RegisterService registerService =
                    new RegisterService(
                            initializer.getSipProvider(),
                            initializer.getAddressFactory(),
                            initializer.getHeaderFactory(),
                            initializer.getMessageFactory());

            registerService.register();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}