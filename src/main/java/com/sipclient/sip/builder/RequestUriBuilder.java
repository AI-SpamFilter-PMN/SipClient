package com.sipclient.sip.builder;

import com.sipclient.sip.config.SipConfig;
import com.sipclient.sip.model.SipAccount;

import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;

public class RequestUriBuilder {

    private final AddressFactory addressFactory;

    public RequestUriBuilder(AddressFactory addressFactory) {
        this.addressFactory = addressFactory;
    }

    public SipURI build(SipAccount account) throws Exception {

        SipURI uri =
                addressFactory.createSipURI(
                        null,
                        account.getDomain());

        uri.setPort(SipConfig.SERVER_PORT);

        return uri;
    }
}