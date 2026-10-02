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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DowntimeEventEntity
import com.example.data.model.EquipmentEntity
import com.example.data.model.InspectionEntity
import com.example.data.model.MaintenanceNotificationEntity
import com.example.data.model.MaintenancePlanEntity
import com.example.data.model.MaterialConsumptionEntity
import com.example.data.model.WorkOrderEntity
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "AI" or "USER"
    val text: String,
    val timestamp: String = "Just now",
    val actionButtonText: String? = null
)

@Composable
fun AiAssistantSection(
    equipment: EquipmentEntity,
    workOrders: List<WorkOrderEntity>,
    notifications: List<MaintenanceNotificationEntity>,
    plans: List<MaintenancePlanEntity>,
    inspections: List<InspectionEntity>,
    downtimes: List<DowntimeEventEntity>,
    spareParts: List<MaterialConsumptionEntity>,
    onRequestCreateNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var userPrompt by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "AI",
                text = "Hello! I am PlantCare AI Diagnostics for ${equipment.equipmentId} (${equipment.name}). I have loaded all active work orders, notifications, PM strategies, downtime logs, and spare parts. How can I assist you with maintenance on this asset?"
            )
        )
    }

    val quickQuestions = listOf(
        "Summarize the maintenance history of this equipment.",
        "What open work orders exist for this equipment?",
        "What problems occur most frequently?",
        "When is the next preventive maintenance?",
        "Summarize the downtime history.",
        "Which spare parts have been frequently used?",
        "Create a maintenance notification from my observation."
    )

    fun answerQuery(query: String) {
        messages.add(ChatMessage(sender = "USER", text = query))
        isThinking = true

        scope.launch {
            delay(400) // Brief natural pause
            val responseText: String
            var actionBtn: String? = null

            val lower = query.lowercase()
            when {
                lower.contains("summarize the maintenance history") || lower.contains("history") && !lower.contains("downtime") -> {
                    val openWoCount = workOrders.count { it.status in listOf("CRTD", "REL", "PCNF") }
                    val totalDt = downtimes.sumOf { it.durationMinutes } / 60.0
                    val latestInsp = inspections.firstOrNull()?.result ?: "N/A"
                    responseText = "Maintenance Summary for ${equipment.name} (${equipment.equipmentId}):\n\n" +
                            "• Status: ${equipment.status} (Criticality: ${equipment.criticality})\n" +
                            "• Active Work Orders: $openWoCount open/in-progress out of ${workOrders.size} total.\n" +
                            "• Recent Inspection: Rated $latestInsp on ${inspections.firstOrNull()?.inspectionDate ?: "N/A"}.\n" +
                            "• Total Downtime YTD: $totalDt hours recorded over ${downtimes.size} events.\n" +
                            "• Primary Maintenance Focus: Drive eccentric balance, deck bar liner wear, and vibration harmonics."
                }
                lower.contains("open work order") || lower.contains("work order") -> {
                    val open = workOrders.filter { it.status in listOf("CRTD", "REL", "PCNF") }
                    if (open.isEmpty()) {
                        responseText = "There are currently NO open work orders for ${equipment.equipmentId}."
                    } else {
                        val items = open.joinToString("\n") {
                            "• [${it.status}] ${it.orderNumber}: ${it.description} (Priority: ${it.priority}, Planned: ${it.plannedDate})"
                        }
                        responseText = "Found ${open.size} active work order(s) for ${equipment.equipmentId}:\n\n$items"
                    }
                }
                lower.contains("frequently") || lower.contains("common problem") -> {
                    responseText = "Frequent Failure Modes for ${equipment.name}:\n\n" +
                            "1. Boulder blockage / wedging between grizzly fingers (${downtimes.firstOrNull { it.cause.contains("blockage") }?.durationMinutes ?: 210} mins downtime).\n" +
                            "2. Drive bearing thermal spikes and vibration rise under rock surge.\n" +
                            "3. Deck liner plate impact wear on bars #2 and #3.\n" +
                            "4. Rubber isolator spring fatigue and slurry contamination."
                }
                lower.contains("preventive maintenance") || lower.contains("next pm") -> {
                    val next = plans.firstOrNull()
                    responseText = if (next != null) {
                        "Next Planned Maintenance for ${equipment.equipmentId}:\n\n" +
                                "• Plan: ${next.planName}\n" +
                                "• Item: ${next.maintenanceItem}\n" +
                                "• Task List: ${next.taskList} (${next.frequency})\n" +
                                "• Scheduled Date: ${next.nextPlannedDate} (Last executed: ${next.lastExecution})\n" +
                                "• Scope: ${next.checklistItemsRaw.split(";").size} inspection verification points."
                    } else {
                        "No active PM plans configured for this equipment."
                    }
                }
                lower.contains("downtime") -> {
                    val totalMinutes = downtimes.sumOf { it.durationMinutes }
                    val hours = totalMinutes / 60.0
                    val log = downtimes.joinToString("\n") {
                        "• ${it.startDateTime}: ${it.durationMinutes}m [${it.downtimeType}] - ${it.cause}"
                    }
                    responseText = "Downtime Record for ${equipment.equipmentId}:\n\n" +
                            "Total YTD Downtime: $hours hours across ${downtimes.size} events.\n\nRecent incidents:\n$log"
                }
                lower.contains("spare parts") || lower.contains("material") -> {
                    val parts = spareParts.joinToString("\n") {
                        "• ${it.materialNumber}: ${it.materialDescription} (${it.quantity.toInt()} ${it.unit} from ${it.storageLocation})"
                    }
                    responseText = "Frequently Consumed Spare Parts for ${equipment.equipmentId} (SAP MM):\n\n$parts"
                }
                lower.contains("create") && lower.contains("notification") || lower.contains("observation") -> {
                    responseText = "I have extracted the context for equipment ${equipment.equipmentId} (${equipment.name}). Click the button below to launch the pre-populated notification form:"
                    actionBtn = "Open Notification Form"
                }
                else -> {
                    responseText = "Based on local telemetry for ${equipment.equipmentId} (${equipment.name}):\n\n" +
                            "• Functional Location: ${equipment.functionalLocation}\n" +
                            "• Work Center: ${equipment.workCenter}\n" +
                            "• Active Notifications: ${notifications.count { it.status == "OPEN" }} open.\n\n" +
                            "You can ask me to summarize work orders, inspect PM task lists, view downtime trends, or structure a new notification."
                }
            }

            isThinking = false
            messages.add(ChatMessage(sender = "AI", text = responseText, actionButtonText = actionBtn))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // AI Context Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF6366F1), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "PLANTCARE AI DIAGNOSTICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA5B4FC)
                    )
                    Text(
                        text = "Trained on ${equipment.equipmentId} history & OEM manuals",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        // Quick Suggestion Chips
        Text(
            text = "Suggested Inquiries:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(quickQuestions) { q ->
                Box(
                    modifier = Modifier
                        .background(Slate800, RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { answerQuery(q) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("ai_chip_${q.take(15)}")
                ) {
                    Text(
                        text = q,
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Message Thread
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            messages.forEach { msg ->
                val isAi = msg.sender == "AI"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAi) Slate800 else Color(0xFF0F766E)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isAi) "PlantCare AI" else "Technician",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAi) SafetyAmber else Color(0xFF99F6E4)
                                )
                                Text(msg.timestamp, fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                color = Color.White,
                                lineHeight = 18.sp
                            )
                            if (msg.actionButtonText != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = onRequestCreateNotification,
                                    colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("ai_launch_notification_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = Slate900)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(msg.actionButtonText, color = Slate900, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            if (isThinking) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = SafetyAmber
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyzing telemetry & logs...", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_prompt_input"),
                placeholder = { Text("Ask about ${equipment.equipmentId}...", color = Color(0xFF64748B), fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SafetyAmber,
                    unfocusedBorderColor = Slate700,
                    focusedContainerColor = Slate800,
                    unfocusedContainerColor = Slate800
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    val q = userPrompt.trim()
                    if (q.isNotBlank()) {
                        userPrompt = ""
                        answerQuery(q)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(SafetyAmber, CircleShape)
                    .testTag("ai_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Slate900
                )
            }
        }
    }
}
