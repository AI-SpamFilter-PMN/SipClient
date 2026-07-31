package com.sipclient.sip.handler;

import javax.sip.RequestEvent;

public class CancelRequestHandler {

    public void handle(RequestEvent event) {

        System.out.println("Incoming CANCEL received.");

    }

}