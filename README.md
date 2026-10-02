# PlantCare AI — Industrial Equipment Scan & Equipment 360°

**PlantCare AI** is a mobile maintenance intelligence application built for plant technicians and maintenance engineers in heavy industrial environments (crushing, screening, grinding, and material handling plants).

---

## 1. System Architecture & DataRepository Pattern

PlantCare AI employs a Clean Architecture and **DataRepository Pattern** that fully abstracts local **Room Database (SQLite)** access from the Jetpack Compose UI layer.

### Layer Diagram

```text
┌────────────────────────────────────────────────────────┐
│               Jetpack Compose UI Layer                 │
│  DashboardScreen  •  ScannerScreen  • Equipment360Screen│
└───────────────────────────▲────────────────────────────┘
                            │ (StateFlow / UI Events)
┌───────────────────────────┴────────────────────────────┐
│                  ViewModel Layer                       │
│              Equipment360ViewModel                     │
└───────────────────────────▲────────────────────────────┘
                            │ (Domain Flows & Coroutines)
┌───────────────────────────┴────────────────────────────┐
│             DataRepository Abstraction                 │
│        IEquipmentRepository (Contract Interface)       │
│        EquipmentRepository (Offline-First Impl)        │
└───────────────────────────▲────────────────────────────┘
                            │ (Dispatched on Dispatchers.IO)
┌───────────────────────────┴────────────────────────────┐
│                   Room Local Database                  │
│   AppDatabase  •  EquipmentDao  •  WorkOrderDao        │
│   MaintenanceNotificationDao    •  InspectionDao       │
│   MaintenancePlanDao            •  DowntimeDao         │
│   MaterialConsumptionDao        •  TechnicalDocDao     │
└────────────────────────────────────────────────────────┘
```

### Key Repository Principles

1. **Complete Database Decoupling**: The ViewModel and UI never interact with Room DAOs or SQLite statements directly. All access is mediated by `IEquipmentRepository`.
2. **Unified Aggregate Stream (`getEquipment360`)**: The repository combines multiple Room DAO reactive flows into a single cohesive `Equipment360Data` stream, ensuring atomic, consistent updates across all sections (indicators, work orders, notifications, downtime, and spare parts).
3. **Guaranteed Offline-First Availability**: 
   - All equipment specifications, procedures, spare parts, and work order histories are persisted in the local Room database (`plantcare_maintenance.db`).
   - The database is automatically pre-seeded with realistic plant data on first launch via `DemoDataSeeder`.
   - Technicians can scan, inspect, update status, and log notifications in remote plant tunnels or pits with **zero internet connection**.
4. **Future SAP PM/EAM Integration Ready**: The repository interface is designed so that a background sync engine can synchronize local mutations with SAP PM / Maximo REST APIs without requiring changes to the UI layer.

---

## 2. Core Workflow

```text
┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐     ┌─────────────────┐     ┌────────────────┐
│ 1. SCAN TAG  │ ──► │ 2. IDENTIFY ASSET│ ──► │ 3. EQUIPMENT 360°│ ──► │ 4. VIEW STATUS  │ ──► │ 5. TAKE ACTION │
│ Camera / QR  │     │ Parse ID & Lookup│     │ Master Dashboard │     │ WO, PM, History │     │ Notif / Inspect│
└──────────────┘     └──────────────────┘     └──────────────────┘     └─────────────────┘     └────────────────┘
```

1. **Scan**: The technician points the camera at an equipment tag or selects quick-scan in the emulator.
2. **Identify**: `QrCodeParser` extracts the clean equipment ID (e.g., `121SC008`).
3. **Lookup**: The local repository queries the Room database for the equipment record.
4. **Equipment 360°**: The full 360° asset maintenance view opens instantly.
5. **Take Action**: The technician creates a notification or completes an inspection — the Equipment ID is **automatically inherited** with zero manual re-entry.

---

## 3. QR Code & Barcode Parsing Specification

The scanner supports multiple industrial tagging formats:

| Format Type | Example Payload | Extracted Equipment ID |
| :--- | :--- | :--- |
| **PlantCare Structured** | `PLANTCARE:EQUIPMENT:121SC008` | `121SC008` |
| **Direct Serial / Raw** | `121SC008` | `121SC008` |
| **SAP EAM Tag** | `SAP:EAM:121SC008` | `121SC008` |
| **Asset Web URL** | `https://plantcare.ai/eq/121SC008` | `121SC008` |
| **JSON Payload** | `{"equipmentId": "121SC008"}` | `121SC008` |
| **Key-Value Tag** | `EQUIPMENT_ID=121SC008` | `121SC008` |

If a scanned code is not found in the database, the app presents an **Equipment not found** dialog with manual search and scan-again options without crashing.

---

## 4. Equipment 360° Features

* **Equipment Master Details**: ID, Name, Functional Location (`BI-PLN-CRU/CRS-003`), Area, Criticality (`CRITICAL`, `HIGH`), Work Center (`MECHANICAL`, `ELECTRICAL`), Manufacturer (`Metso Outotec`, `FLSmidth`, `Continental`), Model, Serial Number, and Commissioning Date.
* **Clickable Summary Indicators**:
  * `Open WO` → Navigates to Work Orders.
  * `Open Notifications` → Navigates to Notifications.
  * `Next PM` → Navigates to Preventive Maintenance.
  * `Downtime YTD` → Navigates to Downtime History.
* **Work Orders (SAP PM Lifecycle)**: Filter by Open, In Progress, Completed, or All. Supports standard SAP statuses: `CRTD` (Created), `REL` (Released), `PCNF` (Partially Confirmed), `CNF` (Confirmed), and `TECO` (Technically Completed).
* **Maintenance Notifications**: Create new notifications with priority, damage description, suspected cause, observations, photos, and voice note transcription.
* **Preventive Maintenance & Interactive Checklists**: View maintenance plans, task lists, frequencies, and complete PM walkdown checklists with sign-off.
* **Downtime Tracking**: Tracks start/end timestamps, duration, breakdown type, and calculated Total Downtime YTD.
* **Spare Parts History (SAP MM Ready)**: Part numbers, descriptions, quantities, units, and warehouse bin locations.
* **Technical Documents**: Access OEM manuals, general arrangement drawings, and safe work procedures.
* **AI Diagnostics Assistant**: Context-aware industrial assistant pre-loaded with asset history, answering questions on failure modes, work orders, and maintenance strategies.

---

## 5. AI Visual Inspection (Field Maintenance UX)

The **AI Visual Inspection** workflow is streamlined to 4 simple field actions:

```text
SCAN
 ↓
PHOTO
 ↓
AI ANALYSIS
 ↓
REVIEW & CONFIRM
```

1. **Auto Equipment Context**: Automatically inherits Equipment ID, Name, Functional Location, Area, Criticality, and Work Center from Equipment 360° with zero manual data entry.
2. **Simplified Camera**: Full-screen preview, shutter button, gallery picker, flashlight toggle, retake options, and quick sample field photos for emulator testing.
3. **Optional Observation**: One-tap speech-to-text recording (`🎤 Describe what you noticed`) or optional text note. Can be bypassed directly to analysis.
4. **Contextual AI Inference**: Blends photo, observations, open work orders, notifications, downtime history, and maintenance plans. Uses cautious language (`"Possible issue"`, `"AI observation"`) and flags safety items (`"⚠ Technician verification required"`).
5. **Single-Screen Actionable Result**: Shows the possible issue, confidence level, AI observation, recommended checks, and a primary button to create a notification.
6. **Human-in-the-Loop Validation**: Technician reviews and modifies all pre-filled fields before confirming creation.
7. **Immediate 360° Sync**: Creates the maintenance notification and adds an inspection log to the local database, immediately updating Equipment 360° counters.

---

## 6. UI/UX for Industrial Technicians

* **Glove-Friendly Touch Targets**: Minimum 48dp touch targets (`minimumInteractiveComponentSize`) across all buttons, chips, and cards.
* **High-Contrast Outdoor Readability**: Industrial palette with deep slate surfaces (`#0F172A`, `#1E293B`), safety amber accents (`#F59E0B`), and distinct color-coded status badges.
* **Haptic Feedback**: Vibrates on successful scan detection.
* **Camera Permission Handling**: Graceful runtime permission flow with manual code input fallback.

---

## 6. Maintenance Job Confirmation (Field Execution UX)

PlantCare AI includes a technician-first **Job Confirmation** module designed for 30–60 second field confirmations without exposing complex SAP GUI terminology:

```text
SELECT JOB
   ↓
REVIEW WORK
   ↓
ENTER ACTUAL EXECUTION
   ↓
REVIEW
   ↓
CONFIRM
```

### Key Workflow Capabilities

1. **Activities / "MY WORK" Screen**:
   - Filter sections: `Today`, `Upcoming`, `In Progress`, `Waiting`, `Completed`.
   - Native mobile cards replacing dense desktop tables.
   - Shows WO number, status badge (`REL`, `PCNF`, `CNF`), description, equipment ID, functional location, work center, and planned hours (`Mechanical • 6.0 h`).
   - One-tap `[ START JOB ]` and `[ CONFIRM WORK ]` actions.

2. **Persistent Active Job Experience**:
   - Starting a job automatically captures **Actual Start Date & Start Time** (e.g., `14:32`).
   - Displays a persistent active card banner across the app with live elapsed timer (`CURRENT JOB: WO 155704 • Elapsed 01:24`).

3. **Confirm Work — 4 Simple Sections**:
   - **1. Job Context (Read-Only)**: WO, Operation (`0010`), Equipment, Functional Location (`BI-PLN-CRU/CRS-003`), Work Center (`MECHANICAL`).
   - **2. Time**:
     - Work Start (e.g., `14:32`) & Work Finish (e.g., `16:05`).
     - **Calculated Elapsed Time** (e.g., `1 h 33 min`).
   - **3. Actual Work**:
     - Large numeric input (`[ 1.5 ] [ H ]`) with H / MIN unit toggle.
     - Helper hint: `Suggested from elapsed time: 1.55 h`.
     - **Independent Time Concept**: Work Finish - Work Start ≠ Actual Work. Active wrench time (e.g., 3.0 h) is strictly distinguished from access window / elapsed duration (e.g., 4.0 h) and is never silently overwritten.
     - **Team Labor Architecture**: Supports multi-technician jobs (e.g., primary tech + additional co-workers with allocated hours and total labor calculation).
   - **4. Work Performed & Field Evidence**:
     - Quick checkboxes: `Inspection completed`, `Lubrication completed`, `Adjustment performed`, `Component replaced`, `Cleaning performed`.
     - `Add work note` with 🎤 voice-to-text dictation simulation.
     - Contextual measurements: Bearing Temperature (`68 °C`), Vibration (`4.2 mm/s`), Bearing Clearance (`0.08 mm`).
     - Materials Used: Spare parts consumption (e.g., `Bearing 6208 Qty 2`, `Grease EP2 Qty 0.5 KG`) with `[ + Add Material ]`.
     - Photo evidence: Before, During, After photo attachments.
   - **5. Completion**:
     - Simple question: `IS THE JOB COMPLETE?`
     - `YES — Work Completed` (maps to Final confirmation in SAP PM, updating WO status to `CNF`).
     - `NO — More Work Required` (maps to Partial confirmation, with quick reason chips: *Waiting for spare parts, Additional work identified, Equipment unavailable, Specialist required, Tools unavailable, Shift ended*).
     - Follow-up work option: `Create Notification` or `Add Observation`, inheriting asset context.

4. **Compact Pre-Submission Review & Success Screens**:
   - Summary view with explicit `[ EDIT ]` and `[ CONFIRM WORK ]` actions.
   - Success screen confirming WO, Operation, Actual Work, and timestamp with quick navigation to `[ VIEW WORK ORDER ]` or `[ NEXT JOB ]`.

5. **Clean API Architecture & Offline Sync Queue**:
   ```text
   Compose UI ──► ConfirmationViewModel ──► ConfirmationRepository ──► Room DB (Local)
                                                                            │
                                                       (Sync Queue) ────────┘
                                                            ▼
                                                 PlantCare API Contract (POST /api/v1/confirmations)
                                                            ▼
                                                       SAP PM / EAM
   ```
   - **Idempotency Protection**: Uses device-generated `clientConfirmationId` (UUID) to prevent accidental duplicate confirmations.
   - **Sync States**: `DRAFT`, `PENDING_SYNC` (*"Saved on device • Waiting to sync"*), `SYNCING`, `SYNCED`, `SYNC_FAILED` (*"Sync issue • Your confirmation is safely stored on this device [ Retry ]"*).
   - **Confirmation History**: Displayed on Equipment 360° and Work Order Detail screens.

---

## 7. Pre-seeded Industrial Equipment

* `121SC008` — Vibrating Grizzly Feeder Screen (Scalping) — Includes `WO 155704` (REL, Planned 6.0 h)
* `CRU01` — Superior MKIII Gyratory Primary Crusher
* `AF01` — Heavy Duty Apron Feeder
* `CV04` — Overland Belt Conveyor 1200mm
* `CV05C` — Reclaim Conveyor EP-800
* `CV06A` — Mill Feed Conveyor A with Tripper
* `CV06B` — Mill Feed Conveyor B

---

## 8. Testing & Verification

Unit and local JVM tests executed with **Robolectric**:

* `ConfirmationValidationTest`: Validates required fields, work order presence, operation format, and non-negative actual work.
* `ElapsedTimeCalculationTest`: Validates elapsed calculations (14:32 → 16:05 = 93 min, 08:00 → 12:00 = 240 min).
* `ActualWorkIndependenceTest`: Validates that `Work Start = 08:00, Work Finish = 12:00 (Elapsed = 4.0h), Actual Work = 3.0h` is accepted and preserved without silent overwrite.
* `OfflineConfirmationTest`: Validates that offline submissions succeed locally, store with `PENDING_SYNC`, update WO to `CNF`, and clear active jobs.
* `ConfirmationSyncTest`: Validates queued record synchronization when connectivity is restored, updating status to `SYNCED` with remote SAP ID.
* `DuplicateConfirmationTest`: Validates idempotency protection using `clientConfirmationId`.
* `JobConfirmationEndToEndTest`: Validates full Critical User Journey (CUJ) online and offline.
* `QrCodeParserTest`: Validates parsing of structured tags, URLs, JSON, and raw barcodes.
* `EquipmentLookupTest`: Validates Room in-memory database queries and `getEquipment360` aggregation.
* `VisualInspectionTest`: Validates AI visual inspection pipeline and fault inference.

Run tests:
```bash
gradle :app:testDebugUnitTest
```

