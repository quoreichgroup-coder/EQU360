package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.ActiveJobEntity
import com.example.data.model.ConfirmationApiRequest
import com.example.data.model.ConfirmationSyncStatus
import com.example.data.model.JobCompletionType
import com.example.data.model.JobConfirmationEntity
import com.example.data.model.MaterialUsedItem
import com.example.data.model.MeasurementItem
import com.example.data.model.TeamLaborItem
import com.example.data.model.WorkOrderEntity
import com.example.data.remote.ConfirmationRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

interface IConfirmationRepository {
    fun getActiveJob(): Flow<ActiveJobEntity?>
    suspend fun getActiveJobSync(): ActiveJobEntity?
    suspend fun startJob(
        workOrder: String,
        equipmentId: String,
        equipmentName: String,
        functionalLocation: String,
        workCenter: String,
        technicianId: String = "A. Sawadogo",
        operation: String = "0010"
    ): ActiveJobEntity

    suspend fun clearActiveJob()

    fun getConfirmationsForWorkOrder(workOrder: String): Flow<List<JobConfirmationEntity>>
    fun getConfirmationsForEquipment(equipmentId: String): Flow<List<JobConfirmationEntity>>
    fun getAllConfirmations(): Flow<List<JobConfirmationEntity>>

    suspend fun submitConfirmation(confirmation: JobConfirmationEntity): Result<JobConfirmationEntity>
    suspend fun syncPendingConfirmations(): Int
    suspend fun getWorkOrder(workOrder: String): WorkOrderEntity?
    fun calculateElapsedMinutes(startStr: String, finishStr: String): Int
}

class ConfirmationRepository(
    private val database: AppDatabase,
    val remoteDataSource: ConfirmationRemoteDataSource = ConfirmationRemoteDataSource()
) : IConfirmationRepository {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    override fun getActiveJob(): Flow<ActiveJobEntity?> {
        return database.activeJobDao().getActiveJob()
    }

    override suspend fun getActiveJobSync(): ActiveJobEntity? = withContext(Dispatchers.IO) {
        database.activeJobDao().getActiveJobSync()
    }

    override suspend fun startJob(
        workOrder: String,
        equipmentId: String,
        equipmentName: String,
        functionalLocation: String,
        workCenter: String,
        technicianId: String,
        operation: String
    ): ActiveJobEntity = withContext(Dispatchers.IO) {
        val now = Date()
        val activeJob = ActiveJobEntity(
            workOrder = workOrder,
            operation = operation,
            equipmentId = equipmentId,
            equipmentName = equipmentName,
            functionalLocation = functionalLocation,
            workCenter = workCenter,
            startDate = dateFormat.format(now),
            startTime = timeFormat.format(now),
            startTimestamp = now.time,
            technicianId = technicianId
        )
        database.activeJobDao().setActiveJob(activeJob)
        activeJob
    }

    override suspend fun clearActiveJob() = withContext(Dispatchers.IO) {
        database.activeJobDao().clearActiveJob()
    }

    override fun getConfirmationsForWorkOrder(workOrder: String): Flow<List<JobConfirmationEntity>> {
        return database.jobConfirmationDao().getConfirmationsForWorkOrder(workOrder)
    }

    override fun getConfirmationsForEquipment(equipmentId: String): Flow<List<JobConfirmationEntity>> {
        return database.jobConfirmationDao().getConfirmationsForEquipment(equipmentId)
    }

    override fun getAllConfirmations(): Flow<List<JobConfirmationEntity>> {
        return database.jobConfirmationDao().getAllConfirmations()
    }

    override suspend fun getWorkOrder(workOrder: String): WorkOrderEntity? = withContext(Dispatchers.IO) {
        database.workOrderDao().getWorkOrderById(workOrder)
    }

    override suspend fun submitConfirmation(confirmation: JobConfirmationEntity): Result<JobConfirmationEntity> =
        withContext(Dispatchers.IO) {
            // Validation step
            val validationError = validateConfirmation(confirmation)
            if (validationError != null) {
                return@withContext Result.failure(IllegalArgumentException(validationError))
            }

            // Save to Room locally first (Guaranteed Offline Availability)
            var currentConf = confirmation.copy(
                syncStatus = ConfirmationSyncStatus.PENDING_SYNC.name
            )
            database.jobConfirmationDao().insertConfirmation(currentConf)

            // If work is completed (FINAL), update work order status to CNF in Room
            if (confirmation.completionType == JobCompletionType.FINAL.name) {
                val existingWo = database.workOrderDao().getWorkOrderById(confirmation.workOrder)
                if (existingWo != null) {
                    database.workOrderDao().updateWorkOrder(existingWo.copy(status = "CNF"))
                }
            } else if (confirmation.completionType == JobCompletionType.PARTIAL.name) {
                val existingWo = database.workOrderDao().getWorkOrderById(confirmation.workOrder)
                if (existingWo != null && existingWo.status == "REL") {
                    database.workOrderDao().updateWorkOrder(existingWo.copy(status = "PCNF"))
                }
            }

            // Clear active job if confirming this work order
            val active = database.activeJobDao().getActiveJobSync()
            if (active != null && active.workOrder == confirmation.workOrder) {
                database.activeJobDao().clearActiveJob()
            }

            // Attempt remote synchronization
            val apiRequest = ConfirmationApiRequest(
                clientConfirmationId = confirmation.clientConfirmationId,
                workOrder = confirmation.workOrder,
                operation = confirmation.operation,
                equipmentId = confirmation.equipmentId,
                technicianId = confirmation.technicianId,
                workStart = confirmation.workStart,
                workFinish = confirmation.workFinish,
                elapsedMinutes = confirmation.elapsedMinutes,
                actualWork = confirmation.actualWork,
                actualWorkUnit = confirmation.actualWorkUnit,
                completionType = confirmation.completionType,
                incompleteReason = confirmation.incompleteReason,
                workNote = confirmation.workNote,
                workPerformed = confirmation.workPerformedFlags.split(";").filter { it.isNotBlank() },
                measurements = parseMeasurements(confirmation.measurementsJson),
                materials = parseMaterials(confirmation.materialsJson)
            )

            val remoteResult = remoteDataSource.submitConfirmation(apiRequest)
            if (remoteResult.isSuccess) {
                val response = remoteResult.getOrThrow()
                currentConf = currentConf.copy(
                    remoteConfirmationId = response.remoteConfirmationId,
                    syncStatus = ConfirmationSyncStatus.SYNCED.name,
                    syncErrorMessage = null
                )
                database.jobConfirmationDao().updateSyncStatus(
                    clientConfirmationId = currentConf.clientConfirmationId,
                    status = ConfirmationSyncStatus.SYNCED.name,
                    remoteId = response.remoteConfirmationId,
                    error = null
                )
            } else {
                val errorMsg = remoteResult.exceptionOrNull()?.message ?: "Sync error"
                database.jobConfirmationDao().updateSyncStatus(
                    clientConfirmationId = currentConf.clientConfirmationId,
                    status = ConfirmationSyncStatus.PENDING_SYNC.name,
                    remoteId = null,
                    error = errorMsg
                )
            }

            Result.success(currentConf)
        }

    override suspend fun syncPendingConfirmations(): Int = withContext(Dispatchers.IO) {
        val pending = database.jobConfirmationDao().getPendingSyncConfirmations()
        var syncedCount = 0

        for (conf in pending) {
            val apiRequest = ConfirmationApiRequest(
                clientConfirmationId = conf.clientConfirmationId,
                workOrder = conf.workOrder,
                operation = conf.operation,
                equipmentId = conf.equipmentId,
                technicianId = conf.technicianId,
                workStart = conf.workStart,
                workFinish = conf.workFinish,
                elapsedMinutes = conf.elapsedMinutes,
                actualWork = conf.actualWork,
                actualWorkUnit = conf.actualWorkUnit,
                completionType = conf.completionType,
                incompleteReason = conf.incompleteReason,
                workNote = conf.workNote,
                workPerformed = conf.workPerformedFlags.split(";").filter { it.isNotBlank() },
                measurements = parseMeasurements(conf.measurementsJson),
                materials = parseMaterials(conf.materialsJson)
            )

            val result = remoteDataSource.submitConfirmation(apiRequest)
            if (result.isSuccess) {
                val resp = result.getOrThrow()
                database.jobConfirmationDao().updateSyncStatus(
                    clientConfirmationId = conf.clientConfirmationId,
                    status = ConfirmationSyncStatus.SYNCED.name,
                    remoteId = resp.remoteConfirmationId,
                    error = null
                )
                syncedCount++
            }
        }
        syncedCount
    }

    /**
     * Calculates elapsed time in minutes between two timestamps or "HH:mm" time strings.
     */
    override fun calculateElapsedMinutes(startStr: String, finishStr: String): Int {
        try {
            // First try parsing as ISO date-time "yyyy-MM-dd'T'HH:mm:ss" or "yyyy-MM-dd'T'HH:mm"
            val startClean = if (startStr.length == 16) "$startStr:00" else startStr
            val finishClean = if (finishStr.length == 16) "$finishStr:00" else finishStr
            val startDate = isoFormat.parse(startClean)
            val finishDate = isoFormat.parse(finishClean)
            if (startDate != null && finishDate != null) {
                val diffMs = finishDate.time - startDate.time
                return maxOf(0, (diffMs / (60 * 1000)).toInt())
            }
        } catch (_: Exception) {}

        try {
            // Fallback: parse as HH:mm
            val startTime = timeFormat.parse(startStr.takeLast(5))
            val finishTime = timeFormat.parse(finishStr.takeLast(5))
            if (startTime != null && finishTime != null) {
                var diffMs = finishTime.time - startTime.time
                if (diffMs < 0) {
                    // Span across midnight (e.g. 23:00 to 01:00)
                    diffMs += 24 * 60 * 60 * 1000
                }
                return maxOf(0, (diffMs / (60 * 1000)).toInt())
            }
        } catch (_: Exception) {}

        return 0
    }

    private fun validateConfirmation(conf: JobConfirmationEntity): String? {
        if (conf.workOrder.isBlank()) return "Work Order cannot be blank."
        if (conf.operation.isBlank()) return "Operation cannot be blank."
        if (conf.workStart.isBlank()) return "Work Start time is required."
        if (conf.workFinish.isBlank()) return "Work Finish time is required."
        if (conf.actualWork < 0) return "Actual Work cannot be negative."

        val elapsed = calculateElapsedMinutes(conf.workStart, conf.workFinish)
        // Finish >= Start check: if start and finish are on same day and elapsed is 0 while strings differ in reverse order
        if (conf.workStart > conf.workFinish && !conf.workFinish.contains("T")) {
            return "Finish time must be equal to or greater than start time."
        }
        return null
    }

    private fun parseMeasurements(json: String): List<MeasurementItem> {
        if (json.isBlank()) return emptyList()
        return json.split(";").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size >= 2) {
                val key = parts[0].trim()
                val valUnit = parts[1].trim().split(" ")
                val v = valUnit.firstOrNull() ?: ""
                val u = if (valUnit.size > 1) valUnit[1] else ""
                MeasurementItem(name = key, value = v, unit = u)
            } else null
        }
    }

    private fun parseMaterials(json: String): List<MaterialUsedItem> {
        if (json.isBlank()) return emptyList()
        return json.split(";").mapNotNull { entry ->
            val parts = entry.split("Qty")
            if (parts.size >= 2) {
                val desc = parts[0].trim()
                val qtyUnit = parts[1].trim().split(" ")
                val q = qtyUnit.firstOrNull()?.toDoubleOrNull() ?: 1.0
                val u = if (qtyUnit.size > 1) qtyUnit[1] else "PC"
                MaterialUsedItem(materialNumber = "MAT-${desc.hashCode().toString().takeLast(6)}", description = desc, quantity = q, unit = u)
            } else null
        }
    }
}
