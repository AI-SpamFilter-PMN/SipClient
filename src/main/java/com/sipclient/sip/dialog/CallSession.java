package com.sipclient.sip.dialog;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.ServerTransaction;

public class CallSession {

    private Dialog dialog;

    private ClientTransaction clientTransaction;

    private ServerTransaction serverTransaction;

    private String remoteUser;

    private CallState state = CallState.IDLE;

    public Dialog getDialog() {
        return dialog;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public ClientTransaction getClientTransaction() {
        return clientTransaction;
    }

    public void setClientTransaction(ClientTransaction clientTransaction) {
        this.clientTransaction = clientTransaction;
    }

    public ServerTransaction getServerTransaction() {
        return serverTransaction;
    }

    public void setServerTransaction(ServerTransaction serverTransaction) {
        this.serverTransaction = serverTransaction;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    public CallState getState() {
        return state;
    }

    public void setState(CallState state) {
        this.state = state;
    }
    public void clear() {

    dialog = null;
    clientTransaction = null;
    serverTransaction = null;
    remoteUser = null;
    state = CallState.IDLE;

}

}