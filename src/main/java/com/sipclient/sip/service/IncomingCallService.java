
package com.sipclient.sip.service;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipProvider;

import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;

import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;

import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import com.sipclient.sip.dialog.CallState;
import com.sipclient.sip.dialog.DialogManager;
import com.sipclient.sip.model.IncomingCallSession;
import javax.sip.header.ToHeader;

public class IncomingCallService {

    private final SipProvider sipProvider;
    private final MessageFactory messageFactory;
    private final DialogManager dialogManager;

    private final AddressFactory addressFactory;
    private final HeaderFactory headerFactory;

    public IncomingCallService(
            SipProvider sipProvider,
            MessageFactory messageFactory,
            AddressFactory addressFactory,
            HeaderFactory headerFactory,
            DialogManager dialogManager) {

        this.sipProvider = sipProvider;
        this.messageFactory = messageFactory;
        this.addressFactory = addressFactory;
        this.headerFactory = headerFactory;
        this.dialogManager = dialogManager;
    }

    private ServerTransaction getOrCreateServerTransaction(
            RequestEvent event,
            Request request) throws Exception {

        ServerTransaction serverTransaction =
                event.getServerTransaction();

        if (serverTransaction == null) {

            serverTransaction =
                    sipProvider.getNewServerTransaction(request);
        }

        IncomingCallSession session =
                new IncomingCallSession();

        session.setServerTransaction(serverTransaction);
        session.setInviteRequest(request);
        session.setDialog(serverTransaction.getDialog());

        FromHeader fromHeader =
                (FromHeader) request.getHeader(FromHeader.NAME);

        if (fromHeader != null) {

            Address address =
                    fromHeader.getAddress();

            if (address.getURI() instanceof SipURI) {

                SipURI uri =
                        (SipURI) address.getURI();

                session.setCallerNumber(uri.getUser());
            }
        }

        CallIdHeader callIdHeader =
                (CallIdHeader) request.getHeader(
                        CallIdHeader.NAME);

        if (callIdHeader != null) {

            session.setCallId(
                    callIdHeader.getCallId());
        }

        dialogManager.setIncomingCallSession(session);

        return serverTransaction;
    }

    private void sendTrying(
            ServerTransaction serverTransaction,
            Request request) throws Exception {

        Response trying =
                messageFactory.createResponse(
                        Response.TRYING,
                        request);

        serverTransaction.sendResponse(trying);

        System.out.println("100 Trying Sent");
    }

    private void sendRinging(
            ServerTransaction serverTransaction,
            Request request) throws Exception {

        Response ringing =
                messageFactory.createResponse(
                        Response.RINGING,
                        request);

        serverTransaction.sendResponse(ringing);

        System.out.println("180 Ringing Sent");
    }

  public void onIncomingInvite(RequestEvent event) {
    try {
        Request request = event.getRequest();

        ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
        boolean isReInvite = (toHeader != null && toHeader.getTag() != null && !toHeader.getTag().isEmpty())
                || dialogManager.getState() == CallState.IN_CALL;

        if (isReInvite) {
            System.out.println("====== Re-INVITE Received ======");
            ServerTransaction st = event.getServerTransaction();
            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
            }

            Response ok = messageFactory.createResponse(Response.OK, request);
            
            SipURI contactURI = addressFactory.createSipURI("1001", "127.0.0.1");
            contactURI.setPort(5070);
            Address contactAddress = addressFactory.createAddress(contactURI);
            ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
            ok.addHeader(contactHeader);

            
            if (request.getContent() != null) {
                ok.setContent(request.getContent(), headerFactory.createContentTypeHeader("application", "sdp"));
            }

            st.sendResponse(ok);
            System.out.println("200 OK sent for Re-INVITE");
            System.out.println("================================");
            return;
        }

        System.out.println("Request-URI : " + request.getRequestURI());
        System.out.println("From        : " + request.getHeader("From"));
        System.out.println("To          : " + request.getHeader("To"));
        System.out.println("Contact     : " + request.getHeader("Contact"));
        System.out.println("Via         : " + request.getHeader("Via"));

        System.out.println("================================");
        System.out.println("Incoming INVITE");
        System.out.println("Method : " + request.getMethod());
        System.out.println("Call-ID: " + request.getHeader("Call-ID"));
        System.out.println("CSeq   : " + request.getHeader("CSeq"));

        ServerTransaction serverTransaction = getOrCreateServerTransaction(event, request);

        sendTrying(serverTransaction, request);
        sendRinging(serverTransaction, request);

        dialogManager.setState(CallState.RINGING);

        IncomingCallSession session = dialogManager.getIncomingCallSession();

        if (session != null) {
            System.out.println("Incoming Call -> RINGING");
            System.out.println("Caller -> " + session.getCallerNumber());
            System.out.println("Call-ID -> " + session.getCallId());
        }

        System.out.println("Waiting for Accept / Reject");
        System.out.println("================================");

    } catch (Exception e) {
        e.printStackTrace();
    }
}

   public void acceptIncomingCall() {

        try {

            IncomingCallSession session =
                    dialogManager.getIncomingCallSession();

            if (session == null) {
                System.out.println("No incoming call to accept.");
                return;
            }

            ServerTransaction serverTransaction =
                    session.getServerTransaction();

            Request request =
                    session.getInviteRequest();

            if (serverTransaction == null || request == null) {
                System.out.println("Invalid incoming call session.");
                return;
            }

            Response ok =
                    messageFactory.createResponse(
                            Response.OK,
                            request);

            SipURI contactURI =
                    addressFactory.createSipURI(
                            "1001",
                            "127.0.0.1");

            contactURI.setPort(5070);

            Address contactAddress =
                    addressFactory.createAddress(
                            contactURI);

            ContactHeader contactHeader =
                    headerFactory.createContactHeader(
                            contactAddress);

            ok.addHeader(contactHeader);

            if (request.getContent() != null) {
                ok.setContent(
                        request.getContent(),
                        headerFactory.createContentTypeHeader("application", "sdp"));
            }

            serverTransaction.sendResponse(ok);

            dialogManager.setState(CallState.IN_CALL);

            System.out.println("200 OK Sent - Incoming Call Accepted");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rejectIncomingCall() {

        try {

            IncomingCallSession session =
                    dialogManager.getIncomingCallSession();

            if (session == null) {

                System.out.println(
                        "No incoming call to reject.");

                return;
            }

            ServerTransaction serverTransaction =
                    session.getServerTransaction();

            Request request =
                    session.getInviteRequest();

            if (serverTransaction == null || request == null) {

                System.out.println(
                        "Invalid incoming call session.");

                return;
            }

            Response reject =
                    messageFactory.createResponse(
                            Response.BUSY_HERE,
                            request);

            serverTransaction.sendResponse(reject);

            dialogManager.setState(
                    CallState.DISCONNECTED);

            System.out.println(
                    "486 Busy Here Sent - Incoming Call Rejected");

            dialogManager.reset();

            System.out.println(
                    "Incoming Call Session Reset");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}

