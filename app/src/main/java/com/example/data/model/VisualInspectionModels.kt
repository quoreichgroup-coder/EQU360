package com.example.data.model

/**
 * Input context provided to the AI for visual inspection.
 * Combines image, technician observation, and rich equipment maintenance history.
 */
data class VisualInspectionContext(
    val equipment: EquipmentEntity,
    val imageUri: String,
    val imageBase64: String? = null,
    val technicianObservation: String? = null,
    val openWorkOrders: List<WorkOrderEntity> = emptyList(),
    val openNotifications: List<MaintenanceNotificationEntity> = emptyList(),
    val recentInspections: List<InspectionEntity> = emptyList(),
    val maintenancePlans: List<MaintenancePlanEntity> = emptyList(),
    val downtimeHistory: List<DowntimeEventEntity> = emptyList(),
    val sparePartsHistory: List<MaterialConsumptionEntity> = emptyList()
)

/**
 * Structured result returned from AI visual inspection.
 * Designed to fit on a mobile screen and pre-populate SAP PM maintenance notifications.
 */
data class VisualInspectionResult(
    val possibleIssue: String,
    val confidence: String, // "High", "Medium", "Low"
    val observations: String,
    val recommendedChecks: List<String>,
    val suggestedNotificationTitle: String,
    val suggestedPriority: String, // "HIGH", "MEDIUM", "LOW", "VERY HIGH"
    val possibleCauses: String,
    val safetyWarning: String? = null,
    val isOfflineProcessed: Boolean = false
)
