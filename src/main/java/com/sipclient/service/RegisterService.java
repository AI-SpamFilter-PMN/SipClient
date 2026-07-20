package com.sipclient.sip.service;

import com.sipclient.sip.factory.RequestFactory;

import javax.sip.SipProvider;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

public class RegisterService extends RequestFactory {

    private final SipProvider sipProvider;

    public RegisterService(
            SipProvider sipProvider,
            AddressFactory addressFactory,
            HeaderFactory headerFactory,
            MessageFactory messageFactory) {

        super(addressFactory, headerFactory, messageFactory);

        this.sipProvider = sipProvider;
    }

    public void register() {

        System.out.println("Creating REGISTER request...");

    }

}