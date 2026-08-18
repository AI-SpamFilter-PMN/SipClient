package com.sipclient.sip.handler;

import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import com.sipclient.sip.dialog.CallState;
import com.sipclient.sip.dialog.DialogManager;
import com.sipclient.sip.media.RtpMediaEngine;

public class ByeRequestHandler {

    private final SipProvider sipProvider;
    private final MessageFactory messageFactory;
    private final DialogManager dialogManager;

    public ByeRequestHandler(
            SipProvider sipProvider,
            MessageFactory messageFactory,
            DialogManager dialogManager) {

        this.sipProvider = sipProvider;
        this.messageFactory = messageFactory;
        this.dialogManager = dialogManager;
    }

    public void handle(RequestEvent event) {

        try {

            Request request = event.getRequest();

            System.out.println("================================");
            System.out.println("Incoming BYE");
            System.out.println("Call-ID: " + request.getHeader("Call-ID"));
            System.out.println("CSeq   : " + request.getHeader("CSeq"));

            ServerTransaction serverTransaction = event.getServerTransaction();

            if (serverTransaction == null) {
                serverTransaction = sipProvider.getNewServerTransaction(request);
            }

            Response response = messageFactory.createResponse(Response.OK, request);

            serverTransaction.sendResponse(response);

            System.out.println("200 OK Sent - BYE");

            RtpMediaEngine.getInstance().stopAudio();

            dialogManager.setState(CallState.DISCONNECTED);
            dialogManager.reset();

            System.out.println("Call State -> DISCONNECTED");
            System.out.println("Call Session Reset");
            System.out.println("================================");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}