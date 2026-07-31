package com.sipclient.sip.dialog;
import com.sipclient.sip.model.IncomingCallSession;
public class DialogManager {

    private final CallSession currentSession;
    private IncomingCallSession incomingCallSession;

    public DialogManager() {

        currentSession = new CallSession();

    }

    public CallSession getCurrentSession() {

        return currentSession;

    }


    public void setState(CallState state) {

        currentSession.setState(state);

        System.out.println("Call State -> " + state);

    }

    public CallState getState() {

        return currentSession.getState();

    }

    public void reset() {

       currentSession.clear();

    }

    public IncomingCallSession getIncomingCallSession() {
    return incomingCallSession;
}

public void setIncomingCallSession(IncomingCallSession incomingCallSession) {
    this.incomingCallSession = incomingCallSession;
}

}