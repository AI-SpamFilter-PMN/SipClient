package com.sipclient.sip.dialog;

public class DialogManager {

    private final CallSession currentSession;

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

}