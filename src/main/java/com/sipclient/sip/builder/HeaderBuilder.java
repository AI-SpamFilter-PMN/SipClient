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

import javax.sip.header.CallIdHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;

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

        ViaHeader via =
                headerFactory.createViaHeader(
                        SipConfig.LOCAL_IP,
                        SipConfig.LOCAL_PORT,
                        ListeningPoint.UDP,
                        null);

        headers.add(via);

        return headers;
    }

    public FromHeader buildFrom(SipAccount account) throws Exception {

        SipURI uri =
                addressFactory.createSipURI(
                        account.getUsername(),
                        account.getDomain());

        Address address =
                addressFactory.createAddress(uri);

        return headerFactory.createFromHeader(
                address,
                Long.toHexString(System.currentTimeMillis()));
    }

    public ToHeader buildTo(SipAccount account) throws Exception {

        SipURI uri =
                addressFactory.createSipURI(
                        account.getUsername(),
                        account.getDomain());

        Address address =
                addressFactory.createAddress(uri);

        return headerFactory.createToHeader(address, null);
    }

    public CallIdHeader buildCallId() {

        return sipProvider.getNewCallId();

    }

    public CSeqHeader buildCSeq() throws Exception {

        return headerFactory.createCSeqHeader(
                1L,
                "REGISTER");

    }

    public MaxForwardsHeader buildMaxForwards() throws Exception {

        return headerFactory.createMaxForwardsHeader(70);

    }

    public ContactHeader buildContact(SipAccount account) throws Exception {

        SipURI uri =
                addressFactory.createSipURI(
                        account.getUsername(),
                        SipConfig.LOCAL_IP);

        uri.setPort(SipConfig.LOCAL_PORT);

        Address address =
                addressFactory.createAddress(uri);

        return headerFactory.createContactHeader(address);

    }

    public ExpiresHeader buildExpires() throws Exception {

        return headerFactory.createExpiresHeader(3600);

    }

}