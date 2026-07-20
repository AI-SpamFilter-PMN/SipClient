package com.sipclient.sip.listener;

import javax.sip.*;

public class SipListenerImpl implements SipListener {

    @Override
    public void processRequest(RequestEvent requestEvent) {
        System.out.println("Received Request: "
                + requestEvent.getRequest().getMethod());
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        System.out.println("Received Response: "
                + responseEvent.getResponse().getStatusCode());
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