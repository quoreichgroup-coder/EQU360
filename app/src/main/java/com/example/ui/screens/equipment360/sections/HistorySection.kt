package com.example.ui.screens.equipment360.sections

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DowntimeEventEntity
import com.example.data.model.MaterialConsumptionEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusRed

@Composable
fun HistorySection(
    downtimes: List<DowntimeEventEntity>,
    spareParts: List<MaterialConsumptionEntity>,
    initialSubTab: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab) }
    val totalDowntimeMinutes = downtimes.sumOf { it.durationMinutes }
    val totalHours = (totalDowntimeMinutes / 60.0 * 10).toInt() / 10.0

    Column(modifier = modifier.fillMaxWidth()) {
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
            listOf("Downtime Events (${downtimes.size})", "Spare Parts (${spareParts.size})").forEachIndexed { idx, title ->
                Tab(
                    selected = selectedSubTab == idx,
                    onClick = { selectedSubTab = idx },
                    modifier = Modifier.testTag("history_subtab_$idx"),
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedSubTab == idx) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedSubTab == idx) SafetyAmber else Color(0xFF94A3B8)
                        )
                    }
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            when (selectedSubTab) {
                0 -> {
                    // Downtime Summary Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF451A03)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(StatusRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.HourglassBottom, contentDescription = null, tint = StatusRed)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "TOTAL DOWNTIME YTD",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFED7AA)
                                )
                                Text(
                                    text = "$totalHours hours (${downtimes.size} events)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    downtimes.forEach { dt ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
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
                                        text = "${dt.durationMinutes / 60}h ${dt.durationMinutes % 60}m",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusRed
                                    )
                                    StatusBadge(status = dt.downtimeType)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Cause: ${dt.cause}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = dt.comment,
                                    fontSize = 12.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${dt.startDateTime} → ${dt.endDateTime}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    if (dt.relatedWorkOrder != null) {
                                        Text(
                                            text = "WO: ${dt.relatedWorkOrder}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SafetyAmber
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Spare Parts Sub-section
                    Text(
                        text = "MATERIAL CONSUMPTION (SAP MM READY)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    spareParts.forEach { part ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
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
                                        text = part.materialNumber,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SafetyAmber
                                    )
                                    Text(
                                        text = "${part.quantity.toInt()} ${part.unit}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = part.materialDescription,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Inventory, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(part.storageLocation, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                    Text("Consumed: ${part.consumptionDate}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
