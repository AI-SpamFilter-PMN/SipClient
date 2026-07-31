package com.sipclient.sip.handler;

import com.sipclient.sip.service.IncomingCallService;
import javax.sip.RequestEvent;

public class IncomingInviteHandler {

    private final IncomingCallService incomingCallService;

public IncomingInviteHandler(
        IncomingCallService incomingCallService) {

    this.incomingCallService = incomingCallService;
}

    public void handle(RequestEvent event) {

        incomingCallService.onIncomingInvite(event);

    }
}