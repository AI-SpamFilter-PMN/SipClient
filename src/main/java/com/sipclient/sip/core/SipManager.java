package com.sipclient.sip.core;

import javax.sip.SipProvider;

public class SipManager {

    private final SipInitializer initializer;

    public SipManager() {
        initializer = new SipInitializer();
    }

    public void initialize() {

        try {

            SipProvider provider = initializer.initialize();

            System.out.println("Provider: " + provider);

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}