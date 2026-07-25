package com.sipclient.sip.core;

import com.sipclient.sip.listener.SipListenerImpl;
import com.sipclient.sip.model.SipAccount;
import com.sipclient.sip.service.RegisterService;

public class SipManager {

    private final SipInitializer initializer;

    public SipManager() {

        initializer = new SipInitializer();

    }

    public void initialize(SipAccount account) {

        try {

            initializer.initialize();

            RegisterService registerService =
                    new RegisterService(
                            initializer.getSipProvider(),
                            initializer.getAddressFactory(),
                            initializer.getHeaderFactory(),
                            initializer.getMessageFactory());

            SipListenerImpl listener =
                    new SipListenerImpl(registerService);

            initializer.registerListener(listener);

            registerService.register(account);

            System.out.println("SIP Initialized");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}