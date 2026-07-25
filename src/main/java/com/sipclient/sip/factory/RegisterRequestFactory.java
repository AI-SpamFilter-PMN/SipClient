package com.sipclient.sip.factory;

import com.sipclient.sip.builder.HeaderBuilder;
import com.sipclient.sip.builder.RequestUriBuilder;
import com.sipclient.sip.model.SipAccount;

import java.util.List;

import javax.sip.SipProvider;

import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;

import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;

import javax.sip.message.MessageFactory;
import javax.sip.message.Request;

public class RegisterRequestFactory {

    private final MessageFactory messageFactory;

    private final RequestUriBuilder requestUriBuilder;
    private final HeaderBuilder headerBuilder;

    public RegisterRequestFactory(
            SipProvider sipProvider,
            AddressFactory addressFactory,
            HeaderFactory headerFactory,
            MessageFactory messageFactory) {

        this.messageFactory = messageFactory;

        this.requestUriBuilder =
                new RequestUriBuilder(addressFactory);

        this.headerBuilder =
                new HeaderBuilder(
                        sipProvider,
                        headerFactory,
                        addressFactory);

    }

    /*
     * First REGISTER
     */

    public Request create(SipAccount account) {

        try {

            CallIdHeader callId =
                    headerBuilder.buildCallId();

            FromHeader from =
                    headerBuilder.buildFrom(account);

            CSeqHeader cseq =
                    headerBuilder.buildCSeq();

            return buildRegister(
                    account,
                    callId,
                    from,
                    cseq,
                    null);

        } catch (Exception e) {

            e.printStackTrace();

            return null;

        }

    }

    /*
     * REGISTER with Authorization
     */

    public Request createAuthenticated(
            SipAccount account,
            CallIdHeader callId,
            FromHeader from,
            long sequence,
            AuthorizationHeader authorizationHeader) {

        try {

            CSeqHeader cseq =
                    headerBuilder.buildCSeq(sequence);

            return buildRegister(
                    account,
                    callId,
                    from,
                    cseq,
                    authorizationHeader);

        } catch (Exception e) {

            e.printStackTrace();

            return null;

        }

    }

    /*
     * Internal Builder
     */

    private Request buildRegister(
            SipAccount account,
            CallIdHeader callId,
            FromHeader from,
            CSeqHeader cseq,
            AuthorizationHeader authorizationHeader)
            throws Exception {

        SipURI requestURI =
                requestUriBuilder.build(account);

        List<ViaHeader> viaHeaders =
                headerBuilder.buildViaHeaders();

        ToHeader to =
                headerBuilder.buildTo(account);

        MaxForwardsHeader max =
                headerBuilder.buildMaxForwards();

        ContactHeader contact =
                headerBuilder.buildContact(account);

        ExpiresHeader expires =
                headerBuilder.buildExpires();

        Request request =
                messageFactory.createRequest(
                        requestURI,
                        Request.REGISTER,
                        callId,
                        cseq,
                        from,
                        to,
                        viaHeaders,
                        max);

        request.addHeader(contact);

        request.addHeader(expires);

        if (authorizationHeader != null) {

            request.addHeader(
                    authorizationHeader);

        }

        return request;

    }

}