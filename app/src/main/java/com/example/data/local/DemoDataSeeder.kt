package com.example.data.local

import com.example.data.model.DowntimeEventEntity
import com.example.data.model.EquipmentEntity
import com.example.data.model.InspectionEntity
import com.example.data.model.MaintenanceNotificationEntity
import com.example.data.model.MaintenancePlanEntity
import com.example.data.model.MaterialConsumptionEntity
import com.example.data.model.TechnicalDocumentEntity
import com.example.data.model.WorkOrderEntity

object DemoDataSeeder {

    val equipments = listOf(
        EquipmentEntity(
            equipmentId = "121SC008",
            name = "VIBRATING GRIZZLY 121SC008",
            description = "Heavy-duty double-deck vibrating grizzly feeder screen for primary run-of-mine ore scalping.",
            functionalLocation = "BI-PLN-CRU/CRS-003",
            site = "Bauxite Mine 1 - Processing Plant",
            area = "Primary Crushing & Scalping Area",
            workCenter = "MECHANICAL",
            equipmentType = "Vibrating Screen / Grizzly Feeder",
            manufacturer = "Metso Outotec",
            model = "Nordberg VF650",
            serialNumber = "SN-2023-VG-8812",
            criticality = "HIGH",
            status = "ACTIVE",
            installationDate = "15 Jan 2021",
            qrCodePayload = "PLANTCARE:EQUIPMENT:121SC008"
        ),
        EquipmentEntity(
            equipmentId = "CRU01",
            name = "PRIMARY CRUSHER",
            description = "Gyratory crusher for primary ore size reduction from mining pit face.",
            functionalLocation = "BI-PLN-CRU/PRI-001",
            site = "Bauxite Mine 1 - Processing Plant",
            area = "Primary Crushing Pit",
            workCenter = "MECHANICAL",
            equipmentType = "Superior MKIII Gyratory Crusher",
            manufacturer = "FLSmidth",
            model = "TS-60-110",
            serialNumber = "FLS-MK3-44910",
            criticality = "CRITICAL",
            status = "ACTIVE",
            installationDate = "10 May 2019",
            qrCodePayload = "PLANTCARE:EQUIPMENT:CRU01"
        ),
        EquipmentEntity(
            equipmentId = "AF01",
            name = "PRIMARY APRON FEEDER",
            description = "Heavy duty apron feeder receiving dumped dump-truck ore and feeding primary grizzly.",
            functionalLocation = "BI-PLN-MAT/FED-002",
            site = "Bauxite Mine 1 - Processing Plant",
            area = "Dump Pocket & Primary Feed",
            workCenter = "MECHANICAL",
            equipmentType = "Heavy Duty Apron Feeder",
            manufacturer = "Thyssenkrupp Industrial Solutions",
            model = "AF-2400-HD",
            serialNumber = "TK-FED-99231",
            criticality = "HIGH",
            status = "ACTIVE",
            installationDate = "22 Aug 2020",
            qrCodePayload = "PLANTCARE:EQUIPMENT:AF01"
        ),
        EquipmentEntity(
            equipmentId = "CV04",
            name = "CONVEYOR CV04",
            description = "Overland belt conveyor transferring crushed ore to secondary surge bin stockpile.",
            functionalLocation = "BI-PLN-CVY/CV04-001",
            site = "Bauxite Mine 1 - Processing Plant",
            area = "Stockpile Transfer Corridor",
            workCenter = "MECHANICAL",
            equipmentType = "Trough Belt Conveyor 1200mm",
            manufacturer = "Continental AG",
            model = "Steelcord ST-1600",
            serialNumber = "CT-CV04-5512",
            criticality = "HIGH",
            status = "ACTIVE",
            installationDate = "05 Mar 2021",
            qrCodePayload = "PLANTCARE:EQUIPMENT:CV04"
        ),
        EquipmentEntity(
            equipmentId = "CV05C",
            name = "CONVEYOR CV05C",
            description = "Secondary reclaim conveyor connecting emergency discharge chute to mill feed silo.",
            functionalLocation = "BI-PLN-CVY/CV05C-003",
            site = "Bauxite Mine 1 - Processing Plant",
            area = "Surge Storage & Silo Feed",
            workCenter = "ELECTRICAL",
            equipmentType = "EP-800 4-Ply Belt Conveyor",
            manufacturer = "Bridgestone Conveyor Systems",
            model = "BCS-1000-HD",
            serialNumber = "BS-CV05C-1099",
            criticality = "MEDIUM",
            status = "ACTIVE",
            installationDate = "18 Nov 2021",
            qrCodePayload = "PLANTCARE:EQUIPMENT:CV05C"
        ),
        EquipmentEntity(
            equipmentId = "CV06A",
            name = "CONVEYOR CV06A",
            description = "Main mill feed conveyor A with weightometer and metal detector system.",
            functionalLocation = "BI-PLN-CVY/CV06A-001",
            site = "Bauxite Mine 1 - Processing Plant",
            area = "Grinding Plant Feed",
            workCenter = "AUTOMATION",
            equipmentType = "Trough Belt Conveyor with VFD Drive",
            manufacturer = "Continental AG",
            model = "Steelcord ST-2000",
            serialNumber = "CT-CV06A-7821",
            criticality = "HIGH",
            status = "ACTIVE",
            installationDate = "12 Feb 2022",
            qrCodePayload = "PLANTCARE:EQUIPMENT:CV06A"
        ),
        EquipmentEntity(
            equipmentId = "CV06B",
            name = "CONVEYOR CV06B",
            description = "Main mill feed conveyor B (redundant line) with reversible tripper carriage.",
            functionalLocation = "BI-PLN-CVY/CV06B-002",
            site = "Bauxite Mine 1 - Processing Plant",
            area = "Grinding Plant Feed",
            workCenter = "MECHANICAL",
            equipmentType = "Trough Belt Conveyor with Tripper",
            manufacturer = "Continental AG",
            model = "Steelcord ST-2000",
            serialNumber = "CT-CV06B-7822",
            criticality = "HIGH",
            status = "ACTIVE",
            installationDate = "14 Feb 2022",
            qrCodePayload = "PLANTCARE:EQUIPMENT:CV06B"
        )
    )

    val workOrders = listOf(
        WorkOrderEntity(
            orderNumber = "WO 155704",
            equipmentId = "121SC008",
            description = "Vibrating Grizzly Inspection",
            orderType = "PM02",
            priority = "HIGH",
            status = "REL",
            plannedDate = "2026-10-02",
            workCenter = "MECHANICAL",
            assignedTechnician = "A. Sawadogo",
            longText = "Perform mechanical inspection on vibrating grizzly 121SC008. Verify screen deck fasteners, exciter bearings temperature, clearance tolerances, and check lubrication (Planned 6.0 h)."
        ),
        // 121SC008 Work Orders
        WorkOrderEntity(
            orderNumber = "WO-4001928",
            equipmentId = "121SC008",
            description = "Replace drive motor eccentric counterweights and inspect cardan shaft",
            orderType = "PM01",
            priority = "HIGH",
            status = "REL",
            plannedDate = "2026-10-04",
            workCenter = "MECHANICAL",
            assignedTechnician = "Marcus Vance",
            longText = "Excessive axial play observed on drive-end eccentric pod. Isolate drive, lock out, and disassemble protective housing to inspect taper lock bushing."
        ),
        WorkOrderEntity(
            orderNumber = "WO-4001844",
            equipmentId = "121SC008",
            description = "Monthly vibration analysis & dynamic balance validation",
            orderType = "PM02",
            priority = "MEDIUM",
            status = "PCNF",
            plannedDate = "2026-10-02",
            workCenter = "PREDICTIVE",
            assignedTechnician = "Elena Rostova",
            longText = "Connect tri-axial accelerometer probes to non-drive and drive end bearing housings. Record baseline spectrum 10Hz-2kHz."
        ),
        WorkOrderEntity(
            orderNumber = "WO-4001710",
            equipmentId = "121SC008",
            description = "Grizzly bar deck liner replacement (Step 1 to 4)",
            orderType = "PM01",
            priority = "VERY HIGH",
            status = "CRTD",
            plannedDate = "2026-10-08",
            workCenter = "MECHANICAL",
            assignedTechnician = "David Chen",
            longText = "Wear measurements indicate deck bars 2 and 3 have worn below 35mm minimum thickness. Crane assist required."
        ),
        WorkOrderEntity(
            orderNumber = "WO-3998120",
            equipmentId = "121SC008",
            description = "Emergency repair of cracked discharge lip weldment",
            orderType = "PM04",
            priority = "VERY HIGH",
            status = "TECO",
            plannedDate = "2026-09-14",
            workCenter = "BOILERMAKER",
            assignedTechnician = "J. Kowalski",
            longText = "Gouged crack line, preheated to 150°C, welded with E7018 low-hydrogen electrodes and MPI inspected. Completed successfully."
        ),
        WorkOrderEntity(
            orderNumber = "WO-3995011",
            equipmentId = "121SC008",
            description = "Exciter gearbox oil drain and flush",
            orderType = "PM02",
            priority = "MEDIUM",
            status = "CNF",
            plannedDate = "2026-08-28",
            workCenter = "LUBRICATION",
            assignedTechnician = "K. Mwangi",
            longText = "Drained 42L Mobil SHC 630. Flushed housing and replaced breathers with desiccant air filters."
        ),

        // CV04 Work Orders
        WorkOrderEntity(
            orderNumber = "WO-4002010",
            equipmentId = "CV04",
            description = "Splice vulcanization inspection at marker #12",
            orderType = "PM02",
            priority = "HIGH",
            status = "REL",
            plannedDate = "2026-10-05",
            workCenter = "MECHANICAL",
            assignedTechnician = "Samir Patel",
            longText = "Inspect cold bond splice joint for edge fraying and cord pull-out."
        ),
        WorkOrderEntity(
            orderNumber = "WO-4001955",
            equipmentId = "CV04",
            description = "Replace seized return idler roller at station 44",
            orderType = "PM01",
            priority = "MEDIUM",
            status = "CRTD",
            plannedDate = "2026-10-03",
            workCenter = "MECHANICAL",
            assignedTechnician = "Marcus Vance",
            longText = "Idler roller shell worn through; squealing noise reported during night shift."
        ),

        // CRU01 Work Orders
        WorkOrderEntity(
            orderNumber = "WO-4001600",
            equipmentId = "CRU01",
            description = "Mantle and concave liner wear measurement laser scan",
            orderType = "PM03",
            priority = "HIGH",
            status = "PCNF",
            plannedDate = "2026-10-03",
            workCenter = "PREDICTIVE",
            assignedTechnician = "Elena Rostova",
            longText = "Deploy 3D laser profiler down the crushing cavity during planned lunch lull."
        )
    )

    val notifications = listOf(
        // 121SC008 Notifications
        MaintenanceNotificationEntity(
            notificationNumber = "NOTIF-100234",
            equipmentId = "121SC008",
            description = "High vibration spike on drive-side exciter bearing during rock surge",
            priority = "HIGH",
            status = "OPEN",
            creationDate = "2026-10-01 14:32",
            reportedBy = "Alex Mercer (Shift Supervisor)",
            damage = "Bearing cage distress and abnormal high-frequency harmonics",
            cause = "Possible lubrication starvation or excessive feed lump size",
            observation = "Vibration meter logged 7.8 mm/s peak velocity (threshold 5.5 mm/s). Audible clicking when feed surges."
        ),
        MaintenanceNotificationEntity(
            notificationNumber = "NOTIF-100198",
            equipmentId = "121SC008",
            description = "Rubber buffer isolator boot torn on rear left corner",
            priority = "MEDIUM",
            status = "IN_PROGRESS",
            creationDate = "2026-09-28 09:15",
            reportedBy = "P. Johal (Operator)",
            damage = "Elastomer split exposing internal coil spring to slurry contamination",
            cause = "Material spillage overflow from feed chute curtain",
            observation = "Temporary skirt clamp installed. Need replacement heavy-duty hollow rubber spring."
        ),
        MaintenanceNotificationEntity(
            notificationNumber = "NOTIF-099882",
            equipmentId = "121SC008",
            description = "Feed chute deflector liner bolt sheared off",
            priority = "LOW",
            status = "COMPLETED",
            creationDate = "2026-09-18 16:40",
            reportedBy = "David Chen",
            damage = "One M24 high tensile cup-head bolt dropped into discharge chute",
            cause = "Impact fatigue from oversize boulder strike",
            observation = "Replaced with Grade 10.9 fastener and torque-checked adjacent 7 bolts."
        ),

        // CV04 Notifications
        MaintenanceNotificationEntity(
            notificationNumber = "NOTIF-100240",
            equipmentId = "CV04",
            description = "Belt drift switch tripped twice in shift 1",
            priority = "HIGH",
            status = "OPEN",
            creationDate = "2026-10-02 06:10",
            reportedBy = "Operator Station 2",
            damage = "Belt tracking 60mm to non-drive side",
            cause = "Material buildup on snub pulley face",
            observation = "Belt scraper blade tungsten tip missing on left edge."
        )
    )

    val maintenancePlans = listOf(
        MaintenancePlanEntity(
            planId = "MP-121SC-01",
            equipmentId = "121SC008",
            planName = "GRIZZLY FEEDER INSPECTION & LUBE",
            maintenanceItem = "Deck Liners, Exciter Bearings & Spring Isolators",
            taskList = "TL-SC-008-WK",
            strategy = "Time-Based Preventive",
            frequency = "Weekly",
            lastExecution = "26 Sep 2026",
            nextPlannedDate = "03 Oct 2026",
            status = "SCHEDULED",
            checklistItemsRaw = "Inspect deck bar wear and clamp tightness;Verify exciter gearbox oil levels and clarity;Check coil spring isolators for cracking or tilt;Test emergency pull-wire stop switches;Inspect flexible seal curtains on feed box"
        ),
        MaintenancePlanEntity(
            planId = "MP-121SC-02",
            equipmentId = "121SC008",
            planName = "MONTHLY DYNAMIC CALIBRATION & STRUCTURE",
            maintenanceItem = "Screen Body Weldments & Cardan Shaft",
            taskList = "TL-SC-008-MO",
            strategy = "Condition-Based Predictive",
            frequency = "Monthly",
            lastExecution = "04 Sep 2026",
            nextPlannedDate = "04 Oct 2026",
            status = "SCHEDULED",
            checklistItemsRaw = "Torque check all huck bolts on side plates;Non-destructive MPI check on cross-beam gussets;Grease cardan universal joints with EP2;Measure stroke angle and amplitude with vibration chart;Check drive motor V-belt tension and alignment"
        ),
        MaintenancePlanEntity(
            planId = "MP-CV04-01",
            equipmentId = "CV04",
            planName = "CONVEYOR COMPREHENSIVE WALKDOWN",
            maintenanceItem = "Drive Pulleys, Scrapers & Emergency Wire",
            taskList = "TL-CV-004-WK",
            strategy = "Time-Based Preventive",
            frequency = "Weekly",
            lastExecution = "27 Sep 2026",
            nextPlannedDate = "04 Oct 2026",
            status = "SCHEDULED",
            checklistItemsRaw = "Inspect primary and secondary belt scrapers;Check gearbox oil sight glass;Inspect all return and carry idler bearings;Test conveyor emergency stop pull-cords"
        )
    )

    val inspections = listOf(
        InspectionEntity(
            inspectionId = "INSP-20261001-01",
            equipmentId = "121SC008",
            inspectionDate = "2026-10-01 11:30",
            technician = "Marcus Vance",
            inspectionType = "Weekly Operational Walkdown",
            result = "WARNING",
            observations = "Elevated temperature on drive exciter housing. Minor slurry splash ingress on side skirt rubber.",
            measurements = "Drive Bearing Temp: 74.2°C (Norm <65°C), Non-Drive Temp: 58.1°C, Stroke: 10.2mm @ 48°",
            anomaliesDetected = "Drive bearing thermal rise (+16°C above ambient baseline); Feed curtain needs alignment.",
            photos = null
        ),
        InspectionEntity(
            inspectionId = "INSP-20260924-02",
            equipmentId = "121SC008",
            inspectionDate = "2026-09-24 10:15",
            technician = "Elena Rostova",
            inspectionType = "Acoustic & Vibration Scan",
            result = "OK",
            observations = "Overall spectrum within ISO 10816-3 Class IV acceptable band. No looseness peaks detected.",
            measurements = "Overall Vibration: 4.1 mm/s RMS, Crest Factor: 3.2, Oil Level: Nominal",
            anomaliesDetected = "None. Routine wear consistent with 8,400 run hours.",
            photos = null
        ),
        InspectionEntity(
            inspectionId = "INSP-20260912-03",
            equipmentId = "121SC008",
            inspectionDate = "2026-09-12 15:45",
            technician = "David Chen",
            inspectionType = "Deck Liner Ultrasonic Thickness",
            result = "WARNING",
            observations = "Grizzly tines 2 and 3 show accelerated gouging wear near discharge crown.",
            measurements = "Bar #1: 42mm, Bar #2: 36mm (Min 35mm), Bar #3: 34mm (Below Spec), Bar #4: 41mm",
            anomaliesDetected = "Bar #3 reached replacement criterion. Scheduled for upcoming planned shutdown.",
            photos = null
        )
    )

    val downtimeEvents = listOf(
        DowntimeEventEntity(
            downtimeId = "DT-20260921-01",
            equipmentId = "121SC008",
            startDateTime = "2026-09-21 04:15",
            endDateTime = "2026-09-21 07:45",
            durationMinutes = 210,
            downtimeType = "Unplanned Breakdown",
            cause = "Feed chute boulder blockage wedging between grizzly fingers",
            comment = "Hydraulic rock breaker deployed to fragment 1.8m boulder. Fingers inspected for deformation prior to restart.",
            relatedWorkOrder = "WO-3998901"
        ),
        DowntimeEventEntity(
            downtimeId = "DT-20260905-02",
            equipmentId = "121SC008",
            startDateTime = "2026-09-05 08:00",
            endDateTime = "2026-09-05 14:00",
            durationMinutes = 360,
            downtimeType = "Planned PM",
            cause = "Monthly planned shutdown maintenance window",
            comment = "Drive V-belt replacement, grease purge, safety interlocking verification.",
            relatedWorkOrder = "WO-3995011"
        ),
        DowntimeEventEntity(
            downtimeId = "DT-20260714-03",
            equipmentId = "121SC008",
            startDateTime = "2026-07-14 22:30",
            endDateTime = "2026-07-15 01:10",
            durationMinutes = 160,
            downtimeType = "Emergency Stop",
            cause = "Tripped zero-speed rotation monitor due to rock jam",
            comment = "Clearance verified by mechanical shift lead. Speed sensor bracket realigned.",
            relatedWorkOrder = null
        )
    )

    val spareParts = listOf(
        MaterialConsumptionEntity(
            consumptionId = "MC-88120",
            equipmentId = "121SC008",
            materialNumber = "MAT-08912",
            materialDescription = "Spherical Roller Bearing 22328 CC/W33 (Vibrating Screen Duty)",
            quantity = 2.0,
            unit = "PC",
            consumptionDate = "2026-08-14",
            workOrder = "WO-3991200",
            storageLocation = "WH-01 / Shelf B-14"
        ),
        MaterialConsumptionEntity(
            consumptionId = "MC-88121",
            equipmentId = "121SC008",
            materialNumber = "MAT-04415",
            materialDescription = "Mobil SHC 630 Synthetic Gear & Bearing Lubricant",
            quantity = 42.0,
            unit = "L",
            consumptionDate = "2026-08-28",
            workOrder = "WO-3995011",
            storageLocation = "LUBE-STATION / Tank 04"
        ),
        MaterialConsumptionEntity(
            consumptionId = "MC-88122",
            equipmentId = "121SC008",
            materialNumber = "MAT-11029",
            materialDescription = "Cast Manganese Tapered Grizzly Bar Step Liner",
            quantity = 4.0,
            unit = "PC",
            consumptionDate = "2026-06-10",
            workOrder = "WO-3978100",
            storageLocation = "YARD-HEAVY / Bay 02"
        ),
        MaterialConsumptionEntity(
            consumptionId = "MC-88123",
            equipmentId = "121SC008",
            materialNumber = "MAT-02941",
            materialDescription = "Hollow Cylindrical Rubber Isolation Spring Block 160x200",
            quantity = 8.0,
            unit = "PC",
            consumptionDate = "2026-05-02",
            workOrder = "WO-3965412",
            storageLocation = "WH-01 / Shelf E-08"
        )
    )

    val documents = listOf(
        TechnicalDocumentEntity(
            documentId = "DOC-VF650-OM",
            equipmentId = "121SC008",
            documentName = "Metso VF650 Installation, Operation & Maintenance Manual",
            type = "Manual",
            revision = "Rev 4.1",
            date = "2023-04-15",
            summary = "Factory OEM technical specification, clearance tolerances, bolt torque tables, and lubrication schedules."
        ),
        TechnicalDocumentEntity(
            documentId = "DOC-VF650-GA",
            equipmentId = "121SC008",
            documentName = "General Arrangement & Foundation Static/Dynamic Load Drawing",
            type = "Drawing",
            revision = "Rev 2.0",
            date = "2021-01-10",
            summary = "Structural anchor bolt centers, feeder throat slope geometry, and chute clearances."
        ),
        TechnicalDocumentEntity(
            documentId = "DOC-VF650-SOP",
            equipmentId = "121SC008",
            documentName = "Safe Work Procedure: Confined Space Entry & Chute Clearance",
            type = "Procedure",
            revision = "Rev 3.0",
            date = "2025-11-20",
            summary = "Isolation protocol, tagout points, atmospheric gas testing, and overhead rock hazard barrier installation."
        ),
        TechnicalDocumentEntity(
            documentId = "DOC-VF650-ELEC",
            equipmentId = "121SC008",
            documentName = "Exciter Drive Motor MCC Schematic & Interlock Logic",
            type = "Electrical Schematic",
            revision = "Rev 1.3",
            date = "2022-09-08",
            summary = "415V soft-starter schematics, RTD thermistor wiring, and DCS emergency trip loops."
        )
    )

    val initialConfirmations = listOf(
        com.example.data.model.JobConfirmationEntity(
            clientConfirmationId = "CONF-DEMO-001",
            remoteConfirmationId = "SAP-CONF-98412",
            workOrder = "WO-4001844",
            operation = "0010",
            equipmentId = "121SC008",
            equipmentName = "VIBRATING GRIZZLY 121SC008",
            functionalLocation = "BI-PLN-CRU/CRS-003",
            workCenter = "PREDICTIVE",
            technicianId = "Elena Rostova",
            workStart = "2026-10-02T08:00:00",
            workFinish = "2026-10-02T12:00:00",
            elapsedMinutes = 240,
            actualWork = 3.0,
            actualWorkUnit = "H",
            completionType = "PARTIAL",
            incompleteReason = "Additional vibration spectrum monitoring required",
            workNote = "Baseline vibration spectrum recorded. Bearing DE vibration at 3.8 mm/s within limits.",
            workPerformedFlags = "Inspection completed;Adjustment performed",
            measurementsJson = "Vibration: 3.8 mm/s; Bearing Temp: 62 °C",
            materialsJson = "",
            syncStatus = "SYNCED",
            createdAt = "2026-10-02T12:05:00"
        ),
        com.example.data.model.JobConfirmationEntity(
            clientConfirmationId = "CONF-DEMO-002",
            remoteConfirmationId = "SAP-CONF-98110",
            workOrder = "WO-3998120",
            operation = "0010",
            equipmentId = "121SC008",
            equipmentName = "VIBRATING GRIZZLY 121SC008",
            functionalLocation = "BI-PLN-CRU/CRS-003",
            workCenter = "MECHANICAL",
            technicianId = "A. Sawadogo",
            workStart = "2026-09-28T09:15:00",
            workFinish = "2026-09-28T11:45:00",
            elapsedMinutes = 150,
            actualWork = 2.5,
            actualWorkUnit = "H",
            completionType = "FINAL",
            workNote = "Discharge lip weldment gouged and re-welded with E7018 electrode. Stress relief inspect completed.",
            workPerformedFlags = "Component replaced;Inspection completed",
            measurementsJson = "",
            materialsJson = "Hardox 450 Liner Qty 1; Welding Rod E7018 Qty 5 KG",
            syncStatus = "SYNCED",
            createdAt = "2026-09-28T11:50:00"
        )
    )
}
