package com.sipclient.sip.factory;

import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

public class RequestFactory {

    protected final AddressFactory addressFactory;
    protected final HeaderFactory headerFactory;
    protected final MessageFactory messageFactory;

    public RequestFactory(
            AddressFactory addressFactory,
            HeaderFactory headerFactory,
            MessageFactory messageFactory) {

        this.addressFactory = addressFactory;
        this.headerFactory = headerFactory;
        this.messageFactory = messageFactory;
    }

}