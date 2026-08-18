package com.sipclient.sip.handler;

import javax.sip.Dialog;
import javax.sip.RequestEvent;
import javax.sip.message.Request;

import com.sipclient.sip.dialog.CallState;
import com.sipclient.sip.dialog.DialogManager;
import com.sipclient.sip.model.IncomingCallSession;

public class AckRequestHandler {

    private final DialogManager dialogManager;

    public AckRequestHandler(DialogManager dialogManager) {
        this.dialogManager = dialogManager;
    }

    public void handle(RequestEvent event) {

        Request request = event.getRequest();

        System.out.println("================================");
        System.out.println("Incoming ACK");
        System.out.println("Call-ID: " + request.getHeader("Call-ID"));
        System.out.println("CSeq   : " + request.getHeader("CSeq"));

        IncomingCallSession incomingSession = dialogManager.getIncomingCallSession();
        var currentSession = dialogManager.getCurrentSession();
        Dialog eventDialog = event.getDialog();

        if (incomingSession != null) {

            if (eventDialog != null) {
                incomingSession.setDialog(eventDialog);
            }

            dialogManager.setState(CallState.IN_CALL);
            System.out.println("Incoming Call ACK Processed -> IN_CALL");

        } 
        else if (currentSession != null) {

            if (eventDialog != null) {
                currentSession.setDialog(eventDialog);
            }

            dialogManager.setState(CallState.IN_CALL);
            System.out.println("Re-INVITE ACK Processed for Outgoing Call -> IN_CALL");

        } 

        else if (eventDialog != null) {

            dialogManager.setState(CallState.IN_CALL);
            System.out.println("ACK Processed for Active Dialog -> IN_CALL");

        } 
        else {

            System.out.println("Warning: No active session or dialog found for this ACK");

        }

        System.out.println("================================");
    }
}