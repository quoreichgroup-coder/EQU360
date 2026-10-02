package com.example.ui.screens.visualinspection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Equipment360Data
import com.example.data.model.EquipmentEntity
import com.example.data.model.InspectionEntity
import com.example.data.model.MaintenanceNotificationEntity
import com.example.data.model.VisualInspectionContext
import com.example.data.model.VisualInspectionResult
import com.example.data.repository.IEquipmentRepository
import com.example.data.service.IMaintenanceAiService
import com.example.data.service.MaintenanceAiServiceImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class VisualInspectionStep {
    CAPTURE_PHOTO,
    PHOTO_PREVIEW_AND_OBSERVATION,
    ANALYZING,
    RESULT_SUMMARY,
    CONFIRM_NOTIFICATION,
    SUCCESS
}

data class VisualInspectionUiState(
    val equipmentId: String,
    val equipment: EquipmentEntity? = null,
    val currentStep: VisualInspectionStep = VisualInspectionStep.CAPTURE_PHOTO,
    val capturedImageUri: String? = null,
    val technicianObservation: String = "",
    val isRecordingVoice: Boolean = false,
    val analysisProgressMessage: String = "Analyzing equipment condition...",
    val inspectionResult: VisualInspectionResult? = null,
    // Notification Draft fields
    val draftTitle: String = "",
    val draftPriority: String = "HIGH",
    val draftObservation: String = "",
    val draftDamage: String = "",
    val draftCause: String = "",
    val draftChecks: List<String> = emptyList(),
    val createdNotificationNumber: String? = null,
    val isLoading: Boolean = true
)

class VisualInspectionViewModel(
    val equipmentId: String,
    private val repository: IEquipmentRepository,
    private val aiService: IMaintenanceAiService = MaintenanceAiServiceImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisualInspectionUiState(equipmentId = equipmentId))
    val uiState: StateFlow<VisualInspectionUiState> = _uiState.asStateFlow()

    private var equipmentData: Equipment360Data? = null
    private var analysisJob: Job? = null

    init {
        loadEquipmentContext()
    }

    private fun loadEquipmentContext() {
        viewModelScope.launch {
            repository.getEquipment360(equipmentId).collect { data ->
                equipmentData = data
                _uiState.value = _uiState.value.copy(
                    equipment = data.equipment,
                    isLoading = false
                )
            }
        }
    }

    fun onPhotoCaptured(imageUri: String) {
        _uiState.value = _uiState.value.copy(
            capturedImageUri = imageUri,
            currentStep = VisualInspectionStep.PHOTO_PREVIEW_AND_OBSERVATION
        )
    }

    fun onRetakePhoto() {
        _uiState.value = _uiState.value.copy(
            capturedImageUri = null,
            currentStep = VisualInspectionStep.CAPTURE_PHOTO
        )
    }

    fun onObservationChanged(observation: String) {
        _uiState.value = _uiState.value.copy(technicianObservation = observation)
    }

    fun toggleVoiceRecording() {
        val currentlyRecording = _uiState.value.isRecordingVoice
        if (!currentlyRecording) {
            _uiState.value = _uiState.value.copy(isRecordingVoice = true)
            viewModelScope.launch {
                delay(1500)
                val voiceTranscribed = "Abnormal vibration on the head pulley bearing. The bearing also feels hotter than usual."
                val existing = _uiState.value.technicianObservation
                val updated = if (existing.isBlank()) voiceTranscribed else "$existing $voiceTranscribed"
                _uiState.value = _uiState.value.copy(
                    technicianObservation = updated,
                    isRecordingVoice = false
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(isRecordingVoice = false)
        }
    }

    fun startAnalysis(skipAnimation: Boolean = false) {
        val eq = _uiState.value.equipment ?: return
        val imageUri = _uiState.value.capturedImageUri ?: "camera://captured/inspection_photo.jpg"

        _uiState.value = _uiState.value.copy(
            currentStep = VisualInspectionStep.ANALYZING,
            analysisProgressMessage = "Analyzing equipment condition..."
        )

        analysisJob = viewModelScope.launch {
            if (!skipAnimation) {
                delay(400)
                _uiState.value = _uiState.value.copy(analysisProgressMessage = "Checking visible abnormalities...")
                delay(500)
                _uiState.value = _uiState.value.copy(analysisProgressMessage = "Reviewing equipment history & work orders...")
            }

            val context = VisualInspectionContext(
                equipment = eq,
                imageUri = imageUri,
                technicianObservation = _uiState.value.technicianObservation.ifBlank { null },
                openWorkOrders = equipmentData?.workOrders ?: emptyList(),
                openNotifications = equipmentData?.notifications ?: emptyList(),
                recentInspections = equipmentData?.inspections ?: emptyList(),
                maintenancePlans = equipmentData?.plans ?: emptyList(),
                downtimeHistory = equipmentData?.downtimes ?: emptyList(),
                sparePartsHistory = equipmentData?.spareParts ?: emptyList()
            )

            val result = aiService.analyzeVisualInspection(context)

            _uiState.value = _uiState.value.copy(
                currentStep = VisualInspectionStep.RESULT_SUMMARY,
                inspectionResult = result,
                draftTitle = result.suggestedNotificationTitle,
                draftPriority = result.suggestedPriority,
                draftObservation = result.observations,
                draftDamage = result.possibleIssue,
                draftCause = result.possibleCauses,
                draftChecks = result.recommendedChecks
            )
        }
    }

    fun cancelAnalysis() {
        analysisJob?.cancel()
        _uiState.value = _uiState.value.copy(currentStep = VisualInspectionStep.PHOTO_PREVIEW_AND_OBSERVATION)
    }

    fun proceedToConfirmNotification() {
        _uiState.value = _uiState.value.copy(currentStep = VisualInspectionStep.CONFIRM_NOTIFICATION)
    }

    fun updateDraftTitle(title: String) {
        _uiState.value = _uiState.value.copy(draftTitle = title)
    }

    fun updateDraftPriority(priority: String) {
        _uiState.value = _uiState.value.copy(draftPriority = priority)
    }

    fun updateDraftObservation(obs: String) {
        _uiState.value = _uiState.value.copy(draftObservation = obs)
    }

    fun updateDraftDamage(damage: String) {
        _uiState.value = _uiState.value.copy(draftDamage = damage)
    }

    fun updateDraftCause(cause: String) {
        _uiState.value = _uiState.value.copy(draftCause = cause)
    }

    fun confirmAndCreateNotification(onFinished: () -> Unit) {
        val state = _uiState.value
        val eq = state.equipment ?: return

        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            val notifNumber = "NTF-${(System.currentTimeMillis() % 100000).toString().padStart(6, '0')}"

            // 1. Create Maintenance Notification
            val notification = MaintenanceNotificationEntity(
                notificationNumber = notifNumber,
                equipmentId = eq.equipmentId,
                description = state.draftTitle.ifBlank { "AI Visual Inspection Finding on ${eq.equipmentId}" },
                priority = state.draftPriority,
                status = "OPEN",
                creationDate = dateStr,
                reportedBy = "Alex Mercer (AI Visual Inspection)",
                damage = state.draftDamage.ifBlank { "Visible equipment anomaly" },
                cause = state.draftCause.ifBlank { "Operational wear" },
                observation = "${state.draftObservation}\n\nRecommended Checks:\n" +
                        state.draftChecks.joinToString("\n") { "• $it" },
                photoUri = state.capturedImageUri,
                voiceNoteSummary = if (state.technicianObservation.isNotBlank()) "Technician note: ${state.technicianObservation}" else null
            )
            repository.createNotification(notification)

            // 2. Log into Inspections history
            val inspection = InspectionEntity(
                inspectionId = "INSP-AI-${System.currentTimeMillis() % 1000000}",
                equipmentId = eq.equipmentId,
                inspectionDate = dateStr,
                technician = "Alex Mercer (AI Assisted)",
                inspectionType = "AI Visual Inspection",
                result = if (state.draftPriority in listOf("HIGH", "VERY HIGH")) "WARNING" else "OK",
                observations = "AI Visual Inspection: ${state.draftDamage}. Technician verified finding. Notification $notifNumber created.",
                measurements = "Visual inspection photo analyzed • Confidence: ${state.inspectionResult?.confidence ?: "Medium"}",
                anomaliesDetected = state.draftDamage,
                photos = state.capturedImageUri
            )
            repository.recordInspection(inspection)

            _uiState.value = _uiState.value.copy(
                createdNotificationNumber = notifNumber,
                currentStep = VisualInspectionStep.SUCCESS
            )
        }
    }

    class Factory(
        private val equipmentId: String,
        private val repository: IEquipmentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VisualInspectionViewModel(equipmentId, repository) as T
        }
    }
}
