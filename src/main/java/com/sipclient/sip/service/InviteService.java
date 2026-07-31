package com.sipclient.sip.service;

import com.sipclient.sip.auth.InviteAuthenticator;
import com.sipclient.sip.factory.InviteRequestFactory;
import com.sipclient.sip.handler.SipResponseHandler;
import com.sipclient.sip.model.SipAccount;
import com.sipclient.sip.dialog.DialogManager;
import com.sipclient.sip.dialog.CallState;
import com.sipclient.sip.factory.ByeRequestFactory;
import com.sipclient.sip.model.IncomingCallSession; 

import javax.sip.ClientTransaction;
import javax.sip.SipProvider;
import javax.sip.address.AddressFactory;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.Dialog;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class InviteService implements SipResponseHandler {

    private final SipProvider sipProvider;
    private final InviteRequestFactory inviteRequestFactory;
    private final InviteAuthenticator authenticator;
    private final DialogManager dialogManager;
    private final ByeRequestFactory byeRequestFactory;

    private SipAccount currentAccount;
    private String currentDestination;
    private CallIdHeader currentCallId;
    private FromHeader currentFromHeader;
    private long currentCSeq = 1;
    private boolean authenticationAttempted = false;

   
    private Clip ringtoneClip;

    public InviteService(
            SipProvider sipProvider,
            AddressFactory addressFactory,
            HeaderFactory headerFactory,
            MessageFactory messageFactory,
            InviteRequestFactory inviteRequestFactory,
            DialogManager dialogManager) {

        this.sipProvider = sipProvider;
        this.inviteRequestFactory = inviteRequestFactory;
        this.dialogManager = dialogManager;
        this.byeRequestFactory = new ByeRequestFactory(sipProvider);

        this.authenticator = new InviteAuthenticator(
                sipProvider,
                inviteRequestFactory,
                headerFactory,
                addressFactory);
    }

    public void call(
            SipAccount account,
            String destination) {

        try {
            currentAccount = account;
            currentDestination = destination;

            Request invite = inviteRequestFactory.create(
                    account,
                    destination);

            currentCallId = (CallIdHeader) invite.getHeader(CallIdHeader.NAME);
            currentFromHeader = (FromHeader) invite.getHeader(FromHeader.NAME);
            currentCSeq = 1;
            authenticationAttempted = false;

            System.out.println();
            System.out.println("========== INVITE ==========");
            System.out.println(invite);
            System.out.println("============================");

            ClientTransaction transaction = sipProvider.getNewClientTransaction(invite);

            dialogManager.getCurrentSession().setClientTransaction(transaction);
            dialogManager.getCurrentSession().setRemoteUser(destination);
            dialogManager.setState(CallState.CALLING);

            transaction.sendRequest();

            System.out.println();
            System.out.println("INVITE Sent Successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleResponse(Response response) {

        int statusCode = response.getStatusCode();

        switch (statusCode) {

            case Response.UNAUTHORIZED:

                if (authenticationAttempted) {
                    System.out.println("INVITE Authentication Failed");
                    return;
                }

                authenticationAttempted = true;

                WWWAuthenticateHeader authenticateHeader =
                        (WWWAuthenticateHeader) response.getHeader(WWWAuthenticateHeader.NAME);

                authenticator.authenticate(
                        currentAccount,
                        currentDestination,
                        currentCallId,
                        currentFromHeader,
                        ++currentCSeq,
                        authenticateHeader);

                break;

            case Response.TRYING:

                System.out.println();
                System.out.println("Trying...");
                break;

            case Response.RINGING:

                System.out.println();
                System.out.println("Ringing...");

                dialogManager.setState(CallState.RINGING);
                startRingtone(); 

                break;

            case Response.OK:

                stopRingtone(); 

                if (dialogManager.getState() == CallState.TERMINATING) {
                    dialogManager.reset();
                    System.out.println("Call Finished");
                    break;
                }

                Dialog dialog = dialogManager
                        .getCurrentSession()
                        .getDialog();

                if (dialog != null && dialogManager.getState() != CallState.IN_CALL) {

                    try {
                        javax.sip.header.CSeqHeader cseq =
                                (javax.sip.header.CSeqHeader) response.getHeader(javax.sip.header.CSeqHeader.NAME);

                        Request ack = dialog.createAck(cseq.getSeqNumber());
                        dialog.sendAck(ack);

                        System.out.println("ACK Sent");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    dialogManager.setState(CallState.IN_CALL);
                    System.out.println("Call Connected");
                }

                break;

            default:

                if (statusCode >= 400) {

                    System.out.println();
                    System.out.println("Call Rejected / Failed with status: " + statusCode + " " + response.getReasonPhrase());

                    stopRingtone(); 

                    dialogManager.setState(CallState.DISCONNECTED);
                    dialogManager.reset();
                }

                break;
        }
    }

    public void hangup() {
        try {
            Dialog dialog = null;

            if (dialogManager.getCurrentSession() != null) {
                dialog = dialogManager.getCurrentSession().getDialog();
            }

            if (dialog == null && dialogManager.getIncomingCallSession() != null) {
                IncomingCallSession session = dialogManager.getIncomingCallSession();
                if (session.getServerTransaction() != null) {
                    dialog = session.getServerTransaction().getDialog();
                }
            }

            if (dialog == null) {
                System.out.println("No active dialog to hangup.");
                stopRingtone();
                return;
            }

            Request byeRequest = dialog.createRequest(Request.BYE);
            ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(byeRequest);
            dialog.sendRequest(clientTransaction);

            System.out.println("====== BYE REQUEST SENT ======");
            System.out.println(byeRequest);
            System.out.println("==============================");

            stopRingtone(); 
            dialogManager.setState(CallState.DISCONNECTED);

        } catch (Exception e) {
            System.err.println("Failed to send BYE request:");
            e.printStackTrace();
        }
    }

    // =======================================================
    // 🎧 إدارة تشغيل وإيقاف الصوت من الـ Resources
    // =======================================================

    private synchronized void startRingtone() {
        try {
            if (ringtoneClip != null && ringtoneClip.isRunning()) {
                return;
            }

            InputStream audioSrc = getClass().getResourceAsStream("/sounds/ringtone.wav");
            
            if (audioSrc == null) {
                System.err.println("Audio file not found in /sounds/ringtone.wav!");
                return;
            }

            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);

            ringtoneClip = AudioSystem.getClip();
            ringtoneClip.open(audioStream);
            ringtoneClip.loop(Clip.LOOP_CONTINUOUSLY);
            ringtoneClip.start();

        } catch (Exception e) {
            System.err.println("Error playing ringtone: " + e.getMessage());
        }
    }

    private synchronized void stopRingtone() {
        try {
            if (ringtoneClip != null) {
                if (ringtoneClip.isRunning()) {
                    ringtoneClip.stop();
                }
                ringtoneClip.close();
                ringtoneClip = null;
            }
        } catch (Exception e) {
            System.err.println("Error stopping ringtone: " + e.getMessage());
        }
    }
}