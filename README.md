# Plexus — Hybrid Offline & Cloud Messaging System

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**Plexus** is an advanced, resilient Android messaging application engineered to maintain uninterrupted real-time communication regardless of internet connectivity. It seamlessly transitions between **Local Peer-to-Peer (P2P) Ad-Hoc Mesh / LAN Mode** and **Global Cloud Mode**, ensuring high availability and zero-configuration communication under any network constraint.

---

## 🌟 Key Highlights

- **Dual-Mode Communication Engine**:
  - **Local LAN / Offline P2P Mode**: Discover nearby devices and exchange messages over local Wi-Fi without active WAN/internet access via **Network Service Discovery (NSD / mDNS)** and embedded duplex **WebSockets (NanoWSD + OkHttp)**.
  - **Global Cloud Mode**: End-to-end cloud messaging powered by **Cloud Firestore** real-time listeners and **Firebase Cloud Messaging (FCM)**.
- **Smart User & Group Discovery**:
  - Dynamic user discovery via `@username` indexing and E.164 phone number queries.
  - Hybrid 1:1 and multi-participant Group Chats functioning over both local broadcast and cloud sync.
- **Interactive Rich Notifications**:
  - Android `NotificationCompat.MessagingStyle` integration with grouped conversation threads, direct inline quick replies (`RemoteInput`), and profile avatars.
- **Robust Authentication & Onboarding**:
  - Seamless phone OTP authentication via Firebase Auth with dynamic user profile setup and username reservation.
- **Modern Cyberpunk Neon UI**:
  - 100% declarative UI built with **Jetpack Compose (Material 3)**, reactive state flows, custom neon gradients, and fluid dark-mode transitions.

---

## 🏗 Architecture & Engineering

The application follows Google's recommended modern Android architecture: **Clean MVVM (Model-View-ViewModel)** with unidirectional data flow (UDF) powered by Kotlin Coroutines and reactive `StateFlow`.

```
┌────────────────────────────────────────────────────────┐
│             Jetpack Compose UI (Material 3)            │
│   (Screens, Custom Theming, Components, NavGraph)      │
└───────────────────────────▲────────────────────────────┘
                            │ (StateFlow / UDF)
┌───────────────────────────┴────────────────────────────┐
│                    ViewModel Layer                     │
│    (ChatViewModel, LocalChatViewModel, AuthViewModel)  │
└───────────────────────────▲────────────────────────────┘
                            │
              ┌─────────────┴─────────────┐
              ▼                           ▼
   [ Local Network Engine ]     [ Cloud Sync Engine ]
   • NSD / mDNS Discovery       • Cloud Firestore Listeners
   • Embedded NanoWSD Server    • Firebase Auth (Phone OTP)
   • OkHttp WebSocket Client    • FCM Push Notifications
```

---

## 🛠 Tech Stack & Libraries

| Domain | Technology / Library |
| :--- | :--- |
| **Language** | Kotlin 2.0+ (100%) |
| **UI Framework** | Jetpack Compose (BOM 2024), Material 3, Compose Navigation |
| **Concurrency & Async** | Kotlin Coroutines, Flow / StateFlow |
| **Local P2P Networking** | Android Network Service Discovery (NSD/DNS-SD), NanoWSD (`org.nanohttpd:nanohttpd-websocket`), OkHttp 4.12 WebSocket Client |
| **Cloud Backend** | Firebase Auth (Phone OTP), Cloud Firestore (Real-time snapshots), Firebase Cloud Messaging (FCM) |
| **Architecture** | MVVM, Unidirectional Data Flow (UDF), Android Jetpack Lifecycle |

---

## 📂 Project Structure

```text
app/src/main/java/com/example/plexus/
├── data/
│   ├── local/              # Network Service Discovery (NSD) manager & NanoWSD / OkHttp socket handlers
│   └── model/              # Domain & DTO models (User, Message, Chat, LocalMessage)
├── navigation/             # Navigation host, screen routes, and argument handling
├── ui/
│   ├── screens/            # Jetpack Compose screens (Home, Chat, LocalDevices, Group, Auth, etc.)
│   └── theme/              # Cyberpunk / Neon design system, custom typography, and color palettes
├── utils/                  # Background services (FCM MessagingService with MessagingStyle & Quick Reply)
└── viewmodel/              # State management & business logic (Auth, Cloud Chat, and P2P Local Chat)
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug / Meerkat or newer
- Android SDK 34+ (Compile SDK 36, Min SDK 24)
- JDK 11+
- Physical Android device (recommended for local Wi-Fi testing) or Android Emulator

### Installation & Build

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/plexus.git
   cd plexus
   ```

2. **Configure Firebase:**
   - Create a project on [Firebase Console](https://console.firebase.google.com/).
   - Enable **Phone Authentication**, **Cloud Firestore**, and **Firebase Cloud Messaging**.
   - Download `google-services.json` and place it in the `app/` directory.

3. **Build & Run:**
   ```bash
   ./gradlew assembleDebug
   ```

---

## 📥 Download Demo APK

Try out the pre-built debug APK directly:
- [Download Plexus APK (Google Drive)](https://drive.google.com/file/d/1wUeEcyu4OCz-8iWTlLKPyyDTbJiwaJr9/view?usp=drivesdk)

---

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
