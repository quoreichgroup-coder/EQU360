package com.example.ui.screens.mywork

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ActiveJobEntity
import com.example.data.model.WorkOrderEntity
import com.example.data.repository.IConfirmationRepository
import com.example.data.repository.IEquipmentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MyWorkUiState(
    val selectedTab: MyWorkTab = MyWorkTab.TODAY,
    val workOrders: List<WorkOrderEntity> = emptyList(),
    val activeJob: ActiveJobEntity? = null,
    val elapsedTimerString: String = "00:00",
    val jobStartFeedback: ActiveJobEntity? = null,
    val pendingSyncCount: Int = 0,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null
)

enum class MyWorkTab(val label: String) {
    TODAY("Today"),
    UPCOMING("Upcoming"),
    IN_PROGRESS("In Progress"),
    WAITING("Waiting"),
    COMPLETED("Completed")
}

class MyWorkViewModel(
    private val confirmationRepo: IConfirmationRepository,
    private val equipmentRepo: IEquipmentRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(MyWorkTab.TODAY)
    val selectedTab: StateFlow<MyWorkTab> = _selectedTab.asStateFlow()

    private val _jobStartFeedback = MutableStateFlow<ActiveJobEntity?>(null)
    val jobStartFeedback: StateFlow<ActiveJobEntity?> = _jobStartFeedback.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    val activeJob = confirmationRepo.getActiveJob()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val uiState: StateFlow<MyWorkUiState> = combine(
        combine(_selectedTab, equipmentRepo.getAllWorkOrders(), activeJob) { tab, orders, active ->
            Triple(tab, orders, active)
        },
        _elapsedSeconds,
        _jobStartFeedback,
        _syncMessage
    ) { triple, elapsedSec, feedback, syncMsg ->
        val (tab, orders, active) = triple
        val filtered = filterWorkOrders(orders, tab, active)
        val timerStr = formatTimer(elapsedSec)

        MyWorkUiState(
            selectedTab = tab,
            workOrders = filtered,
            activeJob = active,
            elapsedTimerString = timerStr,
            jobStartFeedback = feedback,
            syncMessage = syncMsg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MyWorkUiState())

    init {
        // Ticking timer for active job elapsed time
        viewModelScope.launch {
            while (isActive) {
                val currentActive = confirmationRepo.getActiveJobSync()
                if (currentActive != null) {
                    val diff = (System.currentTimeMillis() - currentActive.startTimestamp) / 1000
                    _elapsedSeconds.value = maxOf(0L, diff)
                } else {
                    _elapsedSeconds.value = 0L
                }
                delay(1000)
            }
        }
    }

    fun selectTab(tab: MyWorkTab) {
        _selectedTab.value = tab
    }

    fun startJob(workOrder: WorkOrderEntity) {
        viewModelScope.launch {
            val active = confirmationRepo.startJob(
                workOrder = workOrder.orderNumber,
                equipmentId = workOrder.equipmentId,
                equipmentName = "VIBRATING GRIZZLY 121SC008",
                functionalLocation = "BI-PLN-CRU/CRS-003",
                workCenter = workOrder.workCenter,
                technicianId = workOrder.assignedTechnician.ifBlank { "A. Sawadogo" }
            )
            _jobStartFeedback.value = active
            _selectedTab.value = MyWorkTab.IN_PROGRESS
        }
    }

    fun dismissJobStartFeedback() {
        _jobStartFeedback.value = null
    }

    fun triggerSync() {
        viewModelScope.launch {
            _syncMessage.value = "Synchronizing with PlantCare API..."
            val count = confirmationRepo.syncPendingConfirmations()
            _syncMessage.value = if (count > 0) "Synced $count pending confirmation(s) with SAP PM" else "All records up to date"
            delay(2500)
            _syncMessage.value = null
        }
    }

    private fun filterWorkOrders(
        orders: List<WorkOrderEntity>,
        tab: MyWorkTab,
        activeJob: ActiveJobEntity?
    ): List<WorkOrderEntity> {
        return when (tab) {
            MyWorkTab.TODAY -> orders.filter { it.status in listOf("REL", "CRTD") }
            MyWorkTab.UPCOMING -> orders.filter { it.status == "CRTD" || it.plannedDate >= "2026-10-04" }
            MyWorkTab.IN_PROGRESS -> orders.filter {
                it.status in listOf("REL", "PCNF") || (activeJob != null && it.orderNumber == activeJob.workOrder)
            }
            MyWorkTab.WAITING -> orders.filter { it.status == "PCNF" || it.priority == "VERY HIGH" }
            MyWorkTab.COMPLETED -> orders.filter { it.status in listOf("CNF", "TECO") }
        }
    }

    private fun formatTimer(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
        } else {
            String.format(Locale.US, "%02d:%02d", m, s)
        }
    }

    class Factory(
        private val confirmationRepo: IConfirmationRepository,
        private val equipmentRepo: IEquipmentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MyWorkViewModel(confirmationRepo, equipmentRepo) as T
        }
    }
}
