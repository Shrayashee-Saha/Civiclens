# CivicLens BD: Milestone 1 with project transparency

Native Android app in Java with XML layouts and Material Design components.

## Screens

- **Home:** plain-language service search with synonyms ("lost" matches "duplicate").
- **Service details:** fees, processing notes, verification date, portal link, Save, and a
  document checklist with saved progress and a readiness percentage.
- **Offices:** search, district filter, authorized office vs help-centre label, directions,
  call, and bookmarks.
- **Projects:** browse by district and category. Every record is FICTIONAL demonstration data.
  The detail screen shows the planned milestones, an explanation for each schedule status, a
  progress summary, citizen observations saved on this device, and an audit trail.
- **My Services:** saved services and offices.
- **Bangla:** `values-bn` translation. The app follows the device language.

## How the transparency checks work

`domain/ProjectAnalyzer.java` compares each planned date with the completion record and today's date:

- Completed on or before the plan: On time.
- Completed after the plan: Completed late, with the number of days.
- Plan date passed, no completion recorded: Past planned date, with the number of days.
- Within 30 days of the plan with no completion: Due soon.
- Otherwise: Upcoming.

Every result includes its explanation, and every explanation says a schedule difference is not a
finding of wrongdoing. The rules are tested in `ProjectAnalyzerTest`.

## Limits in this version

- Citizen observations are saved on the device only. They have no reviewer approval, and the app
  does not claim they reached any authority.
- Reviewer approval, role-based access, and shared storage need Firebase Authentication and
  Firestore Security Rules. Those require a Firebase project and credentials that only you can
  create, so they aren't included.
- Photo attachments are not included yet.
- Schedule explanations are written in English. Only the labels are translated.
- Demo dates are fixed, so the "past planned date" results will change as the device date changes.

## Open and run

1. Open this folder in Android Studio (Hedgehog or newer) with **File > Open**.
2. Copy `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar` from a new Android
   Studio project into this root folder. They are not in the zip.
3. Sync Gradle, then run on an emulator or device (API 24 or newer).

## Tests

    ./gradlew test

Date arithmetic, schedule evaluation, progress, filtering, and ranking all run on the JVM.

## Package layout

    model/    Room entities
    data/     DAOs, CivicDatabase (version 3), repositories, demo data
    domain/   SearchRanker, OfficeFinder, DocumentReadiness, DateMath, ProjectAnalyzer
    ui/       MainActivity (4 tabs), Home/Offices/Projects/MyServices fragments,
              detail activities, adapters, IntentHelper
