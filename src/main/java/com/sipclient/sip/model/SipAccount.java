package com.sipclient.sip.model;

public class SipAccount {

    private final String username;
    private final String password;
    private final String domain;
    private int port = 5066; 

    public SipAccount(String username,
                      String password,
                      String domain) {

        this.username = username;
        this.password = password;
        this.domain = domain;
    }

    public SipAccount(String username,
                      String password,
                      String domain,
                      int port) {

        this.username = username;
        this.password = password;
        this.domain = domain;
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDomain() {
        return domain;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}