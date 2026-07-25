package com.sipclient.sip.handler;

import javax.sip.message.Response;

public interface SipResponseHandler {

    void handleResponse(Response response);

}