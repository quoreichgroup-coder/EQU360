package com.example.ui.screens.mywork

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ActiveJobEntity
import com.example.data.model.WorkOrderEntity
import com.example.ui.components.ActiveJobBanner
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWorkScreen(
    viewModel: MyWorkViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToConfirmWork: (String) -> Unit,
    onNavigateToEquipment: (String) -> Unit,
    onNavigateToScanner: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Dialog when job is started automatically capturing start time
    uiState.jobStartFeedback?.let { startedJob ->
        JobStartedFeedbackDialog(
            activeJob = startedJob,
            onDismiss = { viewModel.dismissJobStartFeedback() },
            onConfirmNow = {
                viewModel.dismissJobStartFeedback()
                onNavigateToConfirmWork(startedJob.workOrder)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(SafetyAmber, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = Slate900,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MY WORK",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Technician Field Execution",
                                fontSize = 11.sp,
                                color = SafetyAmber
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("my_work_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.triggerSync() }, modifier = Modifier.testTag("sync_btn")) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync", tint = SafetyAmber)
                    }
                    IconButton(onClick = onNavigateToScanner, modifier = Modifier.testTag("scanner_btn")) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        containerColor = Slate900
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Persistent Active Job banner if a job is in progress
            uiState.activeJob?.let { active ->
                ActiveJobBanner(
                    activeJob = active,
                    elapsedTimer = uiState.elapsedTimerString,
                    onOpenJob = { onNavigateToConfirmWork(active.workOrder) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Sync message toast / banner
            uiState.syncMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SafetyAmber)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
            }

            // Category sections tabs: Today, Upcoming, In Progress, Waiting, Completed
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = Slate900,
                contentColor = SafetyAmber,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab.ordinal]),
                        color = SafetyAmber,
                        height = 3.dp
                    )
                }
            ) {
                MyWorkTab.values().forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}"),
                        text = {
                            Text(
                                text = tab.label,
                                fontSize = 13.sp,
                                fontWeight = if (uiState.selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.selectedTab == tab) SafetyAmber else Color(0xFF94A3B8)
                            )
                        }
                    )
                }
            }

            // Cards list (optimized for phone UX - no dense desktop tables)
            if (uiState.workOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No work orders in '${uiState.selectedTab.label}'",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.workOrders, key = { it.orderNumber }) { wo ->
                        val isCurrentActive = uiState.activeJob?.workOrder == wo.orderNumber

                        WorkOrderCard(
                            workOrder = wo,
                            isActive = isCurrentActive,
                            onStartJob = { viewModel.startJob(wo) },
                            onConfirmWork = { onNavigateToConfirmWork(wo.orderNumber) },
                            onOpenEquipment = { onNavigateToEquipment(wo.equipmentId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkOrderCard(
    workOrder: WorkOrderEntity,
    isActive: Boolean,
    onStartJob: () -> Unit,
    onConfirmWork: () -> Unit,
    onOpenEquipment: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("work_order_card_${workOrder.orderNumber}"),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp),
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, SafetyAmber) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: WO number + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workOrder.orderNumber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                StatusBadge(status = workOrder.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Work Description
            Text(
                text = workOrder.description,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF1F5F9),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Asset & Functional Location
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenEquipment() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workOrder.equipmentId,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafetyAmber
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "•",
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BI-PLN-CRU/CRS-003",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Work center • planned hours
            Text(
                text = "${workOrder.workCenter} • 6.0 h",
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isActive || workOrder.status in listOf("REL", "PCNF")) {
                    Button(
                        onClick = onConfirmWork,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_confirm_work_${workOrder.orderNumber}"),
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "CONFIRM WORK",
                            color = Slate900,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                if (!isActive && workOrder.status == "REL") {
                    OutlinedButton(
                        onClick = onStartJob,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_start_job_${workOrder.orderNumber}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = SafetyAmber)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "START JOB",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JobStartedFeedbackDialog(
    activeJob: ActiveJobEntity,
    onDismiss: () -> Unit,
    onConfirmNow: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Slate900,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("job_started_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(SafetyAmber, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Slate900,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Job Started",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = activeJob.startTime,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SafetyAmber
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${activeJob.equipmentId}\n${activeJob.equipmentName}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = activeJob.workOrder,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onConfirmNow,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Proceed to Job", color = Slate900, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Continue in Field", color = Color(0xFFCBD5E1))
                }
            }
        }
    }
}
