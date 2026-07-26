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

    /*
     * Via
     */
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

    /*
     * From (Automatic Tag)
     */
    public FromHeader buildFrom(
            SipAccount account)
            throws Exception {

        return buildFrom(
                account.getUsername(),
                account.getDomain(),
                Long.toHexString(
                        System.currentTimeMillis()));

    }

    /*
     * From (Custom Tag)
     */
    public FromHeader buildFrom(
            SipAccount account,
            String tag)
            throws Exception {

        return buildFrom(
                account.getUsername(),
                account.getDomain(),
                tag);

    }

    /*
     * Generic From
     */
    public FromHeader buildFrom(
            String username,
            String domain,
            String tag)
            throws Exception {

        SipURI uri =
                addressFactory.createSipURI(
                        username,
                        domain);

        Address address =
                addressFactory.createAddress(uri);

        return headerFactory.createFromHeader(
                address,
                tag);

    }

    /*
     * To (Register)
     */
    public ToHeader buildTo(
            SipAccount account)
            throws Exception {

        return buildTo(
                account.getUsername(),
                account.getDomain());

    }

    /*
     * Generic To
     */
    public ToHeader buildTo(
            String username,
            String domain)
            throws Exception {

        SipURI uri =
                addressFactory.createSipURI(
                        username,
                        domain);

        Address address =
                addressFactory.createAddress(uri);

        return headerFactory.createToHeader(
                address,
                null);

    }

    /*
     * Call-ID
     */
    public CallIdHeader buildCallId() {

        return sipProvider.getNewCallId();

    }

    /*
     * REGISTER CSeq
     */
    public CSeqHeader buildCSeq()
            throws Exception {

        return buildCSeq(
                1L,
                "REGISTER");

    }

    /*
     * Generic CSeq
     */
    public CSeqHeader buildCSeq(
            long sequence,
            String method)
            throws Exception {

        return headerFactory.createCSeqHeader(
                sequence,
                method);

    }

    /*
     * Max-Forwards
     */
    public MaxForwardsHeader buildMaxForwards()
            throws Exception {

        return headerFactory.createMaxForwardsHeader(
                70);

    }

    /*
     * Contact
     */
    public ContactHeader buildContact(
            SipAccount account)
            throws Exception {

        SipURI uri =
                addressFactory.createSipURI(
                        account.getUsername(),
                        SipConfig.LOCAL_IP);

        uri.setPort(
                SipConfig.LOCAL_PORT);

        Address address =
                addressFactory.createAddress(uri);

        return headerFactory.createContactHeader(
                address);

    }

    /*
     * Expires
     */
    public ExpiresHeader buildExpires()
            throws Exception {

        return headerFactory.createExpiresHeader(
                3600);

    }

    public ContentTypeHeader buildContentType()
        throws Exception {

    return headerFactory.createContentTypeHeader(
            "application",
            "sdp");

}

}