package com.sipclient.sip.auth;

import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;

import javax.sip.header.AuthorizationHeader;
import javax.sip.header.HeaderFactory;

public class AuthorizationHeaderBuilder {

    private final HeaderFactory headerFactory;
    private final AddressFactory addressFactory;

    public AuthorizationHeaderBuilder(
            HeaderFactory headerFactory,
            AddressFactory addressFactory) {

        this.headerFactory = headerFactory;
        this.addressFactory = addressFactory;

    }

    public AuthorizationHeader build(
            String username,
            String realm,
            String nonce,
            String uri,
            String response,
            String opaque,
            String cnonce,
            String nc,
            String qop) throws Exception {

        AuthorizationHeader authorization =
                headerFactory.createAuthorizationHeader("Digest");

        authorization.setUsername(username);
        authorization.setRealm(realm);
        authorization.setNonce(nonce);

       SipURI sipURI =
        (SipURI) addressFactory.createURI(uri);


authorization.setURI(sipURI);

authorization.setURI(sipURI);
        authorization.setResponse(response);

        authorization.setAlgorithm("MD5");

        authorization.setOpaque(opaque);

        authorization.setQop(qop);

        authorization.setCNonce(cnonce);

        authorization.setNonceCount(
                Integer.parseInt(nc, 16));

        return authorization;

    }

}