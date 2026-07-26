package com.sipclient.sip.core;

import com.sipclient.sip.listener.SipListenerImpl;
import com.sipclient.sip.model.SipAccount;
import com.sipclient.sip.service.InviteService;
import com.sipclient.sip.service.RegisterService;

public class SipManager {

    private final SipInitializer initializer;

    private InviteService inviteService;

    public SipManager() {

        initializer = new SipInitializer();

    }

    public InviteService getInviteService() {

        return inviteService;

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

            inviteService =
                    new InviteService(
                            initializer.getSipProvider(),
                            initializer.getAddressFactory(),
                            initializer.getHeaderFactory(),
                            initializer.getMessageFactory(),
                            initializer.getInviteRequestFactory());

            SipListenerImpl listener =
        new SipListenerImpl(
                registerService,
                inviteService);

            initializer.registerListener(listener);

            registerService.register(account);

            Thread.sleep(2000);

            call(
                    account,
                    "1002");

            System.out.println("SIP Initialized");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public void call(
            SipAccount account,
            String destination) {

        if (inviteService == null) {

            System.out.println("InviteService is not initialized.");
            return;

        }

        inviteService.call(
                account,
                destination);

    }

}