package com.sipclient.sip.core;

import com.sipclient.sip.dialog.CallState;
import com.sipclient.sip.dialog.DialogManager;
import com.sipclient.sip.listener.SipListenerImpl;
import com.sipclient.sip.model.SipAccount;
import com.sipclient.sip.service.InviteService;
import com.sipclient.sip.service.RegisterService;
import com.sipclient.sip.service.IncomingCallService;

import com.sipclient.sip.handler.IncomingInviteHandler;
import com.sipclient.sip.handler.ByeRequestHandler;
import com.sipclient.sip.handler.CancelRequestHandler;
import com.sipclient.sip.handler.OptionsRequestHandler;
import com.sipclient.sip.handler.AckRequestHandler;
import com.sipclient.sip.handler.RequestDispatcher;

public class SipManager {

    private final SipInitializer initializer;

    private InviteService inviteService;

    private final DialogManager dialogManager;
    private IncomingCallService incomingCallService;

    public SipManager() {

        initializer = new SipInitializer();
        dialogManager = new DialogManager();

    }

    public InviteService getInviteService() {

        return inviteService;

    }
    public IncomingCallService getIncomingCallService() {
    return incomingCallService;
}

    public DialogManager getDialogManager() {

        return dialogManager;

    }

    public CallState getCallState() {

        return dialogManager.getState();

    }

    public void initialize(SipAccount account) {

        try {

            // ==========================================
            // SIP INITIALIZATION
            // ==========================================

            initializer.initialize();

            // ==========================================
            // REGISTER SERVICE
            // ==========================================

            RegisterService registerService =
                    new RegisterService(
                            initializer.getSipProvider(),
                            initializer.getAddressFactory(),
                            initializer.getHeaderFactory(),
                            initializer.getMessageFactory());

            // ==========================================
            // OUTGOING INVITE SERVICE
            // ==========================================

            inviteService =
                    new InviteService(
                            initializer.getSipProvider(),
                            initializer.getAddressFactory(),
                            initializer.getHeaderFactory(),
                            initializer.getMessageFactory(),
                            initializer.getInviteRequestFactory(),
                            dialogManager);

            // ==========================================
            // INCOMING CALL SERVICE
            // ==========================================

            incomingCallService =
        new IncomingCallService(
                initializer.getSipProvider(),
                initializer.getMessageFactory(),
                initializer.getAddressFactory(),
                initializer.getHeaderFactory(),
                dialogManager);

            // ==========================================
            // REQUEST HANDLERS
            // ==========================================

            IncomingInviteHandler incomingInviteHandler =
                    new IncomingInviteHandler(
                            incomingCallService);

         ByeRequestHandler byeHandler =
        new ByeRequestHandler(
                initializer.getSipProvider(),
                initializer.getMessageFactory(),
                dialogManager);
CancelRequestHandler cancelHandler =
        new CancelRequestHandler();

            OptionsRequestHandler optionsHandler =
                    new OptionsRequestHandler();

            AckRequestHandler ackHandler =
                    new AckRequestHandler(
                            dialogManager);

            // ==========================================
            // REQUEST DISPATCHER
            // ==========================================

            RequestDispatcher requestDispatcher =
                    new RequestDispatcher(
                            incomingInviteHandler,
                            byeHandler,
                            cancelHandler,
                            optionsHandler,
                            ackHandler);

            // ==========================================
            // SIP LISTENER
            // ==========================================

            SipListenerImpl listener =
                    new SipListenerImpl(
                            registerService,
                            inviteService,
                            dialogManager,
                            requestDispatcher);

            initializer.registerListener(listener);

            // ==========================================
            // REGISTER
            // ==========================================

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

            System.out.println(
                    "InviteService is not initialized.");

            return;
        }

        inviteService.call(
                account,
                destination);

    }

}