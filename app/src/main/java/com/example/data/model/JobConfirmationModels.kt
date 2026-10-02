package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Sync status for confirmations in the offline queue.
 */
enum class ConfirmationSyncStatus {
    DRAFT,
    PENDING_SYNC,
    SYNCING,
    SYNCED,
    SYNC_FAILED
}

/**
 * Confirmation completion type (SAP Final vs Partial confirmation).
 */
enum class JobCompletionType {
    FINAL,
    PARTIAL
}

/**
 * Persistent Room entity storing recorded job confirmations locally.
 */
@Entity(tableName = "job_confirmations")
data class JobConfirmationEntity(
    @PrimaryKey
    val clientConfirmationId: String = UUID.randomUUID().toString(),
    val remoteConfirmationId: String? = null,
    val workOrder: String,
    val operation: String = "0010",
    val equipmentId: String,
    val equipmentName: String = "",
    val functionalLocation: String = "",
    val workCenter: String = "MECHANICAL",
    val technicianId: String = "A. Sawadogo",
    val workStart: String, // e.g. "2026-10-02T14:32:00"
    val workFinish: String, // e.g. "2026-10-02T16:05:00"
    val elapsedMinutes: Int, // Calculated elapsed duration
    val actualWork: Double, // Actual work entered by technician (independent from elapsed time)
    val actualWorkUnit: String = "H", // "H" or "MIN"
    val completionType: String = JobCompletionType.FINAL.name, // "FINAL" or "PARTIAL"
    val incompleteReason: String? = null, // e.g. "Waiting for spare parts"
    val incompleteNotes: String? = null,
    val workNote: String = "",
    val workPerformedFlags: String = "", // Semicolon-delimited e.g. "Inspection completed;Lubrication completed"
    val measurementsJson: String = "", // e.g. Bearing Temp, Vibration, Clearance
    val materialsJson: String = "", // e.g. Bearing 6208, Grease EP2
    val photosJson: String? = null,
    val coWorkers: String? = null, // Secondary technicians and allocated hours
    val syncStatus: String = ConfirmationSyncStatus.PENDING_SYNC.name,
    val syncErrorMessage: String? = null,
    val createdAt: String = "2026-10-02T16:05:00"
)

/**
 * Room entity tracking currently active maintenance job on device.
 */
@Entity(tableName = "active_jobs")
data class ActiveJobEntity(
    @PrimaryKey
    val workOrder: String,
    val operation: String = "0010",
    val equipmentId: String,
    val equipmentName: String,
    val functionalLocation: String,
    val workCenter: String,
    val startDate: String, // "02 Oct 2026"
    val startTime: String, // "14:32"
    val startTimestamp: Long, // Epoch millis
    val technicianId: String = "A. Sawadogo"
)

/**
 * Work Order Operation detail.
 */
data class WorkOrderOperation(
    val operationNumber: String,
    val description: String,
    val workCenter: String,
    val plannedHours: Double
)

/**
 * Measurement item for contextual checks.
 */
data class MeasurementItem(
    val name: String,
    val value: String,
    val unit: String,
    val isWarning: Boolean = false
)

/**
 * Material item recorded during confirmation.
 */
data class MaterialUsedItem(
    val materialNumber: String,
    val description: String,
    val quantity: Double,
    val unit: String
)

/**
 * Additional technician participating in the job.
 */
data class TeamLaborItem(
    val technicianName: String,
    val hours: Double
)

/**
 * DTO for remote PlantCare API Contract POST /api/v1/confirmations
 */
data class ConfirmationApiRequest(
    val clientConfirmationId: String,
    val workOrder: String,
    val operation: String,
    val equipmentId: String,
    val technicianId: String,
    val workStart: String,
    val workFinish: String,
    val elapsedMinutes: Int,
    val actualWork: Double,
    val actualWorkUnit: String,
    val completionType: String,
    val incompleteReason: String?,
    val workNote: String,
    val workPerformed: List<String>,
    val measurements: List<MeasurementItem>,
    val materials: List<MaterialUsedItem>,
    val photos: List<String> = emptyList(),
    val teamLabor: List<TeamLaborItem> = emptyList()
)

/**
 * DTO response from remote PlantCare API Contract
 */
data class ConfirmationApiResponse(
    val success: Boolean,
    val confirmationId: String,
    val remoteConfirmationId: String?,
    val workOrder: String,
    val operation: String,
    val syncStatus: String,
    val message: String? = null
)
