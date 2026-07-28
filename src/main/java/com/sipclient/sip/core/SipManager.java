package com.sipclient.sip.core;

import com.sipclient.sip.dialog.CallState;
import com.sipclient.sip.dialog.DialogManager;
import com.sipclient.sip.listener.SipListenerImpl;
import com.sipclient.sip.model.SipAccount;
import com.sipclient.sip.service.InviteService;
import com.sipclient.sip.service.RegisterService;

public class SipManager {

    private final SipInitializer initializer;

    private InviteService inviteService;

    private final DialogManager dialogManager;

    public SipManager() {

        initializer = new SipInitializer();
        dialogManager = new DialogManager();

    }

    public InviteService getInviteService() {

        return inviteService;

    }

    public DialogManager getDialogManager() {

        return dialogManager;

    }

    public CallState getCallState() {

        return dialogManager.getState();

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
                            initializer.getInviteRequestFactory(),
                            dialogManager);

            SipListenerImpl listener =
                    new SipListenerImpl(
                            registerService,
                            inviteService,
                            dialogManager);

            initializer.registerListener(listener);

            registerService.register(account);

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