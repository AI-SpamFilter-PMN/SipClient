package com.sipclient.sip.service;

import com.sipclient.sip.factory.RegisterRequestFactory;
import com.sipclient.sip.model.SipAccount;

import javax.sip.SipProvider;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

public class RegisterService {

    private final SipProvider sipProvider;
    private final RegisterRequestFactory requestFactory;

    public RegisterService(SipProvider sipProvider,
                           AddressFactory addressFactory,
                           HeaderFactory headerFactory,
                           MessageFactory messageFactory) {

        this.sipProvider = sipProvider;

        this.requestFactory = new RegisterRequestFactory(
                sipProvider,
                addressFactory,
                headerFactory,
                messageFactory
        );
    }

    public void register(SipAccount account) {

        requestFactory.create(account);

    }

    public SipProvider getSipProvider() {
        return sipProvider;
    }
}