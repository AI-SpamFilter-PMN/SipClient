package com.sipclient.sip.service;

import com.sipclient.sip.auth.InviteAuthenticator;
import com.sipclient.sip.factory.InviteRequestFactory;
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

public class InviteService implements SipResponseHandler {

    private final SipProvider sipProvider;

    private final InviteRequestFactory inviteRequestFactory;

    private final InviteAuthenticator authenticator;

    private SipAccount currentAccount;

    private String currentDestination;

    private CallIdHeader currentCallId;

    private FromHeader currentFromHeader;

    private long currentCSeq = 1;

    private boolean authenticationAttempted = false;

    public InviteService(
            SipProvider sipProvider,
            AddressFactory addressFactory,
            HeaderFactory headerFactory,
            MessageFactory messageFactory,
            InviteRequestFactory inviteRequestFactory) {

        this.sipProvider = sipProvider;

        this.inviteRequestFactory = inviteRequestFactory;

        this.authenticator =
                new InviteAuthenticator(
                        sipProvider,
                        inviteRequestFactory,
                        headerFactory,
                        addressFactory);

    }

    public void call(
            SipAccount account,
            String destination) {

        try {

            currentAccount = account;

            currentDestination = destination;

            Request invite =
                    inviteRequestFactory.create(
                            account,
                            destination);

            currentCallId =
                    (CallIdHeader)
                            invite.getHeader(
                                    CallIdHeader.NAME);

            currentFromHeader =
                    (FromHeader)
                            invite.getHeader(
                                    FromHeader.NAME);

            currentCSeq = 1;

            authenticationAttempted = false;

            System.out.println();
            System.out.println("========== INVITE ==========");
            System.out.println(invite);
            System.out.println("============================");

            ClientTransaction transaction =
                    sipProvider.getNewClientTransaction(
                            invite);

            transaction.sendRequest();

            System.out.println();
            System.out.println("INVITE Sent Successfully");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    @Override
    public void handleResponse(Response response) {

        switch (response.getStatusCode()) {

            case Response.UNAUTHORIZED:

                if (authenticationAttempted) {

                    System.out.println("INVITE Authentication Failed");

                    return;

                }

                authenticationAttempted = true;

                WWWAuthenticateHeader authenticateHeader =
                        (WWWAuthenticateHeader)
                                response.getHeader(
                                        WWWAuthenticateHeader.NAME);

                authenticator.authenticate(
                        currentAccount,
                        currentDestination,
                        currentCallId,
                        currentFromHeader,
                        ++currentCSeq,
                        authenticateHeader);

                break;

            case Response.TRYING:

                System.out.println();
                System.out.println("Trying...");

                break;

            case Response.RINGING:

                System.out.println();
                System.out.println("Ringing...");

                break;

            case Response.OK:

                System.out.println();
                System.out.println("Call Connected");

                break;

            default:

                break;

        }

    }

}