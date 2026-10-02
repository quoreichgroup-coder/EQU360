package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipments")
data class EquipmentEntity(
    @PrimaryKey
    val equipmentId: String,
    val name: String,
    val description: String,
    val functionalLocation: String,
    val site: String,
    val area: String,
    val workCenter: String,
    val equipmentType: String,
    val manufacturer: String,
    val model: String,
    val serialNumber: String,
    val criticality: String, // "CRITICAL", "HIGH", "MEDIUM", "LOW"
    val status: String, // "ACTIVE", "MAINTENANCE", "STOPPED", "DEGRADED"
    val installationDate: String,
    val qrCodePayload: String
)

@Entity(tableName = "work_orders")
data class WorkOrderEntity(
    @PrimaryKey
    val orderNumber: String,
    val equipmentId: String,
    val description: String,
    val orderType: String, // "PM01" (Corrective), "PM02" (Preventive), "PM03" (Inspection), "PM04" (Emergency)
    val priority: String, // "VERY HIGH", "HIGH", "MEDIUM", "LOW"
    val status: String, // "CRTD" (Created), "REL" (Released), "PCNF" (Partially Confirmed), "CNF" (Confirmed), "TECO" (Technically Completed)
    val plannedDate: String,
    val workCenter: String,
    val assignedTechnician: String,
    val longText: String
)

@Entity(tableName = "maintenance_notifications")
data class MaintenanceNotificationEntity(
    @PrimaryKey
    val notificationNumber: String,
    val equipmentId: String,
    val description: String,
    val priority: String, // "VERY HIGH", "HIGH", "MEDIUM", "LOW"
    val status: String, // "OPEN", "IN_PROGRESS", "COMPLETED"
    val creationDate: String,
    val reportedBy: String,
    val damage: String,
    val cause: String,
    val observation: String,
    val photoUri: String? = null,
    val voiceNoteSummary: String? = null
)

@Entity(tableName = "maintenance_plans")
data class MaintenancePlanEntity(
    @PrimaryKey
    val planId: String,
    val equipmentId: String,
    val planName: String,
    val maintenanceItem: String,
    val taskList: String,
    val strategy: String,
    val frequency: String,
    val lastExecution: String,
    val nextPlannedDate: String,
    val status: String, // "SCHEDULED", "OVERDUE", "IN_PROGRESS", "COMPLETED"
    val checklistItemsRaw: String // Semicolon-delimited list of checklist items
)

@Entity(tableName = "inspections")
data class InspectionEntity(
    @PrimaryKey
    val inspectionId: String,
    val equipmentId: String,
    val inspectionDate: String,
    val technician: String,
    val inspectionType: String,
    val result: String, // "OK", "WARNING", "CRITICAL"
    val observations: String,
    val measurements: String,
    val anomaliesDetected: String,
    val photos: String? = null
)

@Entity(tableName = "downtime_events")
data class DowntimeEventEntity(
    @PrimaryKey
    val downtimeId: String,
    val equipmentId: String,
    val startDateTime: String,
    val endDateTime: String,
    val durationMinutes: Int,
    val downtimeType: String, // "Unplanned Breakdown", "Emergency Stop", "Planned PM"
    val cause: String,
    val comment: String,
    val relatedWorkOrder: String?
)

@Entity(tableName = "material_consumptions")
data class MaterialConsumptionEntity(
    @PrimaryKey
    val consumptionId: String,
    val equipmentId: String,
    val materialNumber: String,
    val materialDescription: String,
    val quantity: Double,
    val unit: String,
    val consumptionDate: String,
    val workOrder: String,
    val storageLocation: String
)

@Entity(tableName = "technical_documents")
data class TechnicalDocumentEntity(
    @PrimaryKey
    val documentId: String,
    val equipmentId: String,
    val documentName: String,
    val type: String, // "Procedure", "Manual", "Drawing", "Electrical Schematic", "Datasheet"
    val revision: String,
    val date: String,
    val summary: String
)

data class Equipment360Summary(
    val openWorkOrdersCount: Int,
    val openNotificationsCount: Int,
    val nextPmDays: String,
    val downtimeYtdHours: Double
)

data class Equipment360Data(
    val equipment: EquipmentEntity?,
    val summary: Equipment360Summary = Equipment360Summary(0, 0, "N/A", 0.0),
    val workOrders: List<WorkOrderEntity> = emptyList(),
    val notifications: List<MaintenanceNotificationEntity> = emptyList(),
    val plans: List<MaintenancePlanEntity> = emptyList(),
    val inspections: List<InspectionEntity> = emptyList(),
    val downtimes: List<DowntimeEventEntity> = emptyList(),
    val spareParts: List<MaterialConsumptionEntity> = emptyList(),
    val documents: List<TechnicalDocumentEntity> = emptyList(),
    val isOfflineAvailable: Boolean = true
)

