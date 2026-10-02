package com.example.ui.screens.equipment360.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InspectionEntity
import com.example.data.model.MaintenanceNotificationEntity
import com.example.data.model.WorkOrderEntity
import com.example.ui.components.StatusBadge
import com.example.ui.components.WorkCenterBadge
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun ActivitiesSection(
    workOrders: List<WorkOrderEntity>,
    notifications: List<MaintenanceNotificationEntity>,
    inspections: List<InspectionEntity>,
    initialSubTab: Int = 0,
    onSelectWorkOrder: (WorkOrderEntity) -> Unit,
    onCreateNotification: () -> Unit,
    onStartInspection: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab) }
    val subTabs = listOf("Work Orders (${workOrders.size})", "Notifications (${notifications.size})", "Inspections (${inspections.size})")

    Column(modifier = modifier.fillMaxWidth()) {
        // Sub-navigation tab row
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = Slate900,
            contentColor = SafetyAmber,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = SafetyAmber,
                    height = 3.dp
                )
            }
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    modifier = Modifier.testTag("activities_subtab_$index"),
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedSubTab == index) SafetyAmber else Color(0xFF94A3B8)
                        )
                    }
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            when (selectedSubTab) {
                0 -> WorkOrdersSubSection(workOrders = workOrders, onSelectWorkOrder = onSelectWorkOrder)
                1 -> NotificationsSubSection(notifications = notifications, onCreateNotification = onCreateNotification)
                2 -> InspectionsSubSection(inspections = inspections, onStartInspection = onStartInspection)
            }
        }
    }
}

@Composable
fun WorkOrdersSubSection(
    workOrders: List<WorkOrderEntity>,
    onSelectWorkOrder: (WorkOrderEntity) -> Unit
) {
    var filterStatus by remember { mutableStateOf("All") }
    val filters = listOf("All", "Open", "In Progress", "Completed")

    val filteredList = remember(workOrders, filterStatus) {
        when (filterStatus) {
            "Open" -> workOrders.filter { it.status in listOf("CRTD", "REL", "PCNF") }
            "In Progress" -> workOrders.filter { it.status in listOf("REL", "PCNF") }
            "Completed" -> workOrders.filter { it.status in listOf("CNF", "TECO") }
            else -> workOrders
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { f ->
                val isSelected = filterStatus == f
                Box(
                    modifier = Modifier
                        .background(if (isSelected) SafetyAmber else Slate800, RoundedCornerShape(16.dp))
                        .border(1.dp, if (isSelected) SafetyAmber else Slate700, RoundedCornerShape(16.dp))
                        .clickable { filterStatus = f }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("wo_filter_$f")
                ) {
                    Text(
                        text = f,
                        color = if (isSelected) Slate900 else Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No work orders matching filter '$filterStatus'",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }
        } else {
            filteredList.forEach { wo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectWorkOrder(wo) }
                        .testTag("wo_item_${wo.orderNumber}"),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = wo.orderNumber,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafetyAmber
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusBadge(status = wo.status)
                                StatusBadge(status = wo.priority)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = wo.description,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(wo.plannedDate, fontSize = 12.sp, color = Color(0xFF94A3B8))
                            }
                            WorkCenterBadge(workCenter = wo.workCenter)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsSubSection(
    notifications: List<MaintenanceNotificationEntity>,
    onCreateNotification: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onCreateNotification,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("activities_create_notification_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Slate900)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Maintenance Notification", color = Slate900, fontWeight = FontWeight.Bold)
        }

        notifications.forEach { notif ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notification_item_${notif.notificationNumber}"),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notif.notificationNumber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafetyAmber
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatusBadge(status = notif.status)
                            StatusBadge(status = notif.priority)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = notif.description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Damage: ${notif.damage}",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )

                    Text(
                        text = "Observation: ${notif.observation}",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(notif.reportedBy, fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        Text(notif.creationDate, fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

@Composable
fun InspectionsSubSection(
    inspections: List<InspectionEntity>,
    onStartInspection: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onStartInspection,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("activities_start_inspection_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Slate900)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start New Inspection", color = Slate900, fontWeight = FontWeight.Bold)
        }

        inspections.forEach { insp ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inspection_item_${insp.inspectionId}"),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = insp.inspectionType,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        StatusBadge(status = insp.result)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = insp.observations,
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = insp.measurements,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SafetyAmber
                        )
                    }

                    if (insp.anomaliesDetected.isNotBlank() && insp.anomaliesDetected != "None detected.") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Anomalies: ${insp.anomaliesDetected}",
                            fontSize = 12.sp,
                            color = Color(0xFFF87171)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tech: ${insp.technician}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(insp.inspectionDate, fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}
