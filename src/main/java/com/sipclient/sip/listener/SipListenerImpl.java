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

import com.sipclient.sip.service.InviteService;
import com.sipclient.sip.service.RegisterService;
import com.sipclient.sip.dialog.DialogManager;
import com.sipclient.sip.dialog.CallState;
import com.sipclient.sip.handler.RequestDispatcher;

public class SipListenerImpl implements SipListener {

    private final RegisterService registerService;
    private final InviteService inviteService;
    private final DialogManager dialogManager;
    private final RequestDispatcher requestDispatcher;

    public SipListenerImpl(
            RegisterService registerService,
            InviteService inviteService,
            DialogManager dialogManager,
            RequestDispatcher requestDispatcher) {

        this.registerService = registerService;
        this.inviteService = inviteService;
        this.dialogManager = dialogManager;
        this.requestDispatcher = requestDispatcher;
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {

        Request request = requestEvent.getRequest();

        System.out.println();
        System.out.println("========== INCOMING SIP REQUEST ==========");
        System.out.println("Method      : " + request.getMethod());
        System.out.println("Call-ID     : " + request.getHeader("Call-ID"));
        System.out.println("CSeq        : " + request.getHeader("CSeq"));
        System.out.println("From        : " + request.getHeader("From"));
        System.out.println("To          : " + request.getHeader("To"));
        System.out.println("Request-URI : " + request.getRequestURI());
        System.out.println("Dialog      : " + requestEvent.getDialog());
        System.out.println("ServerTx    : " + requestEvent.getServerTransaction());
        System.out.println("==========================================");

        if (Request.INVITE.equals(request.getMethod())) {
            byte[] rawContent = request.getRawContent();
            if (rawContent != null && rawContent.length > 0) {
                String sdpBody = new String(rawContent);
                dialogManager.handleIncomingSdp(sdpBody);
            }
        }

        requestDispatcher.dispatch(requestEvent);
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {

        Response response = responseEvent.getResponse();

        if (responseEvent.getDialog() != null) {
            dialogManager.getCurrentSession().setDialog(responseEvent.getDialog());
            System.out.println("Dialog Stored Successfully");
        }

        System.out.println();
        System.out.println("========== SIP RESPONSE ==========");
        System.out.println(response);
        System.out.println("==================================");

        System.out.println("Status Code : " + response.getStatusCode());
        System.out.println("Reason      : " + response.getReasonPhrase());

        WWWAuthenticateHeader authHeader =
                (WWWAuthenticateHeader) response.getHeader(WWWAuthenticateHeader.NAME);

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

        Iterator<String> headers = response.getHeaderNames();

        while (headers.hasNext()) {
            Header header = response.getHeader(headers.next());
            System.out.println(header);
        }

        CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

        if (cseq == null) {
            return;
        }

        String method = cseq.getMethod();

        if (Request.REGISTER.equals(method)) {

            registerService.handleResponse(response);

        } else if (Request.INVITE.equals(method)) {

            int statusCode = response.getStatusCode();

            switch (statusCode) {
                case Response.TRYING:
                    dialogManager.setState(CallState.CALLING);
                    break;

                case Response.RINGING:
                    dialogManager.setState(CallState.RINGING);
                    break;

                case Response.OK:
                    break;

                default:
                    if (statusCode >= 400) {
                        dialogManager.setState(CallState.DISCONNECTED);
                        dialogManager.reset();
                    }
                    break;
            }

            inviteService.handleResponse(response);

        } else if (Request.BYE.equals(method)) {

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
        dialogManager.setState(CallState.DISCONNECTED);
        dialogManager.reset();
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IO Exception");
        dialogManager.setState(CallState.DISCONNECTED);
        dialogManager.reset();
    }
@Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {

        System.out.println("Transaction Terminated (Ignored to keep call active)");
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("Dialog Terminated Event Received");

        if (dialogManager.getState() == CallState.TERMINATING || dialogManager.getState() == CallState.DISCONNECTED) {
            dialogManager.setState(CallState.DISCONNECTED);
            dialogManager.reset();
        } else {
            System.out.println("Dialog Terminated ignored because call is still active in state: " + dialogManager.getState());
        }
        
    }
    
}