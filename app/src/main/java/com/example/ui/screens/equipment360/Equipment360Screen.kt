package com.example.ui.screens.equipment360

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MaintenancePlanEntity
import com.example.data.model.TechnicalDocumentEntity
import com.example.data.model.WorkOrderEntity
import com.example.ui.components.StatusBadge
import com.example.ui.components.WorkCenterBadge
import com.example.ui.dialogs.CreateNotificationDialog
import com.example.ui.dialogs.DocumentPreviewDialog
import com.example.ui.dialogs.PmChecklistDialog
import com.example.ui.dialogs.StartInspectionDialog
import com.example.ui.dialogs.WorkOrderDetailDialog
import com.example.ui.screens.equipment360.sections.ActivitiesSection
import com.example.ui.screens.equipment360.sections.AiAssistantSection
import com.example.ui.screens.equipment360.sections.DocumentsSection
import com.example.ui.screens.equipment360.sections.HistorySection
import com.example.ui.screens.equipment360.sections.MaintenanceSection
import com.example.ui.screens.equipment360.sections.OverviewSection
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Equipment360Screen(
    viewModel: Equipment360ViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToVisualInspection: (String) -> Unit,
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedMainTab by remember { mutableIntStateOf(0) }
    var activitiesSubTab by remember { mutableIntStateOf(0) }
    var historySubTab by remember { mutableIntStateOf(0) }

    // Dialog controllers
    var showCreateNotifDialog by remember { mutableStateOf(false) }
    var showStartInspDialog by remember { mutableStateOf(false) }
    var selectedPmPlanForChecklist by remember { mutableStateOf<MaintenancePlanEntity?>(null) }
    var selectedWorkOrderForDetail by remember { mutableStateOf<WorkOrderEntity?>(null) }
    var selectedDocForPreview by remember { mutableStateOf<TechnicalDocumentEntity?>(null) }

    val mainTabs = listOf("Overview", "Activities", "Maintenance", "History", "Documents", "AI Assistant")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "EQUIPMENT 360°",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafetyAmber,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = uiState.equipment?.equipmentId ?: viewModel.equipmentId,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("equipment360_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToVisualInspection(uiState.equipment?.equipmentId ?: viewModel.equipmentId) },
                        modifier = Modifier.testTag("equipment360_visual_inspection_button")
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "AI Visual Inspection", tint = SafetyAmber)
                    }
                    IconButton(onClick = onNavigateToScanner, modifier = Modifier.testTag("equipment360_scan_button")) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scan New Equipment", tint = SafetyAmber)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Slate900
    ) { innerPadding ->
        if (uiState.isLoading && uiState.equipment == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SafetyAmber)
            }
            return@Scaffold
        }

        val eq = uiState.equipment
        if (eq == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(colors = CardDefaults.cardColors(containerColor = Slate800)) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Equipment '${viewModel.equipmentId}' Not Found", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(onClick = onNavigateToScanner, colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber)) {
                            Text("Scan Again", color = Slate900)
                        }
                    }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Equipment Banner Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = eq.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(status = eq.status)
                    StatusBadge(status = eq.criticality)
                    WorkCenterBadge(workCenter = eq.workCenter)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Functional Loc: ${eq.functionalLocation}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8)
                )
            }

            // Prominent Field Quick Actions (Requirement 2)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SafetyAmber.copy(alpha = 0.8f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showStartInspDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("action_btn_start_inspection_prominent"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Inspection", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { onNavigateToVisualInspection(eq.equipmentId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("action_btn_ai_visual_inspection_prominent"),
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = Slate900, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Slate900, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Visual Inspection", color = Slate900, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = { showCreateNotifDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("action_btn_create_notification_prominent"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Notification", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Clickable Maintenance Activity Summary Cards (Requirement 7)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    title = "Open WO",
                    value = "${uiState.summary.openWorkOrdersCount}",
                    color = if (uiState.summary.openWorkOrdersCount > 0) StatusOrange else StatusGreen,
                    modifier = Modifier.weight(1f).testTag("summary_card_wo"),
                    onClick = {
                        selectedMainTab = 1 // Activities
                        activitiesSubTab = 0 // Work Orders
                    }
                )
                SummaryCard(
                    title = "Open Notif",
                    value = "${uiState.summary.openNotificationsCount}",
                    color = if (uiState.summary.openNotificationsCount > 0) StatusRed else StatusGreen,
                    modifier = Modifier.weight(1f).testTag("summary_card_notif"),
                    onClick = {
                        selectedMainTab = 1 // Activities
                        activitiesSubTab = 1 // Notifications
                    }
                )
                SummaryCard(
                    title = "Next PM",
                    value = uiState.summary.nextPmDays,
                    color = SafetyAmber,
                    modifier = Modifier.weight(1f).testTag("summary_card_pm"),
                    onClick = {
                        selectedMainTab = 2 // Maintenance
                    }
                )
                SummaryCard(
                    title = "Downtime YTD",
                    value = "${uiState.summary.downtimeYtdHours}h",
                    color = StatusBlue,
                    modifier = Modifier.weight(1f).testTag("summary_card_downtime"),
                    onClick = {
                        selectedMainTab = 3 // History
                        historySubTab = 0 // Downtime
                    }
                )
            }

            // Quick Action Buttons Bar (Requirement 15)
            // Minimum touch targets 48dp, glove-friendly!
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "QUICK ACTIONS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    item {
                        ActionPill(
                            icon = Icons.Default.CameraAlt,
                            label = "AI Visual Inspection",
                            highlight = true,
                            testTag = "action_btn_ai_visual_inspection_pill",
                            onClick = { onNavigateToVisualInspection(eq.equipmentId) }
                        )
                    }
                    item {
                        ActionPill(
                            icon = Icons.Default.Notifications,
                            label = "Create Notification",
                            highlight = false,
                            testTag = "action_btn_create_notification",
                            onClick = { showCreateNotifDialog = true }
                        )
                    }
                    item {
                        ActionPill(
                            icon = Icons.Default.Speed,
                            label = "Start Inspection",
                            testTag = "action_btn_start_inspection",
                            onClick = { showStartInspDialog = true }
                        )
                    }
                    item {
                        ActionPill(
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            label = "View PM Checklist",
                            testTag = "action_btn_view_checklist",
                            onClick = {
                                if (uiState.plans.isNotEmpty()) {
                                    selectedPmPlanForChecklist = uiState.plans.first()
                                } else {
                                    selectedMainTab = 2
                                }
                            }
                        )
                    }
                    item {
                        ActionPill(
                            icon = Icons.Default.CameraAlt,
                            label = "Add Photo",
                            testTag = "action_btn_add_photo",
                            onClick = {
                                showCreateNotifDialog = true
                            }
                        )
                    }
                    item {
                        ActionPill(
                            icon = Icons.Default.AutoAwesome,
                            label = "AI Assistant",
                            testTag = "action_btn_ai_assistant",
                            onClick = {
                                selectedMainTab = 5 // Switch to AI Assistant tab
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Navigation Tab Row (Requirement 22)
            ScrollableTabRow(
                selectedTabIndex = selectedMainTab,
                containerColor = Slate800,
                contentColor = SafetyAmber,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedMainTab]),
                        color = SafetyAmber,
                        height = 3.dp
                    )
                }
            ) {
                mainTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedMainTab == index,
                        onClick = { selectedMainTab = index },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("main_tab_$title"),
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedMainTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedMainTab == index) SafetyAmber else Color(0xFFCBD5E1)
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedMainTab) {
                0 -> OverviewSection(equipment = eq)
                1 -> ActivitiesSection(
                    workOrders = uiState.workOrders,
                    notifications = uiState.notifications,
                    inspections = uiState.inspections,
                    initialSubTab = activitiesSubTab,
                    onSelectWorkOrder = { selectedWorkOrderForDetail = it },
                    onCreateNotification = { showCreateNotifDialog = true },
                    onStartInspection = { showStartInspDialog = true }
                )
                2 -> MaintenanceSection(
                    plans = uiState.plans,
                    onOpenChecklist = { selectedPmPlanForChecklist = it }
                )
                3 -> HistorySection(
                    downtimes = uiState.downtimes,
                    spareParts = uiState.spareParts,
                    initialSubTab = historySubTab
                )
                4 -> DocumentsSection(
                    documents = uiState.documents,
                    onSelectDocument = { selectedDocForPreview = it }
                )
                5 -> AiAssistantSection(
                    equipment = eq,
                    workOrders = uiState.workOrders,
                    notifications = uiState.notifications,
                    plans = uiState.plans,
                    inspections = uiState.inspections,
                    downtimes = uiState.downtimes,
                    spareParts = uiState.spareParts,
                    onRequestCreateNotification = { showCreateNotifDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Dialogs
        if (showCreateNotifDialog) {
            CreateNotificationDialog(
                equipmentId = eq.equipmentId,
                equipmentName = eq.name,
                onDismiss = { showCreateNotifDialog = false },
                onSubmit = { notif ->
                    viewModel.createNotification(notif)
                    showCreateNotifDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Notification ${notif.notificationNumber} created successfully")
                    }
                }
            )
        }

        if (showStartInspDialog) {
            StartInspectionDialog(
                equipmentId = eq.equipmentId,
                equipmentName = eq.name,
                onDismiss = { showStartInspDialog = false },
                onSubmit = { insp ->
                    viewModel.recordInspection(insp)
                    showStartInspDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Inspection record saved with result: ${insp.result}")
                    }
                }
            )
        }

        if (selectedPmPlanForChecklist != null) {
            PmChecklistDialog(
                plan = selectedPmPlanForChecklist!!,
                equipmentId = eq.equipmentId,
                onDismiss = { selectedPmPlanForChecklist = null },
                onComplete = { updated ->
                    viewModel.updatePlan(updated)
                    selectedPmPlanForChecklist = null
                    scope.launch {
                        snackbarHostState.showSnackbar("PM walkdown checklist signed off!")
                    }
                }
            )
        }

        if (selectedWorkOrderForDetail != null) {
            WorkOrderDetailDialog(
                workOrder = selectedWorkOrderForDetail!!,
                onDismiss = { selectedWorkOrderForDetail = null },
                onUpdateStatus = { wo, newStatus ->
                    viewModel.updateWorkOrderStatus(wo, newStatus)
                    selectedWorkOrderForDetail = null
                    scope.launch {
                        snackbarHostState.showSnackbar("Work Order ${wo.orderNumber} updated to $newStatus")
                    }
                }
            )
        }

        if (selectedDocForPreview != null) {
            DocumentPreviewDialog(
                document = selectedDocForPreview!!,
                onDismiss = { selectedDocForPreview = null }
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun ActionPill(
    icon: ImageVector,
    label: String,
    highlight: Boolean = false,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .background(if (highlight) SafetyAmber else Slate800, RoundedCornerShape(24.dp))
            .border(1.dp, if (highlight) SafetyAmber else Slate700, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (highlight) Slate900 else SafetyAmber,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (highlight) Slate900 else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
