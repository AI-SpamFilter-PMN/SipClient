package com.sipclient.sip.builder;

import com.sipclient.sip.config.SipConfig;
import com.sipclient.sip.model.SipAccount;

import java.util.ArrayList;
import java.util.List;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;

import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;

import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.ContentTypeHeader;

public class HeaderBuilder {

    private final SipProvider sipProvider;
    private final HeaderFactory headerFactory;
    private final AddressFactory addressFactory;

    public HeaderBuilder(
            SipProvider sipProvider,
            HeaderFactory headerFactory,
            AddressFactory addressFactory) {

        this.sipProvider = sipProvider;
        this.headerFactory = headerFactory;
        this.addressFactory = addressFactory;
    }

    public List<ViaHeader> buildViaHeaders() throws Exception {

        List<ViaHeader> headers = new ArrayList<>();

        ListeningPoint listeningPoint = sipProvider.getListeningPoint("udp");

        String ip = listeningPoint != null ? listeningPoint.getIPAddress() : SipConfig.LOCAL_IP;
        int port = listeningPoint != null ? listeningPoint.getPort() : SipConfig.LOCAL_PORT;

        if ("0.0.0.0".equals(ip) || "127.0.0.1".equals(ip)) {
            ip = SipConfig.LOCAL_IP;
        }

        ViaHeader via =
                headerFactory.createViaHeader(
                        ip,
                        port,
                        ListeningPoint.UDP,
                        null);

        headers.add(via);

        return headers;
    }

    public FromHeader buildFrom(SipAccount account) throws Exception {
        return buildFrom(
                account.getUsername(),
                account.getDomain(),
                Long.toHexString(System.currentTimeMillis()));
    }

    public FromHeader buildFrom(SipAccount account, String tag) throws Exception {
        return buildFrom(account.getUsername(), account.getDomain(), tag);
    }

    public FromHeader buildFrom(String username, String domain, String tag) throws Exception {
        SipURI uri = addressFactory.createSipURI(username, domain);
        Address address = addressFactory.createAddress(uri);
        return headerFactory.createFromHeader(address, tag);
    }

    public ToHeader buildTo(SipAccount account) throws Exception {
        return buildTo(account.getUsername(), account.getDomain());
    }

    public ToHeader buildTo(String username, String domain) throws Exception {
        SipURI uri = addressFactory.createSipURI(username, domain);
        Address address = addressFactory.createAddress(uri);
        return headerFactory.createToHeader(address, null);
    }

    public CallIdHeader buildCallId() {
        return sipProvider.getNewCallId();
    }

    public CSeqHeader buildCSeq() throws Exception {
        return buildCSeq(1L, "REGISTER");
    }

    public CSeqHeader buildCSeq(long sequence, String method) throws Exception {
        return headerFactory.createCSeqHeader(sequence, method);
    }

    public MaxForwardsHeader buildMaxForwards() throws Exception {
        return headerFactory.createMaxForwardsHeader(70);
    }

    public ContactHeader buildContact(SipAccount account) throws Exception {
        SipURI uri = addressFactory.createSipURI(account.getUsername(), SipConfig.LOCAL_IP);
        uri.setPort(SipConfig.LOCAL_PORT);
        Address address = addressFactory.createAddress(uri);
        return headerFactory.createContactHeader(address);
    }

    public ExpiresHeader buildExpires() throws Exception {
        return headerFactory.createExpiresHeader(3600);
    }

    public ContentTypeHeader buildContentType() throws Exception {
        return headerFactory.createContentTypeHeader("application", "sdp");
    }
}