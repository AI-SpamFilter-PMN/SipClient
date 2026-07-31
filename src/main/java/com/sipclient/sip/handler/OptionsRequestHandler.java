package com.sipclient.sip.handler;

import javax.sip.RequestEvent;

public class OptionsRequestHandler {

    public void handle(RequestEvent event) {

        System.out.println("Incoming OPTIONS received.");

    }

}