package com.sipclient.sip.core;

import java.util.Properties;
import javax.sip.*;
import com.sipclient.sip.listener.SipListenerImpl;

public class SipInitializer {

    private SipFactory sipFactory;
    private SipStack sipStack;
    private ListeningPoint listeningPoint;
    private SipProvider sipProvider;

    public SipProvider initialize() throws Exception {

        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");

        Properties properties = new Properties();

        properties.setProperty("javax.sip.STACK_NAME", "SipClient");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "16");

        sipStack = sipFactory.createSipStack(properties);

        listeningPoint = sipStack.createListeningPoint(
                "127.0.0.1",
                5070,
                ListeningPoint.UDP);

sipProvider = sipStack.createSipProvider(listeningPoint);
sipProvider.addSipListener(new SipListenerImpl());
System.out.println("✓ SIP Stack Created");
System.out.println("✓ SIP Listener Registered");

        return sipProvider;
    }
}