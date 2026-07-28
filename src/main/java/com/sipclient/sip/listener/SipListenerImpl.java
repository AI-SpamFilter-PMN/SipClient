package com.sipclient.sip.listener;

import java.util.Iterator;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;

import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.header.WWWAuthenticateHeader;

import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.ResponseEvent;

import com.sipclient.sip.service.InviteService;
import com.sipclient.sip.service.RegisterService;
import com.sipclient.sip.dialog.DialogManager;
import com.sipclient.sip.dialog.CallState;
import javax.sip.Dialog;

public class SipListenerImpl implements SipListener {

    private final RegisterService registerService;

    private final InviteService inviteService;
    private final DialogManager dialogManager;

    public SipListenerImpl(
        RegisterService registerService,
        InviteService inviteService,
        DialogManager dialogManager) {

    this.registerService = registerService;
    this.inviteService = inviteService;
    this.dialogManager = dialogManager;

}

    @Override
public void processRequest(RequestEvent requestEvent) {

    Request request = requestEvent.getRequest();

    System.out.println("Received Request: "
            + request.getMethod());

    if (Request.BYE.equals(request.getMethod())) {

        dialogManager.setState(CallState.DISCONNECTED);

        dialogManager.reset();

    }

}

    @Override
    public void processResponse(ResponseEvent responseEvent) {

        Response response = responseEvent.getResponse();
        if (responseEvent.getDialog() != null) {

    dialogManager.getCurrentSession()
            .setDialog(responseEvent.getDialog());

}
if (responseEvent.getDialog() != null) {

    System.out.println("Dialog Stored Successfully");

}

        System.out.println();
        System.out.println("========== SIP RESPONSE ==========");
        System.out.println(response);
        System.out.println("==================================");

        System.out.println("Status Code : "
                + response.getStatusCode());

        System.out.println("Reason      : "
                + response.getReasonPhrase());

        WWWAuthenticateHeader authHeader =
                (WWWAuthenticateHeader)
                        response.getHeader(
                                WWWAuthenticateHeader.NAME);

        if (authHeader != null) {

            System.out.println();
            System.out.println("========== AUTH ==========");
            System.out.println("Realm     : " + authHeader.getRealm());
            System.out.println("Nonce     : " + authHeader.getNonce());
            System.out.println("Opaque    : " + authHeader.getOpaque());
            System.out.println("Algorithm : " + authHeader.getAlgorithm());
            System.out.println("Qop       : " + authHeader.getQop());
            System.out.println("==========================");

        }

        System.out.println();
        System.out.println("Headers:");

        Iterator<String> headers =
                response.getHeaderNames();

        while (headers.hasNext()) {

            Header header =
                    response.getHeader(headers.next());

            System.out.println(header);

        }

        CSeqHeader cseq =
                (CSeqHeader) response.getHeader(
                        CSeqHeader.NAME);

        if (cseq == null) {
            return;
        }

        String method =
                cseq.getMethod();

        if (Request.REGISTER.equals(method)) {

    registerService.handleResponse(response);

}
else if (Request.INVITE.equals(method)) {

    switch (response.getStatusCode()) {

        case Response.TRYING:
            dialogManager.setState(CallState.CALLING);
            break;

        case Response.RINGING:
            dialogManager.setState(CallState.RINGING);
            break;



        default:
            break;
    }

    inviteService.handleResponse(response);

}
else if (Request.BYE.equals(method)) {

    if (response.getStatusCode() == Response.OK) {

        System.out.println();
        System.out.println("BYE Completed");

        dialogManager.setState(CallState.DISCONNECTED);
dialogManager.reset();

    }

}

    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {

        System.out.println("Request Timeout");

    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {

        System.out.println("IO Exception");

    }

    @Override
    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {

        System.out.println("Transaction Terminated");

    }

    @Override
    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {

        System.out.println("Dialog Terminated");

    }

}