package com.sipclient.sip.factory;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.SipProvider;
import javax.sip.message.Request;

public class ByeRequestFactory {

    private final SipProvider sipProvider;

    public ByeRequestFactory(SipProvider sipProvider) {

        this.sipProvider = sipProvider;

    }

    public ClientTransaction create(Dialog dialog)
            throws Exception {

        Request bye = dialog.createRequest(Request.BYE);

        return sipProvider.getNewClientTransaction(bye);

    }

}