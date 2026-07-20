package com.sipclient.controller;

import com.sipclient.sip.core.SipManager;
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
    private TextField destinationField;

    @FXML
    private Label statusLabel;

    @FXML
    private Label incomingCallerLabel;

    private final SipManager sipManager = new SipManager();

    @FXML
    private void onRegister() {

        System.out.println("Register button clicked");

        sipManager.initialize();

        statusLabel.setText("Initializing...");
    }

    @FXML
    private void onCall() {

        System.out.println("Call button clicked");

        System.out.println("Destination: " + destinationField.getText());

    }

}