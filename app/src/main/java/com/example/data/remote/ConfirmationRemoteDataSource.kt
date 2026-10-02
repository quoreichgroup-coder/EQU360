package com.example.data.remote

import com.example.data.model.ConfirmationApiRequest
import com.example.data.model.ConfirmationApiResponse
import com.example.data.model.WorkOrderOperation
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

/**
 * Remote Data Source abstraction for the PlantCare API and SAP PM/EAM Integration layer.
 * Implements idempotency via clientConfirmationId, offline failure simulation, and domain responses.
 */
class ConfirmationRemoteDataSource {

    // Simulates remote server idempotency registry: clientConfirmationId -> ConfirmationApiResponse
    private val submittedConfirmations = ConcurrentHashMap<String, ConfirmationApiResponse>()

    // Configurable network simulator for test scenarios (e.g. offline testing)
    var isNetworkConnected: Boolean = true
    var shouldFailNextRequest: Boolean = false

    /**
     * POST /api/v1/confirmations
     * Submits or reconciles a maintenance confirmation to PlantCare API / SAP PM.
     */
    suspend fun submitConfirmation(request: ConfirmationApiRequest): Result<ConfirmationApiResponse> {
        // Check simulated network availability
        if (!isNetworkConnected) {
            return Result.failure(OfflineNetworkException("Device is offline. Saved locally."))
        }

        if (shouldFailNextRequest) {
            shouldFailNextRequest = false
            return Result.failure(RemoteApiException("SAP PM service temporarily unavailable. Stored in device sync queue."))
        }

        // Simulate network latency
        delay(250)

        // Idempotency check: Return existing confirmation if clientConfirmationId was already processed
        submittedConfirmations[request.clientConfirmationId]?.let { existing ->
            return Result.success(existing)
        }

        // Validate API payload
        if (request.actualWork < 0) {
            return Result.failure(IllegalArgumentException("Actual work cannot be negative."))
        }

        val confirmationSeq = 100000 + (submittedConfirmations.size + 1)
        val response = ConfirmationApiResponse(
            success = true,
            confirmationId = "CONF-$confirmationSeq",
            remoteConfirmationId = "SAP-CONF-$confirmationSeq",
            workOrder = request.workOrder,
            operation = request.operation,
            syncStatus = "SYNCED",
            message = "Maintenance confirmation registered in SAP PM successfully."
        )

        submittedConfirmations[request.clientConfirmationId] = response
        return Result.success(response)
    }

    /**
     * GET /api/v1/work-orders/{workOrder}/operations
     */
    suspend fun getWorkOrderOperations(workOrder: String): List<WorkOrderOperation> {
        delay(100)
        return listOf(
            WorkOrderOperation(
                operationNumber = "0010",
                description = "Mechanical Inspection & Lubrication",
                workCenter = "MECHANICAL",
                plannedHours = 6.0
            ),
            WorkOrderOperation(
                operationNumber = "0020",
                description = "Fastener Torque & Clearance Measurement",
                workCenter = "MECHANICAL",
                plannedHours = 2.0
            ),
            WorkOrderOperation(
                operationNumber = "0030",
                description = "Dynamic Run & Final Operational Check",
                workCenter = "MECHANICAL",
                plannedHours = 1.0
            )
        )
    }
}

class OfflineNetworkException(message: String) : Exception(message)
class RemoteApiException(message: String) : Exception(message)
