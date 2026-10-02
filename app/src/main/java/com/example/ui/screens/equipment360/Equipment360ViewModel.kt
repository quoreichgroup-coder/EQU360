package com.example.ui.screens.equipment360

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Equipment360Data
import com.example.data.model.InspectionEntity
import com.example.data.model.MaintenanceNotificationEntity
import com.example.data.model.MaintenancePlanEntity
import com.example.data.model.WorkOrderEntity
import com.example.data.repository.IEquipmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class Equipment360UiState(
    val data: Equipment360Data = Equipment360Data(equipment = null),
    val isLoading: Boolean = true
) {
    val equipment get() = data.equipment
    val summary get() = data.summary
    val workOrders get() = data.workOrders
    val notifications get() = data.notifications
    val plans get() = data.plans
    val inspections get() = data.inspections
    val downtimes get() = data.downtimes
    val spareParts get() = data.spareParts
    val documents get() = data.documents
    val isOfflineAvailable get() = data.isOfflineAvailable
}

class Equipment360ViewModel(
    val equipmentId: String,
    private val repository: IEquipmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(Equipment360UiState())
    val uiState: StateFlow<Equipment360UiState> = _uiState.asStateFlow()

    init {
        observeEquipment360()
    }

    private fun observeEquipment360() {
        viewModelScope.launch {
            repository.getEquipment360(equipmentId).collect { data ->
                _uiState.value = Equipment360UiState(
                    data = data,
                    isLoading = false
                )
            }
        }
    }

    fun createNotification(notification: MaintenanceNotificationEntity) {
        viewModelScope.launch {
            repository.createNotification(notification)
        }
    }

    fun recordInspection(inspection: InspectionEntity) {
        viewModelScope.launch {
            repository.recordInspection(inspection)
        }
    }

    fun updatePlan(plan: MaintenancePlanEntity) {
        viewModelScope.launch {
            repository.updatePlan(plan)
        }
    }

    fun updateWorkOrderStatus(workOrder: WorkOrderEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateWorkOrderStatus(workOrder, newStatus)
        }
    }

    class Factory(
        private val equipmentId: String,
        private val repository: IEquipmentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return Equipment360ViewModel(equipmentId, repository) as T
        }
    }
}
