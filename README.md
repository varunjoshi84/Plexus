# Plexus Chat Application

Plexus is a next-generation hybrid messaging application designed to keep people connected regardless of internet availability. It seamlessly switches between **Local Network Mode** (using NSD and WebSockets) and **Internet Mode** (using Firebase) to ensure your messages always reach their destination.

## 🚀 Key Features

- **Hybrid Messaging**: Chat over a local Wi-Fi network without internet or switch to Global mode for internet-based messaging.
- **Smart Search**: Find friends by their unique `@username` or their registered `phone number`.
- **Hybrid Group Chats**: Create and manage group conversations that work across both local and internet modes.
- **WhatsApp-Style Notifications**: Robust notification system using `MessagingStyle` for grouped conversations and quick replies.
- **Secure Authentication**: Firebase Phone OTP-based login with profile management.
- **Sleek UI/UX**: Built entirely with Jetpack Compose featuring a modern "Cyberpunk/Neon" theme.

## 🛠 Tech Stack

- **UI Framework**: Jetpack Compose (Material 3)
- **Language**: Kotlin
- **Backend/Database**: Firebase (Authentication, Firestore, Cloud Messaging)
- **Local Networking**: 
    - **NSD (Network Service Discovery)**: For discovering nearby devices on the same Wi-Fi.
    - **WebSockets (NanoWSD)**: For real-time local peer-to-peer communication.
- **Architecture**: MVVM (Model-View-ViewModel) with StateFlow
- **Navigation**: Compose Navigation

## 📂 Folder Structure

```text
app/src/main/java/com/example/plexus/
├── data/
│   ├── local/          # NSD & WebSocket implementation
│   └── model/          # Firestore & Local Data Models
├── navigation/         # NavGraph & Route definitions
├── ui/
│   ├── components/     # Reusable UI elements
│   ├── screens/        # All application screens (Home, Chat, Profile, etc.)
│   └── theme/          # Custom Plexus Theme, Colors, and Gradients
├── utils/              # FCM Service and Time formatters
└── viewmodel/          # Business logic & State management
```

## 📥 Download APK

You can download the latest version of Plexus here:
[Download Plexus APK (Google Drive Link)](https://drive.google.com/file/d/1wUeEcyu4OCz-8iWTlLKPyyDTbJiwaJr9/view?usp=drivesdk)

---
