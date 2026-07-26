package com.sipclient.sip.auth;

import com.sipclient.sip.factory.InviteRequestFactory;
import com.sipclient.sip.model.SipAccount;

import java.util.UUID;

import javax.sip.ClientTransaction;
import javax.sip.SipProvider;

import javax.sip.address.AddressFactory;

import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.WWWAuthenticateHeader;

import javax.sip.message.Request;

public class InviteAuthenticator {

    private final SipProvider sipProvider;

    private final InviteRequestFactory requestFactory;

    private final AuthorizationHeaderBuilder authorizationBuilder;

    private final DigestCalculator digestCalculator;

    public InviteAuthenticator(
            SipProvider sipProvider,
            InviteRequestFactory requestFactory,
            HeaderFactory headerFactory,
            AddressFactory addressFactory) {

        this.sipProvider = sipProvider;
        this.requestFactory = requestFactory;

        this.authorizationBuilder =
                new AuthorizationHeaderBuilder(
                        headerFactory,
                        addressFactory);

        this.digestCalculator =
                new DigestCalculator();
    }

    public void authenticate(
            SipAccount account,
            String destination,
            CallIdHeader callId,
            FromHeader fromHeader,
            long sequence,
            WWWAuthenticateHeader authHeader) {

        try {

            String realm = authHeader.getRealm();
            String nonce = authHeader.getNonce();
            String opaque = authHeader.getOpaque();
            String qop = authHeader.getQop();

            String uri =
                    "sip:" + destination + "@"
                    + account.getDomain()
                    + ":5060";

            String nc = "00000001";

            String cnonce =
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0,16);

            String response =
                    digestCalculator.calculateResponse(
                            account.getUsername(),
                            realm,
                            account.getPassword(),
                            Request.INVITE,
                            uri,
                            nonce,
                            nc,
                            cnonce,
                            qop);

            System.out.println();
            System.out.println("========== INVITE DIGEST ==========");
            System.out.println("Response : " + response);
            System.out.println("===================================");

            AuthorizationHeader authorization =
                    authorizationBuilder.build(
                            account.getUsername(),
                            realm,
                            nonce,
                            uri,
                            response,
                            opaque,
                            cnonce,
                            nc,
                            qop);

            Request request =
                    requestFactory.createAuthenticated(
                            account,
                            destination,
                            callId,
                            fromHeader,
                            sequence,
                            authorization);

            System.out.println();
            System.out.println("===== AUTH INVITE =====");
            System.out.println(request);
            System.out.println("=======================");

            ClientTransaction transaction =
                    sipProvider.getNewClientTransaction(request);

            transaction.sendRequest();

            System.out.println();
            System.out.println("Authenticated INVITE Sent");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}