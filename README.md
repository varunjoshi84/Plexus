<div align="center">

# ⚡ P L E X U S ⚡
### *Autonomous Hybrid Mesh & Cloud Real-Time Messaging Engine*

[![Platform](https://img.shields.io/badge/Platform-Android_24%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-Auth_%7C_Firestore_%7C_FCM-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue?style=for-the-badge)](LICENSE)

<br/>

> **"Zero Infrastructure, Zero Internet, Zero Compromise."**  
> Plexus is an offline-first, dual-engine messaging protocol for Android designed for mission-critical communication during internet outages, remote expeditions, local LAN environments, or standard cloud connectivity.

[📥 Download Latest APK](https://drive.google.com/file/d/1wUeEcyu4OCz-8iWTlLKPyyDTbJiwaJr9/view?usp=drivesdk) • [🚀 Architecture](#-system-architecture) • [📊 Data Flow (DFD)](#-data-flow-diagrams-dfd) • [⚡ Tech Stack](#-tech-stack--libraries)

---

</div>

## 🌌 Overview

Most messaging apps fail the moment internet connectivity drops. **Plexus solves this fundamental resilience flaw** by implementing a dynamic routing protocol that multiplexes communication between **local peer-to-peer ad-hoc local network connections** and **global cloud backends**:

```
                       ┌───────────────────────────┐
                       │   PLEXUS ROUTING ENGINE   │
                       └─────────────┬─────────────┘
                                     │
           ┌─────────────────────────┴─────────────────────────┐
           ▼                                                   ▼
 🛰️  [ INTERNET MODE ]                                📡  [ LOCAL MESH MODE ]
  • Cloud Firestore Sync                              • Zero-Config NSD (mDNS)
  • Firebase Phone Auth (OTP)                         • NanoWSD Embedded Server
  • FCM Priority Push Notifications                   • Full-Duplex WebSockets
  • Global Range                                      • Zero Internet Required (<100ms Latency)
```

---

## 📊 Data Flow Diagrams (DFD)

### 🔹 DFD Level 0 — Context Diagram

```mermaid
flowchart TD
    Sender["👤 Sender (Plexus Client)"]
    Receiver["👥 Receiver (Plexus Client)"]
    System(("⚡ Plexus Hybrid Messaging System"))
    Firebase[("🔥 Firebase Cloud\n(Auth, Firestore, FCM)")]
    LAN[("📡 Local Wi-Fi / Hotspot LAN\n(mDNS / WebSockets)")]

    Sender -->|"Auth request, Cloud messages, User queries"| System
    System -->|"Auth verification, Synced chats, Push notifications"| Sender

    System <-->|"WAN: Encrypted message payloads & Auth tokens"| Firebase
    System <-->|"LAN: Service broadcasts, Socket payloads"| LAN

    Receiver -->|"Peer handshake, Message read/write"| System
    System -->|"In-app alerts, UI state updates, Push toasts"| Receiver
```

---

### 🔹 DFD Level 1 — High-Level Functional Decomposition

```mermaid
flowchart LR
    subgraph UI ["📱 Jetpack Compose UI Layer"]
        Input["Compose Text Input"]
        Display["Chat Stream Display"]
    end

    subgraph Core ["🧠 Orchestration & ViewModel"]
        Router{"Mode Resolver\n(Local vs Cloud)"}
        State["StateFlow & Coroutine Scope"]
    end

    subgraph LocalEngine ["📡 Local Engine (Offline LAN)"]
        NSD["NSD / mDNS Discovery"]
        NanoServer["NanoWSD Server"]
        OkClient["OkHttp WebSocket Client"]
    end

    subgraph CloudEngine ["🔥 Cloud Engine (WAN)"]
        FStore[("Firestore DB")]
        FCM["FCM Notification Service"]
    end

    Input --> Router
    Router -->|Offline / LAN Selected| LocalEngine
    Router -->|Global Mode Selected| CloudEngine

    LocalEngine -->|P2P Socket Delivery| Display
    CloudEngine -->|Real-time Snapshot Listener| Display
    State --> Display
```

---

### 🔹 DFD Level 2 — Detailed Message Transmission & Reception

```mermaid
sequenceDiagram
    autonumber
    actor Alice as 👤 Alice (Sender)
    participant UI as 📱 UI & NavGraph
    participant VM as ⚙️ ViewModel
    participant NSD as 📡 NSD Manager
    participant WS as 🔌 WebSocket (NanoWSD)
    participant FS as 🔥 Firestore
    participant FCM as 🔔 FCM Service
    actor Bob as 👤 Bob (Receiver)

    alt Local LAN Mode (Zero Internet)
        Alice->>UI: Type message & tap Send
        UI->>VM: dispatchLocalMessage(text)
        VM->>NSD: Query registered endpoints (_plexus._tcp)
        NSD-->>VM: Resolve Bob's local IP & Port
        VM->>WS: Send JSON Frame payload over socket
        WS-->>Bob: Duplex WebSocket Frame received (<50ms)
        Bob->>Bob: Play audio & render neon bubble
    else Global Cloud Mode (WAN)
        Alice->>UI: Type message & tap Send
        UI->>VM: dispatchCloudMessage(text, chatId)
        VM->>FS: Append Document to /chats/{id}/messages
        FS-->>Bob: Real-time Snapshot fires on active screen
        opt If Bob is in Background / Phone Locked
            FS->>FCM: Trigger downstream Push Notification
            FCM-->>Bob: NotificationCompat.MessagingStyle with Quick Reply
        end
    end
```

---

## 🚀 Key Features

| Feature | Description | Engine |
| :--- | :--- | :--- |
| **🌐 Dual-Mode Switch** | Seamlessly toggle between Local LAN mode and Global Cloud mode without losing context. | Architecture |
| **📡 Zero-Config P2P** | Discovers nearby devices automatically over Wi-Fi/Hotspot using mDNS (DNS-SD). | Android NSD |
| **⚡ Microsecond Sockets** | Direct, low-latency device-to-device communications via embedded server. | NanoWSD + OkHttp |
| **💬 Smart User Discovery**| Find friends with `@username` indexing or E.164 phone number queries. | Firestore Index |
| **👥 Hybrid Group Chat** | Create and manage multi-device group conversations across both network modes. | StateFlow Engine |
| **🔔 WhatsApp-Grade Alerts** | `MessagingStyle` push notifications with inline direct quick replies (`RemoteInput`). | FCM Service |
| **🎨 Cyberpunk Neon UI** | 100% Jetpack Compose with custom neon gradients, fluid animations, and dark themes. | Material 3 |

---

## 🛠 Tech Stack & Libraries

```text
Language              ▶ Kotlin 2.0+ (100% Coroutines & Flow)
User Interface        ▶ Jetpack Compose (Material 3 BOM 2024), Compose Navigation
Reactive State        ▶ Unidirectional Data Flow (UDF), ViewModel, StateFlow
P2P Networking        ▶ Android NSD (Network Service Discovery), NanoWSD, OkHttp 4.12
Cloud Infrastructure  ▶ Firebase Auth (OTP), Cloud Firestore, Firebase Cloud Messaging
Packaging & Build     ▶ Gradle 8.13, AGP 8.x, Compile SDK 36 (Java 21 Adoption)
```

---

## 📂 Project Architecture

```
app/src/main/java/com/example/plexus/
├── 🌐 data/
│   ├── local/              # P2P: NSD discovery service, NanoWSD server, OkHttp client
│   └── model/              # Data schemas: UserModel, ChatModel, MessageModel, LocalMessageModel
├── 🧭 navigation/          # Compose NavHost, Screen sealed classes, and deep-link routes
├── 🎨 ui/
│   ├── components/         # Reusable Neon Buttons, Glow TextFields, Message Bubbles
│   ├── screens/            # Screens: Splash, Auth, OTP, Home, Chat, LocalDevices, Groups
│   └── theme/              # Cyberpunk palette (Neon Cyan, Electric Purple, Dark Surface)
├── 🛠 utils/               # Time formatters & PlexusMessagingService (MessagingStyle FCM)
└── 🧠 viewmodel/           # Business logic: AuthViewModel, ChatViewModel, LocalChatViewModel
```

---

## 💻 Quick Start & Build

### Prerequisites
- Android Studio Ladybug (2024.2+) or newer
- JDK 21 (Adoptium Temurin recommended)
- Android SDK 34+ (Target SDK 36, Min SDK 24)

### Clone & Compile
```bash
# Clone the repository
git clone https://github.com/varunjoshi84/Plexus.git

# Enter project root
cd Plexus

# Compile Debug APK
./gradlew assembleDebug
```

> Output APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

---

## 📥 Download Demo APK

<div align="center">

[![Download Plexus APK](https://img.shields.io/badge/Download-Plexus_APK-00E5FF?style=for-the-badge&logo=google-drive&logoColor=black)](https://drive.google.com/file/d/1wUeEcyu4OCz-8iWTlLKPyyDTbJiwaJr9/view?usp=drivesdk)

*Ready to install and test on physical devices.*

</div>

---

## 📄 License

Distributed under the **Apache License 2.0**. See `LICENSE` for more information.

<div align="center">
<sub>Crafted with ⚡ and Kotlin by Varun Joshi</sub>
</div>
