package com.sipclient.controller;

import com.sipclient.sip.core.SipManager;
import com.sipclient.sip.model.SipAccount;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class MainController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField domainField;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField destinationField;

    private final SipManager sipManager = new SipManager();

    @FXML
    public void onRegister() {

        SipAccount account = new SipAccount(
                usernameField.getText(),
                passwordField.getText(),
                domainField.getText()
        );

        sipManager.initialize(account);

        statusLabel.setText("Initializing...");
    }

    @FXML
    public void onCall() {

        System.out.println("Call button clicked");
        System.out.println("Destination: " + destinationField.getText());

    }

}