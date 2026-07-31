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
import javafx.scene.media.AudioClip;
import java.net.URL;

public class MainController {

    private SipAccount account;

    private final SipManager sipManager = new SipManager();

    private Timeline timer;
    private AudioClip ringtone;
    private boolean ringtonePlaying = false;

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
    private Label incomingCallerLabel;

    @FXML
    private Button answerButton;

    @FXML
    private Button rejectButton;

    @FXML
    public void onRegister() {

        account = new SipAccount(
                usernameField.getText().trim(),
                passwordField.getText().trim(),
                domainField.getText().trim());

        sipManager.initialize(account);

        statusLabel.setText("Initializing...");

        if (timer != null) {
            timer.stop();
        }

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
                stopRingtone();
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
                statusLabel.setText("Incoming Call / Ringing");
                updateIncomingCallUI();
                startRingtone();
                break;

            case IN_CALL:
                stopRingtone();
                statusLabel.setText("In Call");
                answerButton.setDisable(true);
                rejectButton.setDisable(true);
                break;

            case DISCONNECTED:
                stopRingtone(); 
                statusLabel.setText("Disconnected");
                answerButton.setDisable(true);
                rejectButton.setDisable(true);
                incomingCallerLabel.setText("---------");
                break;

            default:
                stopRingtone();
                statusLabel.setText(state.toString());
                break;
        }
    }

    private void updateIncomingCallUI() {

        var session = sipManager.getDialogManager().getIncomingCallSession();

        if (session == null) {
            return;
        }

        String caller = session.getCallerNumber();

        if (caller == null || caller.isEmpty()) {
            caller = "Unknown";
        }

        incomingCallerLabel.setText(caller);
        answerButton.setDisable(false);
        rejectButton.setDisable(false);
    }

    @FXML
    public void onAnswer() {

        if (sipManager.getIncomingCallService() == null) {
            return;
        }

        if (sipManager.getCallState() != CallState.RINGING) {
            return;
        }

        stopRingtone();

        sipManager.getIncomingCallService().acceptIncomingCall();

        answerButton.setDisable(true);
        rejectButton.setDisable(true);
    }

    @FXML
    public void onReject() {

        if (sipManager.getIncomingCallService() == null) {
            return;
        }

        if (sipManager.getCallState() != CallState.RINGING) {
            return;
        }

        stopRingtone();

        sipManager.getIncomingCallService().rejectIncomingCall();

        answerButton.setDisable(true);
        rejectButton.setDisable(true);

        incomingCallerLabel.setText("---------");
    }

    private synchronized void startRingtone() {

        if (ringtonePlaying) {
            return;
        }

        try {

            if (ringtone == null) {

                URL resource = getClass().getResource("/sounds/ringtone.wav");

                if (resource == null) {
                    System.err.println("Ringtone resource not found!");
                    return;
                }

                System.out.println("Ringtone path: " + resource);
                ringtone = new AudioClip(resource.toExternalForm());
                ringtone.setVolume(1.0);
                ringtone.setCycleCount(AudioClip.INDEFINITE);
            }

            ringtonePlaying = true;
            ringtone.play();

            System.out.println("Ringtone started");

        } catch (Exception e) {
            ringtonePlaying = false;
            System.err.println("Failed to start ringtone:");
            e.printStackTrace();
        }
    }

    private synchronized void stopRingtone() {

        if (ringtonePlaying || ringtone != null) {
            if (ringtone != null) {
                ringtone.stop();
                ringtone = null; 
            }
            ringtonePlaying = false;
            System.out.println("Ringtone stopped successfully");
        }
    }
}