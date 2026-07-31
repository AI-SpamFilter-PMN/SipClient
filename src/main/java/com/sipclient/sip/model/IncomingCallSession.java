package com.sipclient.sip.model;

import javax.sip.Dialog;
import javax.sip.ServerTransaction;
import javax.sip.message.Request;

public class IncomingCallSession {

    private ServerTransaction serverTransaction;
    private Dialog dialog;
    private Request inviteRequest;
    private String callerNumber;
    private String callId;

    public ServerTransaction getServerTransaction() {
        return serverTransaction;
    }

    public void setServerTransaction(ServerTransaction serverTransaction) {
        this.serverTransaction = serverTransaction;
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public Request getInviteRequest() {
        return inviteRequest;
    }

    public void setInviteRequest(Request inviteRequest) {
        this.inviteRequest = inviteRequest;
    }

    public String getCallerNumber() {
        return callerNumber;
    }

    public void setCallerNumber(String callerNumber) {
        this.callerNumber = callerNumber;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }
}