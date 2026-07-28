package com.sipclient.controller;

import com.sipclient.sip.core.SipManager;
import com.sipclient.sip.dialog.CallState;
import com.sipclient.sip.model.SipAccount;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class MainController {

    private SipAccount account;

    private final SipManager sipManager = new SipManager();

    private Timeline timer;

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

    @FXML
    private Button callButton;

    @FXML
    private Button hangupButton;

    @FXML
    public void onRegister() {

        account = new SipAccount(
                usernameField.getText().trim(),
                passwordField.getText().trim(),
                domainField.getText().trim());

        sipManager.initialize(account);

        statusLabel.setText("Initializing...");

        timer = new Timeline(
                new KeyFrame(
                        Duration.millis(300),
                        e -> updateCallStatus()));

        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    @FXML
    public void onCall() {

        if (account == null) {

            System.out.println("Please register first.");
            return;

        }

        String destination = destinationField.getText().trim();

        if (destination.isEmpty()) {

            System.out.println("Destination is empty.");
            return;

        }

        System.out.println("Calling " + destination);

        sipManager.call(account, destination);

    }

    @FXML
    public void onHangup() {

        if (sipManager.getInviteService() == null) {

            return;

        }

        CallState state = sipManager.getCallState();

System.out.println("Current State = " + state);

if (state != CallState.IN_CALL) {
    System.out.println("Cannot hangup. Not in active call.");
    return;
}

sipManager.getInviteService().hangup();

    }

    private void updateCallStatus() {

        CallState state = sipManager.getCallState();

        switch (state) {

            case IDLE:
                statusLabel.setText("Idle");
                break;

            case REGISTERING:
                statusLabel.setText("Registering...");
                break;

            case REGISTERED:
                statusLabel.setText("Registered");
                break;

            case CALLING:
                statusLabel.setText("Calling...");
                break;

            case RINGING:
                statusLabel.setText("Ringing...");
                break;

            case IN_CALL:
                statusLabel.setText("In Call");
                break;

            case DISCONNECTED:
                statusLabel.setText("Disconnected");
                break;

            default:
                statusLabel.setText(state.toString());
                break;
        }
    }

}