package com.sipclient.sip.handler;

import javax.sip.RequestEvent;
import javax.sip.message.Request;

public class RequestDispatcher {

    private final IncomingInviteHandler inviteHandler;
private final ByeRequestHandler byeHandler;
private final CancelRequestHandler cancelHandler;
private final OptionsRequestHandler optionsHandler;
private final AckRequestHandler ackHandler;

   public RequestDispatcher(
        IncomingInviteHandler inviteHandler,
        ByeRequestHandler byeHandler,
        CancelRequestHandler cancelHandler,
        OptionsRequestHandler optionsHandler,
        AckRequestHandler ackHandler) {

    this.inviteHandler = inviteHandler;
    this.byeHandler = byeHandler;
    this.cancelHandler = cancelHandler;
    this.optionsHandler = optionsHandler;
    this.ackHandler = ackHandler;
}

    public void dispatch(RequestEvent event) {

    String method = event.getRequest().getMethod();

    switch (method) {

        case Request.INVITE:
            inviteHandler.handle(event);
            break;

        case Request.ACK:
            ackHandler.handle(event);
            break;

        case Request.BYE:
            byeHandler.handle(event);
            break;

        case Request.CANCEL:
            cancelHandler.handle(event);
            break;

        case Request.OPTIONS:
            optionsHandler.handle(event);
            break;

        default:
            System.out.println("Unhandled request: " + method);
    }
}
}