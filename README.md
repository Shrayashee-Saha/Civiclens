# 🇧🇩 CivicLens — Public Infrastructure Transparency, Civic Communities & Digital Governance Platform

[![Android SDK](https://img.shields.io/badge/Android%20SDK-API%2024%20to%2035%2B-10B981.svg)](https://developer.android.com)
[![Java JDK](https://img.shields.io/badge/Language-Java%2017-0F172A.svg)](https://www.oracle.com/java/)
[![Firebase Services](https://img.shields.io/badge/Firebase-Auth%20%7C%20RTDB%20%7C%20Firestore%20%7C%20FCM-FFCA28.svg)](https://firebase.google.com/)
[![WebRTC Video Calling](https://img.shields.io/badge/RealTime%20Calling-Agora%20RTC%20SDK%204.5.0-00A0E9.svg)](https://www.agora.io/)
[![Unit Tests](https://img.shields.io/badge/Unit%20Tests-30%20Passed%20%28100%25%29-059669.svg)](https://github.com/Shrayashee-Saha/Civiclense)

**CivicLens** is an advanced native Android platform engineered to promote public infrastructure transparency, civic participation, RTI legal empowerment, and community assembly in Bangladesh.

---

## 🌟 Core Features & Modules

### 1. 🏛️ Government Services Directory & Document Readiness Inspector
- **Verified Service Checklists**: Fees, timelines, and document requirements for 12+ essential services (*NID, Birth Registration, Land Mutation, Trade License, DESCO Smart Meters*).
- **📸 AI Document Readiness Inspector**: Scans photo evidence of applicant documents to verify mandatory requirements.

### 2. 🤖 CivicLens AI Assistant & Speech-to-Text Voice Input
- Integrated **Google Gemini Pro AI Assistant** with Bangla/English Speech-to-Text input (`RecognizerIntent.ACTION_RECOGNIZE_SPEECH`).
- Text-to-Speech (TTS) voice guidance for blind and visually impaired citizens.

### 3. 📄 RTI Legal Application Assistant & Official PDF Exporter
- Automated legal **Form 'Ka' (ফরম 'ক')** draft generation under the **Right to Information Act 2009**.
- **Official PDF Layout Engine**: Generates legal PDF documents with Navy header banners, border frames, and applicant signature boxes, saved directly to `/storage/emulated/0/Download/`.

### 4. 📊 Infrastructure Reality Index & Daily Taxpayer Delay Loss Calculator
- **Reality Index Score (0-100)**: Evaluates milestone gaps, CAG audit flags, and ground observations.
- **Daily Overrun Loss Calculator**: Calculates delay schedule gaps (Planned vs Actual) and daily taxpayer economic loss in **BDT Lakh/Crore**.

### 5. 👥 Civic Feed & Civic Communities Advocacy Network
- **Civic Feed**: Supports 5 post types (*Text, Photo Evidence, Video Clues, News Probes, Project Updates*) with persistent Support/Like counts and interactive Comments Thread dialogs.
- **Civic Communities**: Create and join public/private advocacy groups (*Save the Sundarbans, Protect Khulna Wetlands, Safe Roads Gazipur*) saved permanently in Room SQLite and Firebase Cloud.

### 6. 📞 Real-Time WebRTC / Agora Voice & Video Calling Hub
- Full-screen hardware-accelerated 2-way Voice and Video Conference Calling (`io.agora.rtc:full-sdk:4.5.0`).
- **Instant Call Notification Banner**: Dispatches high-priority **`📞 INCOMING ASSEMBLY CALL`** status bar banners with **`✓ JOIN CALL`** action buttons.

### 7. 🎮 "Civic Integrity Quest" Kids Mini-Game (নৈতিক শিক্ষা)
- Interactive 2-scenario decision mini-game for children and young citizens teaching civic values, honesty, and accountability.
- Real-time **Integrity Score Tracker (0-500 pts)** awarding the **`⭐ Junior Civic Integrity Ambassador Badge`** (Total size <50 KB).

### 8. 🚨 Emergency Helplines & Navigation Guard
- **Home Quick Dial Chips**: One-tap emergency dialer chips (*`📞 999 Emergency`*, *`📞 333 Civic Helpline`*, *`📞 106 Anti-Corruption`*).
- **Task-Root Navigation Guard**: Prevents accidental app exits when pressing the back button.

---

## 🛠️ Technology Stack & Architecture

```
                       ┌──────────────────────────────────────────┐
                       │          CivicLens Mobile App            │
                       └────────────────────┬─────────────────────┘
                                            │
           ┌────────────────────────────────┼────────────────────────────────┐
           ▼                                ▼                                ▼
┌──────────────────────┐        ┌──────────────────────┐        ┌──────────────────────┐
│  Local Offline DB    │        │  Cloud Sync Engine   │        │  Media & Voice Engine│
│   (Room SQLite v11)  │        │ (Firebase RTDB & FS) │        │ (Agora RTC & WebRTC) │
└──────────────────────┘        └──────────────────────┘        └──────────────────────┘
```

| Layer | Technology | Purpose |
| :--- | :--- | :--- |
| **Language** | Java (JDK 17) & Android SDK | Production native Android codebase |
| **Local Database** | Room SQLite 2.6.1 (Version 11) | Offline-first caching for services, communities, posts, and reports |
| **Cloud Sync** | Firebase Realtime Database & Firestore | Cross-device real-time sync for invitations, communities, and chat |
| **Cloud Messaging**| Firebase Cloud Messaging (FCM) | Topic & token push notification banners (`all_citizens`, `invitations`) |
| **Realtime Calling**| Agora RTC SDK 4.5.0 | Multi-participant WebRTC voice & video conferencing |
| **AI Processing** | Google Gemini Pro API | Natural language civic Q&A and prompt engineering |
| **PDF Generation**| Android `PdfDocument` Graphics API | Official legal PDF document generator |

---

## 🚀 Java 17 Cloud Functions Deployment (`gcloud` CLI)

All backend Cloud Functions are written in **Java 17 (Maven `pom.xml`, Firebase Admin SDK for Java)** without any JavaScript dependencies.

```bash
cd functions-java

# 1. Deploy create-group
gcloud functions deploy create-group \
  --gen2 --region=asia-southeast1 --runtime=java17 \
  --entry-point=com.example.civiclens.functions.CreateGroupFunction \
  --trigger-http --allow-unauthenticated

# 2. Deploy find-user-by-email
gcloud functions deploy find-user-by-email \
  --gen2 --region=asia-southeast1 --runtime=java17 \
  --entry-point=com.example.civiclens.functions.FindUserByEmailFunction \
  --trigger-http --allow-unauthenticated

# 3. Deploy invite-to-group
gcloud functions deploy invite-to-group \
  --gen2 --region=asia-southeast1 --runtime=java17 \
  --entry-point=com.example.civiclens.functions.InviteToGroupFunction \
  --trigger-http --allow-unauthenticated

# 4. Deploy respond-to-invitation
gcloud functions deploy respond-to-invitation \
  --gen2 --region=asia-southeast1 --runtime=java17 \
  --entry-point=com.example.civiclens.functions.RespondToInvitationFunction \
  --trigger-http --allow-unauthenticated

# 5. Deploy notify-invitation-created (Firestore Trigger)
gcloud functions deploy notify-invitation-created \
  --gen2 --region=asia-southeast1 --runtime=java17 \
  --entry-point=com.example.civiclens.functions.NotifyInvitationCreatedFunction \
  --trigger-event-filters=type=google.cloud.firestore.document.v1.created \
  --trigger-event-filters=database='(default)' \
  --trigger-event-filters=document='invitations/{invitationId}'
```

---

## 🧪 Testing & Verification Results

```bash
./gradlew testDebugUnitTest
```

- **Unit Test Suite**: **30 Passed, 0 Skipped, 0 Failed (100% Green)**.
- **Build Status**: `assembleDebug` completed **BUILD SUCCESSFUL**.
- **Device Compatibility**: Verified on physical Android phones (Samsung OneUI, Xiaomi MIUI, Vivo, Pixel) and standard Android Studio emulators (API Level 24 to 35+).

---

## 📄 License & Repository

- **Repository**: [Shrayashee-Saha/Civiclense](https://github.com/Shrayashee-Saha/Civiclense.git)
- **Developed for**: Bangladesh Public Infrastructure Transparency, Environmental Protection & Digital Governance.
