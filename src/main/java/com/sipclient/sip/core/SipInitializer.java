package com.sipclient.sip.core;

import java.util.Properties;
import java.util.TooManyListenersException;

import javax.sip.ListeningPoint;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.SipStack;

import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

import com.sipclient.sip.factory.InviteRequestFactory;
import com.sipclient.sip.listener.SipListenerImpl;

public class SipInitializer {

    private SipFactory sipFactory;
    private SipStack sipStack;
    private ListeningPoint listeningPoint;
    private SipProvider sipProvider;

    private AddressFactory addressFactory;
    private HeaderFactory headerFactory;
    private MessageFactory messageFactory;

    private InviteRequestFactory inviteRequestFactory;

    public void initialize() throws Exception {

        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");

        addressFactory = sipFactory.createAddressFactory();
        headerFactory = sipFactory.createHeaderFactory();
        messageFactory = sipFactory.createMessageFactory();

        Properties properties = new Properties();

        properties.setProperty(
                "javax.sip.STACK_NAME",
                "SipClient");

        properties.setProperty(
                "gov.nist.javax.sip.TRACE_LEVEL",
                "16");

        sipStack = sipFactory.createSipStack(properties);

        listeningPoint =
                sipStack.createListeningPoint(
                        "127.0.0.1",
                        5070,
                        ListeningPoint.UDP);

        sipProvider =
                sipStack.createSipProvider(listeningPoint);

        inviteRequestFactory =
                new InviteRequestFactory(
                        sipProvider,
                        addressFactory,
                        headerFactory,
                        messageFactory);

        System.out.println("✓ SIP Stack Created");

    }

    public void registerListener(
            SipListenerImpl listener)
            throws TooManyListenersException {

        sipProvider.addSipListener(listener);

        System.out.println("✓ SIP Listener Registered");

    }

    public SipProvider getSipProvider() {
        return sipProvider;
    }

    public AddressFactory getAddressFactory() {
        return addressFactory;
    }

    public HeaderFactory getHeaderFactory() {
        return headerFactory;
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    public InviteRequestFactory getInviteRequestFactory() {
        return inviteRequestFactory;
    }

}