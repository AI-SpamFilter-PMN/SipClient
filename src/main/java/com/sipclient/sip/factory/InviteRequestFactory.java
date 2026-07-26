package com.sipclient.sip.factory;

import com.sipclient.sip.builder.HeaderBuilder;
import com.sipclient.sip.builder.RequestUriBuilder;
import com.sipclient.sip.builder.SdpBuilder;
import com.sipclient.sip.model.SipAccount;

import java.util.List;

import javax.sip.SipProvider;

import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;

import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;

import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.header.ContentTypeHeader;
import com.sipclient.sip.builder.SdpBuilder;

public class InviteRequestFactory {

    private final MessageFactory messageFactory;

    private final HeaderBuilder headerBuilder;
    private final RequestUriBuilder requestUriBuilder;
    private final SdpBuilder sdpBuilder;

    public InviteRequestFactory(
            SipProvider sipProvider,
            AddressFactory addressFactory,
            HeaderFactory headerFactory,
            MessageFactory messageFactory) {

        this.messageFactory = messageFactory;

        this.headerBuilder =
                new HeaderBuilder(
                        sipProvider,
                        headerFactory,
                        addressFactory);

        this.requestUriBuilder =
                new RequestUriBuilder(addressFactory);

        this.sdpBuilder =
                new SdpBuilder();

    }

    public Request create(
            SipAccount account,
            String destination)
            throws Exception {

        SipURI requestURI =
                requestUriBuilder.build(
                        destination,
                        account.getDomain());

        CallIdHeader callId =
                headerBuilder.buildCallId();

        FromHeader from =
                headerBuilder.buildFrom(account);

        ToHeader to =
                headerBuilder.buildTo(
                        destination,
                        account.getDomain());

        CSeqHeader cseq =
                headerBuilder.buildCSeq(
                        1L,
                        Request.INVITE);

        List<ViaHeader> viaHeaders =
                headerBuilder.buildViaHeaders();

        MaxForwardsHeader maxForwards =
                headerBuilder.buildMaxForwards();

        ContactHeader contact =
                headerBuilder.buildContact(account);

        Request request =
                messageFactory.createRequest(
                        requestURI,
                        Request.INVITE,
                        callId,
                        cseq,
                        from,
                        to,
                        viaHeaders,
                        maxForwards);

        request.addHeader(contact);

        String sdp =
                sdpBuilder.build(account);

        ContentTypeHeader contentType =
                headerBuilder.buildContentType();

        request.setContent(
                sdp,
                contentType);

        return request;

    }
    public Request createAuthenticated(
        SipAccount account,
        String destination,
        CallIdHeader callId,
        FromHeader from,
        long sequence,
        javax.sip.header.AuthorizationHeader authorization)
        throws Exception {

    CSeqHeader cseq =
            headerBuilder.buildCSeq(
                    sequence,
                    Request.INVITE);

    Request request =
            buildInvite(
                    account,
                    destination,
                    callId,
                    from,
                    cseq);

    request.addHeader(authorization);

    return request;
}
    
private Request buildInvite(
        SipAccount account,
        String destination,
        CallIdHeader callId,
        FromHeader from,
        CSeqHeader cseq)
        throws Exception {

    SipURI requestURI =
        requestUriBuilder.build(
                destination,
                account.getDomain());

    List<ViaHeader> viaHeaders =
            headerBuilder.buildViaHeaders();

    ToHeader to =
            headerBuilder.buildTo(
                    destination,
                    account.getDomain());

    MaxForwardsHeader max =
            headerBuilder.buildMaxForwards();

    ContactHeader contact =
            headerBuilder.buildContact(account);

    String sdp =
        sdpBuilder.build(account);

    ContentTypeHeader contentType =
        headerBuilder.buildContentType();

    Request request =
            messageFactory.createRequest(
                    requestURI,
                    Request.INVITE,
                    callId,
                    cseq,
                    from,
                    to,
                    viaHeaders,
                    max);

    request.addHeader(contact);

    request.setContent(
            sdp,
            contentType);

    return request;

}


}