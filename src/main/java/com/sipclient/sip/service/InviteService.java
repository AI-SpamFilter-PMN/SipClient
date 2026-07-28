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
import com.sipclient.sip.dialog.DialogManager;
import com.sipclient.sip.dialog.CallState;
import javax.sip.Dialog;
import com.sipclient.sip.factory.ByeRequestFactory;
import javax.sip.Dialog;
import javax.sip.ClientTransaction;
import javax.sip.message.Request;

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
    private final DialogManager dialogManager;
    private final ByeRequestFactory byeRequestFactory;

 public InviteService(
        SipProvider sipProvider,
        AddressFactory addressFactory,
        HeaderFactory headerFactory,
        MessageFactory messageFactory,
        InviteRequestFactory inviteRequestFactory,
        DialogManager dialogManager) {

    this.sipProvider = sipProvider;

    this.inviteRequestFactory = inviteRequestFactory;

    this.dialogManager = dialogManager;
    this.byeRequestFactory =
        new ByeRequestFactory(sipProvider);

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

dialogManager.getCurrentSession()
        .setClientTransaction(transaction);

dialogManager.getCurrentSession()
        .setRemoteUser(destination);

dialogManager.setState(CallState.CALLING);

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

    if (dialogManager.getState() == CallState.TERMINATING) {

        dialogManager.reset();
        System.out.println("Call Finished");
        break;
    }

    Dialog dialog = dialogManager
            .getCurrentSession()
            .getDialog();

    if (dialog != null
            && dialogManager.getState() != CallState.IN_CALL) {

        try {

            javax.sip.header.CSeqHeader cseq =
        (javax.sip.header.CSeqHeader)
                response.getHeader(javax.sip.header.CSeqHeader.NAME);

Request ack = dialog.createAck(cseq.getSeqNumber());

dialog.sendAck(ack);



            System.out.println("ACK Sent");

        } catch (Exception e) {

            e.printStackTrace();

        }

        dialogManager.setState(CallState.IN_CALL);

        System.out.println("Call Connected");
    }

    break;

            default:

                break;

        }

    }

public void hangup() {


    try {

        Dialog dialog =
                dialogManager
                        .getCurrentSession()
                        .getDialog();

        if (dialog == null) {

            System.out.println("No active dialog.");

            return;

        }

        ClientTransaction transaction =
                byeRequestFactory.create(dialog);

        dialog.sendRequest(transaction);

        dialogManager.setState(CallState.TERMINATING);

        System.out.println();
        System.out.println("========== BYE ==========");
        System.out.println(transaction.getRequest());
        System.out.println("=========================");
        System.out.println("BYE Sent Successfully");

    } catch (Exception e) {

        e.printStackTrace();

    }

}


}