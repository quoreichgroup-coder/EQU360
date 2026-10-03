package com.example.ui.screens.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ActiveJobEntity
import com.example.data.model.JobCompletionType
import com.example.data.model.JobConfirmationEntity
import com.example.data.model.MaterialUsedItem
import com.example.data.model.MeasurementItem
import com.example.data.model.TeamLaborItem
import com.example.data.model.WorkOrderEntity
import com.example.data.repository.IConfirmationRepository
import com.example.data.repository.IEquipmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ConfirmationUiState(
    val workOrder: String = "",
    val operation: String = "0010",
    val equipmentId: String = "",
    val equipmentName: String = "",
    val functionalLocation: String = "",
    val workCenter: String = "MECHANICAL",
    val technicianId: String = "A. Sawadogo",
    val plannedWorkHours: Double = 6.0,
    val workOrderDescription: String = "",

    // Section 2: Time
    val workStartDate: String = "02 Oct 2026",
    val workStartTime: String = "14:32",
    val workFinishDate: String = "02 Oct 2026",
    val workFinishTime: String = "16:05",
    val elapsedMinutes: Int = 93,
    val formattedElapsedTime: String = "1 h 33 min",

    // Section 3: Actual Work
    val actualWorkInput: String = "1.5",
    val actualWorkUnit: String = "H",
    val suggestedWorkFromElapsed: Double = 1.55,
    val hasManuallyModifiedActualWork: Boolean = false,
    val teamLabor: List<TeamLaborItem> = emptyList(),

    // Section 4: Work Performed
    val performedTasks: Set<String> = setOf("Inspection completed", "Lubrication completed"),
    val workNote: String = "Inspected head and tail pulley bearings. Lubrication completed. No abnormal clearance observed.",
    val isRecordingVoice: Boolean = false,
    val measurements: List<MeasurementItem> = listOf(
        MeasurementItem("Bearing Temperature", "68", "°C"),
        MeasurementItem("Vibration", "4.2", "mm/s"),
        MeasurementItem("Bearing Clearance", "0.08", "mm")
    ),
    val materialsUsed: List<MaterialUsedItem> = listOf(
        MaterialUsedItem("MAT-620801", "Bearing 6208", 2.0, "PC"),
        MaterialUsedItem("MAT-EP2005", "Grease EP2", 0.5, "KG")
    ),
    val photoEvidenceCount: Int = 1,

    // Section 5: Completion
    val isJobComplete: Boolean = true, // YES = Work Completed, NO = More Work Required
    val incompleteReason: String = "Waiting for spare parts",
    val incompleteNotes: String = "",

    // Navigation & Submission
    val isReviewMode: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val submittedResult: JobConfirmationEntity? = null,
    val errorMessage: String? = null
)

class ConfirmationViewModel(
    val workOrderNumber: String,
    val confirmationRepository: IConfirmationRepository,
    val equipmentRepository: IEquipmentRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfirmationUiState(workOrder = workOrderNumber))
    val uiState: StateFlow<ConfirmationUiState> = _uiState.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    init {
        loadInitialContext()
    }

    private fun loadInitialContext() {
        viewModelScope.launch {
            // Check if there is an active job running on device for this work order
            val activeJob = confirmationRepository.getActiveJobSync()
            val now = Date()
            val currentFinishTime = timeFormat.format(now)
            val currentFinishDate = dateFormat.format(now)

            var startTime = "14:32"
            var startDate = "02 Oct 2026"
            var eqId = "121SC008"
            var eqName = "VIBRATING GRIZZLY 121SC008"
            var fl = "BI-PLN-CRU/CRS-003"
            var wc = "MECHANICAL"
            var desc = "Vibrating Grizzly Inspection"

            if (activeJob != null && (activeJob.workOrder == workOrderNumber || workOrderNumber.isBlank())) {
                startTime = activeJob.startTime
                startDate = activeJob.startDate
                eqId = activeJob.equipmentId
                eqName = activeJob.equipmentName
                fl = activeJob.functionalLocation
                wc = activeJob.workCenter
            }

            // Lookup WorkOrder details from DB if available
            val wo = confirmationRepository.getWorkOrder(workOrderNumber.ifBlank { activeJob?.workOrder ?: "WO 155704" })
            if (wo != null) {
                eqId = wo.equipmentId
                wc = wo.workCenter
                desc = wo.description
            }

            val elapsed = confirmationRepository.calculateElapsedMinutes(startTime, currentFinishTime)
            val formatted = formatElapsedMinutes(elapsed)
            val suggestedHours = (elapsed / 60.0 * 100).toInt() / 100.0

            _uiState.update { current ->
                current.copy(
                    workOrder = workOrderNumber.ifBlank { activeJob?.workOrder ?: "WO 155704" },
                    equipmentId = eqId,
                    equipmentName = eqName,
                    functionalLocation = fl,
                    workCenter = wc,
                    workOrderDescription = desc,
                    workStartDate = startDate,
                    workStartTime = startTime,
                    workFinishDate = currentFinishDate,
                    workFinishTime = currentFinishTime,
                    elapsedMinutes = elapsed,
                    formattedElapsedTime = formatted,
                    suggestedWorkFromElapsed = suggestedHours,
                    // Smart default: pre-fill 1.5 h or suggested if valid
                    actualWorkInput = if (suggestedHours > 0) String.format(Locale.US, "%.1f", suggestedHours) else "1.5"
                )
            }
        }
    }

    fun updateStartTime(time: String) {
        val elapsed = confirmationRepository.calculateElapsedMinutes(time, _uiState.value.workFinishTime)
        val formatted = formatElapsedMinutes(elapsed)
        val suggestedHours = (elapsed / 60.0 * 100).toInt() / 100.0

        _uiState.update { current ->
            current.copy(
                workStartTime = time,
                elapsedMinutes = elapsed,
                formattedElapsedTime = formatted,
                suggestedWorkFromElapsed = suggestedHours
            )
        }
    }

    fun updateFinishTime(time: String) {
        val elapsed = confirmationRepository.calculateElapsedMinutes(_uiState.value.workStartTime, time)
        val formatted = formatElapsedMinutes(elapsed)
        val suggestedHours = (elapsed / 60.0 * 100).toInt() / 100.0

        _uiState.update { current ->
            current.copy(
                workFinishTime = time,
                elapsedMinutes = elapsed,
                formattedElapsedTime = formatted,
                suggestedWorkFromElapsed = suggestedHours
            )
        }
    }

    fun updateActualWork(input: String) {
        _uiState.update { current ->
            current.copy(
                actualWorkInput = input,
                hasManuallyModifiedActualWork = true
            )
        }
    }

    fun updateActualWorkUnit(unit: String) {
        _uiState.update { it.copy(actualWorkUnit = unit) }
    }

    fun toggleTaskPerformed(task: String) {
        _uiState.update { current ->
            val set = current.performedTasks.toMutableSet()
            if (set.contains(task)) {
                set.remove(task)
            } else {
                set.add(task)
            }
            current.copy(performedTasks = set)
        }
    }

    fun updateWorkNote(note: String) {
        _uiState.update { it.copy(workNote = note) }
    }

    fun toggleVoiceRecording() {
        val nowRecording = !_uiState.value.isRecordingVoice
        _uiState.update { it.copy(isRecordingVoice = nowRecording) }

        if (nowRecording) {
            // Simulate voice dictation transcription after a brief moment
            viewModelScope.launch {
                kotlinx.coroutines.delay(1200)
                _uiState.update {
                    it.copy(
                        isRecordingVoice = false,
                        workNote = "Inspected head and tail pulley bearings. Lubrication completed. No abnormal clearance observed. Dynamic balance within acceptable threshold."
                    )
                }
            }
        }
    }

    fun updateMeasurement(name: String, value: String) {
        _uiState.update { current ->
            val updated = current.measurements.map { m ->
                if (m.name == name) m.copy(value = value) else m
            }
            current.copy(measurements = updated)
        }
    }

    fun addMaterial(description: String, quantity: Double, unit: String) {
        _uiState.update { current ->
            val newMat = MaterialUsedItem(
                materialNumber = "MAT-${UUID.randomUUID().toString().take(6).uppercase()}",
                description = description,
                quantity = quantity,
                unit = unit
            )
            current.copy(materialsUsed = current.materialsUsed + newMat)
        }
    }

    fun addTeamMember(technicianName: String, hours: Double) {
        _uiState.update { current ->
            current.copy(teamLabor = current.teamLabor + TeamLaborItem(technicianName, hours))
        }
    }

    fun removeTeamMember(index: Int) {
        _uiState.update { current ->
            val updated = current.teamLabor.toMutableList()
            if (index in updated.indices) {
                updated.removeAt(index)
            }
            current.copy(teamLabor = updated)
        }
    }

    fun setJobComplete(isComplete: Boolean) {
        _uiState.update { it.copy(isJobComplete = isComplete) }
    }

    fun setIncompleteReason(reason: String) {
        _uiState.update { it.copy(incompleteReason = reason) }
    }

    fun setIncompleteNotes(notes: String) {
        _uiState.update { it.copy(incompleteNotes = notes) }
    }

    fun setReviewMode(enabled: Boolean) {
        _uiState.update { it.copy(isReviewMode = enabled, errorMessage = null) }
    }

    suspend fun submitConfirmationAndWait(): Result<JobConfirmationEntity> {
        val state = _uiState.value
        val actualWorkVal = state.actualWorkInput.toDoubleOrNull() ?: 0.0

        if (actualWorkVal < 0) {
            _uiState.update { it.copy(errorMessage = "Actual work cannot be negative.") }
            return Result.failure(IllegalArgumentException("Actual work cannot be negative."))
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        val measurementsSummary = state.measurements.joinToString("; ") { "${it.name}: ${it.value} ${it.unit}" }
        val materialsSummary = state.materialsUsed.joinToString("; ") { "${it.description} Qty ${it.quantity} ${it.unit}" }
        val performedFlags = state.performedTasks.joinToString(";")

        val confirmation = JobConfirmationEntity(
            clientConfirmationId = UUID.randomUUID().toString(),
            workOrder = state.workOrder,
            operation = state.operation,
            equipmentId = state.equipmentId,
            equipmentName = state.equipmentName,
            functionalLocation = state.functionalLocation,
            workCenter = state.workCenter,
            technicianId = state.technicianId,
            workStart = "${state.workStartDate}T${state.workStartTime}",
            workFinish = "${state.workFinishDate}T${state.workFinishTime}",
            elapsedMinutes = state.elapsedMinutes,
            actualWork = actualWorkVal,
            actualWorkUnit = state.actualWorkUnit,
            completionType = if (state.isJobComplete) JobCompletionType.FINAL.name else JobCompletionType.PARTIAL.name,
            incompleteReason = if (!state.isJobComplete) state.incompleteReason else null,
            incompleteNotes = if (!state.isJobComplete) state.incompleteNotes else null,
            workNote = state.workNote,
            workPerformedFlags = performedFlags,
            measurementsJson = measurementsSummary,
            materialsJson = materialsSummary
        )

        val result = confirmationRepository.submitConfirmation(confirmation)
        if (result.isSuccess) {
            val saved = result.getOrThrow()
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    isSuccess = true,
                    submittedResult = saved
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Confirmation failed"
                )
            }
        }
        return result
    }

    fun submitConfirmation() {
        viewModelScope.launch {
            submitConfirmationAndWait()
        }
    }

    private fun formatElapsedMinutes(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "$h h $m min" else "$m min"
    }

    class Factory(
        private val workOrder: String,
        private val repository: IConfirmationRepository,
        private val equipmentRepo: IEquipmentRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ConfirmationViewModel(workOrder, repository, equipmentRepo) as T
        }
    }
}
