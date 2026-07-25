package com.sipclient.sip.listener;

import java.util.Iterator;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;

import javax.sip.header.Header;
import javax.sip.header.WWWAuthenticateHeader;

import javax.sip.message.Response;

import com.sipclient.sip.handler.SipResponseHandler;

public class SipListenerImpl implements SipListener {

    private final SipResponseHandler responseHandler;

    public SipListenerImpl(SipResponseHandler responseHandler) {

        this.responseHandler = responseHandler;

    }

    @Override
    public void processRequest(RequestEvent requestEvent) {

        System.out.println("Received Request: "
                + requestEvent.getRequest().getMethod());

    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {

        Response response = responseEvent.getResponse();

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

        Iterator<String> headers = response.getHeaderNames();

        while (headers.hasNext()) {

            Header header =
                    response.getHeader(headers.next());

            System.out.println(header);

        }

        if (responseHandler != null) {

            responseHandler.handleResponse(response);

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