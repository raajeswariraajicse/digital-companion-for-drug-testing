# Android app (Java): planned, not built yet

Planned structure (Android Studio, Java, minSdk 26):
- `MainActivity`: Start New Test (consent + medicines)
- `BleReader`: connects to "TraceRx-Reader", reads the "R,G,B" notification (UUIDs in the firmware file)
- `ApiClient`: Retrofit calls to `POST /api/tests` and `GET /api/chain/verify`
- `ResultActivity`: confidence score, medicine alert, Save Evidence, PDF report (OpenPDF)
- Room (SQLite) for offline storage, synced to the backend later
- Screen designs: see the mockups in `docs/`
