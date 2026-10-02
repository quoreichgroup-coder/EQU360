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

## 6. Pre-seeded Industrial Equipment

* `121SC008` — Vibrating Grizzly Feeder Screen (Scalping)
* `CRU01` — Superior MKIII Gyratory Primary Crusher
* `AF01` — Heavy Duty Apron Feeder
* `CV04` — Overland Belt Conveyor 1200mm
* `CV05C` — Reclaim Conveyor EP-800
* `CV06A` — Mill Feed Conveyor A with Tripper
* `CV06B` — Mill Feed Conveyor B

---

## 7. Testing & Verification

Unit and local JVM tests using **Robolectric**:

* `QrCodeParserTest`: Validates parsing of structured tags, URLs, JSON, and raw barcodes.
* `EquipmentLookupTest`: Validates Room in-memory database queries, `getEquipment360` aggregation, work order status transitions, and notification insertions.

Run tests:
```bash
gradle :app:testDebugUnitTest
```
