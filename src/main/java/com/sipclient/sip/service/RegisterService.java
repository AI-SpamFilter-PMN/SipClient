package com.sipclient.sip.service;

import com.sipclient.sip.auth.AuthorizationHeaderBuilder;
import com.sipclient.sip.auth.DigestCalculator;
import com.sipclient.sip.auth.RegisterAuthenticator;
import com.sipclient.sip.factory.RegisterRequestFactory;
import com.sipclient.sip.handler.SipResponseHandler;
import com.sipclient.sip.model.SipAccount;

import javax.sip.ClientTransaction;
import javax.sip.SipProvider;

import javax.sip.address.AddressFactory;

import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.WWWAuthenticateHeader;

import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class RegisterService implements SipResponseHandler {

    private final SipProvider sipProvider;

    private final AddressFactory addressFactory;
    private final HeaderFactory headerFactory;
    private final MessageFactory messageFactory;

    private final RegisterRequestFactory requestFactory;
    private final RegisterAuthenticator authenticator;

    private SipAccount currentAccount;

    private CallIdHeader currentCallId;
    private FromHeader currentFromHeader;
    private long currentCSeq = 1;
    private boolean authenticationAttempted = false;

    public RegisterService(
            SipProvider sipProvider,
            AddressFactory addressFactory,
            HeaderFactory headerFactory,
            MessageFactory messageFactory) {

        this.sipProvider = sipProvider;

        this.addressFactory = addressFactory;
        this.headerFactory = headerFactory;
        this.messageFactory = messageFactory;

        this.requestFactory =
                new RegisterRequestFactory(
                        sipProvider,
                        addressFactory,
                        headerFactory,
                        messageFactory);

       this.authenticator =
        new RegisterAuthenticator(
                sipProvider,
                requestFactory,
                headerFactory,
                addressFactory);

    }

    public void register(SipAccount account) {

        this.currentAccount = account;

        Request request =
                requestFactory.create(account);

        if (request == null) {
            return;
        }

        currentCallId =
                (CallIdHeader)
                        request.getHeader(
                                CallIdHeader.NAME);

        currentFromHeader =
                (FromHeader)
                        request.getHeader(
                                FromHeader.NAME);

        currentCSeq = 1;

        System.out.println();
        System.out.println("========== REGISTER REQUEST ==========");
        System.out.println(request);
        System.out.println("======================================");

        try {

            ClientTransaction transaction =
                    sipProvider.getNewClientTransaction(
                            request);

            transaction.sendRequest();

            System.out.println("REGISTER Sent Successfully");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    @Override
    public void handleResponse(Response response) {

        switch (response.getStatusCode()) {

           case Response.UNAUTHORIZED:

    if (authenticationAttempted) {

        System.out.println("Authentication Failed");
        return;

    }

    authenticationAttempted = true;

    System.out.println();
    System.out.println("RegisterService -> Authentication Required");

    WWWAuthenticateHeader authenticateHeader =
            (WWWAuthenticateHeader)
                    response.getHeader(
                            WWWAuthenticateHeader.NAME);

    authenticator.authenticate(
            currentAccount,
            currentCallId,
            currentFromHeader,
            ++currentCSeq,
            authenticateHeader);

    break;

            case Response.OK:

                System.out.println();
                System.out.println("REGISTER SUCCESS");

                break;

            default:

                break;

        }

    }

    public SipProvider getSipProvider() {

        return sipProvider;

    }

    public SipAccount getCurrentAccount() {

        return currentAccount;

    }

}