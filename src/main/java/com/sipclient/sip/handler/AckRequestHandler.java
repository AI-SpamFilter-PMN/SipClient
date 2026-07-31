package com.sipclient.sip.handler;

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

        IncomingCallSession session =
                dialogManager.getIncomingCallSession();

        if (session != null) {

            if (event.getDialog() != null) {
                session.setDialog(event.getDialog());
            }

            dialogManager.setState(CallState.IN_CALL);

            System.out.println("Incoming Call -> IN_CALL");

        } else {

            System.out.println(
                    "Warning: No IncomingCallSession found");

        }

        System.out.println("================================");
    }
}