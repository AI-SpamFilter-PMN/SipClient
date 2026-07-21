package com.sipclient.sip.factory;

import com.sipclient.sip.builder.HeaderBuilder;
import com.sipclient.sip.builder.RequestUriBuilder;
import com.sipclient.sip.model.SipAccount;

import java.util.List;

import javax.sip.SipProvider;

import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;

import javax.sip.header.CallIdHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;

import javax.sip.message.MessageFactory;

public class RegisterRequestFactory {

    private final SipProvider sipProvider;
    private final MessageFactory messageFactory;

    private final RequestUriBuilder requestUriBuilder;
    private final HeaderBuilder headerBuilder;

    public RegisterRequestFactory(
            SipProvider sipProvider,
            AddressFactory addressFactory,
            HeaderFactory headerFactory,
            MessageFactory messageFactory) {

        this.sipProvider = sipProvider;
        this.messageFactory = messageFactory;

        this.requestUriBuilder =
                new RequestUriBuilder(addressFactory);

        this.headerBuilder =
                new HeaderBuilder(
                        sipProvider,
                        headerFactory,
                        addressFactory);

    }

    public void create(SipAccount account) {

        try {

            SipURI requestURI =
                    requestUriBuilder.build(account);

            List<ViaHeader> viaHeaders =
                    headerBuilder.buildViaHeaders();

            FromHeader from =
                    headerBuilder.buildFrom(account);

            ToHeader to =
                    headerBuilder.buildTo(account);

            CallIdHeader callId =
                    headerBuilder.buildCallId();

            CSeqHeader cseq =
                    headerBuilder.buildCSeq();

            MaxForwardsHeader max =
                    headerBuilder.buildMaxForwards();

            System.out.println();
            System.out.println("========== REGISTER ==========");
            System.out.println("Request URI : " + requestURI);
            System.out.println();

            System.out.println(viaHeaders.get(0));
            System.out.println(from);
            System.out.println(to);
            System.out.println(callId);
            System.out.println(cseq);
            System.out.println(max);

            System.out.println();
            System.out.println("SipProvider : " + sipProvider);
            System.out.println("MessageFactory : " + messageFactory);
            System.out.println("==============================");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}