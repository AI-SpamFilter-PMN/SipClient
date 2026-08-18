# SIP Client

A desktop SIP softphone built in Java — handles SIP signaling (REGISTER, INVITE, BYE, CANCEL, OPTIONS) and real-time voice calls over RTP, from scratch, with a JavaFX interface.

> Graduation Project — ITI (Information Technology Institute)

## Overview

This is a fully functional VoIP client that can register to a SIP server, place and receive calls, and stream live two-way audio. Unlike many student SIP projects that only implement signaling, this one includes a hand-built RTP media engine — capturing microphone audio, encoding it to G.711 (µ-law), packetizing it into RTP, and decoding incoming audio in real time.

## Features

- **SIP Registration** with Digest Authentication (RFC 2617 — HA1/HA2 challenge-response)
- **Outgoing calls** — INVITE flow with authentication retry on `401`/`407`
- **Incoming calls** — ringing UI, ringtone playback, answer/reject
- **Call teardown** — BYE, CANCEL, and ACK handling
- **Keep-alive** — OPTIONS request handling
- **Live audio** — real-time microphone capture, G.711 µ-law encode/decode, and RTP packet streaming over UDP
- **JavaFX UI** — register, dial, hang up, and answer/reject incoming calls

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Build | Maven |
| UI | JavaFX 21 (FXML) |
| SIP stack | JAIN-SIP (`jain-sip-ri`) |
| Audio I/O | `javax.sound.sampled` |
| Logging | Log4j |

## Architecture

The codebase is organized by responsibility rather than by layer, which keeps the SIP protocol logic isolated from the UI:

```
com.sipclient
├── app/            → JavaFX application entry point
├── controller/      → JavaFX controller (UI <-> SipManager glue)
├── service/         → High-level app services
└── sip/
    ├── auth/         → Digest authentication (REGISTER & INVITE)
    ├── builder/      → SIP request/header/SDP builders
    ├── config/       → Static SIP network configuration
    ├── core/         → SipInitializer (stack bootstrap) & SipManager (facade)
    ├── dialog/       → Call session & dialog state
    ├── factory/      → SIP request factories (INVITE/REGISTER/BYE)
    ├── handler/      → Per-method request handlers + dispatcher
    ├── listener/      → JAIN-SIP SipListener implementation
    ├── media/        → RTP media engine & SDP parsing
    ├── model/        → SIP account & incoming call session models
    ├── service/       → Register/Invite/IncomingCall orchestration services
    └── state/        → Call state enum
```

**Flow at a glance:**
`MainController` → `SipManager` → `{RegisterService, InviteService, IncomingCallService}` → JAIN-SIP `SipProvider` → network. Incoming SIP messages come back through `SipListenerImpl` → `RequestDispatcher` → the relevant handler, and audio is handed off to `RtpMediaEngine` once an SDP offer is parsed.

## Getting Started

### Prerequisites

- JDK 25+
- Maven 3.9+
- A reachable SIP server/PBX (e.g. Asterisk, FreeSWITCH, or any SIP provider) and account credentials

### Configuration

Local network settings are defined in [`SipConfig.java`](src/main/java/com/sipclient/sip/config/SipConfig.java):

```java
LOCAL_IP    = "192.168.1.7"
LOCAL_PORT  = 5070
TRANSPORT   = "udp"
SERVER_PORT = 5066
```

Update `LOCAL_IP` to match your machine's network address before running.

### Run

```bash
mvn clean javafx:run
```

Then, in the app:
1. Enter your SIP username, password, and domain, and click **Register**.
2. Enter a destination and click **Call** to place a call, or wait for an incoming call to answer/reject.

## Known Limitations

- SDP parsing supports a single audio media line (`m=audio`) — no codec negotiation beyond G.711 µ-law
- RTP local port is fixed rather than dynamically allocated
- Network config (`SipConfig`) is static and requires a code change to point at a different network/server

## License

No license specified yet.
