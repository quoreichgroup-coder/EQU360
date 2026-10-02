package com.example.ui.dialogs

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MaintenanceNotificationEntity
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreateNotificationDialog(
    equipmentId: String,
    equipmentName: String,
    onDismiss: () -> Unit,
    onSubmit: (MaintenanceNotificationEntity) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("HIGH") }
    var damage by remember { mutableStateOf("") }
    var cause by remember { mutableStateOf("") }
    var observation by remember { mutableStateOf("") }
    var hasPhoto by remember { mutableStateOf(false) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceSummary by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val priorities = listOf("LOW", "MEDIUM", "HIGH", "VERY HIGH")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("create_notification_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Slate900,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "New Maintenance Notification",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Equipment: $equipmentId",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SafetyAmber
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Locked Equipment Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LOCKED EQUIPMENT ID",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "$equipmentId - $equipmentName",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0F766E), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AUTO-INHERITED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Problem Description
                Text(
                    text = "Problem Description *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        if (it.isNotBlank()) isError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notification_description_input"),
                    placeholder = { Text("E.g. Abnormal grinding noise on drive bearing", color = Color(0xFF64748B)) },
                    isError = isError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SafetyAmber,
                        unfocusedBorderColor = Slate700,
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                if (isError) {
                    Text(
                        text = "Description is required",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Priority Selector
                Text(
                    text = "Priority",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    priorities.forEach { p ->
                        val isSelected = priority == p
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(
                                    if (isSelected) SafetyAmber else Slate800,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) SafetyAmber else Slate700,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { priority = p }
                                .testTag("priority_chip_$p"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p,
                                color = if (isSelected) Slate900 else Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Damage & Cause
                Text(
                    text = "Damage Observed",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = damage,
                    onValueChange = { damage = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("E.g. Bearing race flaking, oil seal blowout", color = Color(0xFF64748B)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SafetyAmber,
                        unfocusedBorderColor = Slate700,
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Probable Cause (if known)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = cause,
                    onValueChange = { cause = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("E.g. Slurry contamination, rock impact surge", color = Color(0xFF64748B)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SafetyAmber,
                        unfocusedBorderColor = Slate700,
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Detailed Observation Notes
                Text(
                    text = "Technician Observations & Measurements",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = observation,
                    onValueChange = { observation = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("E.g. Temperature measured at 78°C, vibration RMS 6.2 mm/s", color = Color(0xFF64748B)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SafetyAmber,
                        unfocusedBorderColor = Slate700,
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Multimedia Attachment Tools (Photo + Voice Note)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { hasPhoto = !hasPhoto },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (hasPhoto) StatusGreen else Color(0xFFCBD5E1)
                        )
                    ) {
                        Icon(
                            imageVector = if (hasPhoto) Icons.Default.Check else Icons.Default.CameraAlt,
                            contentDescription = "Photo Attachment",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (hasPhoto) "Photo Attached" else "Add Photo", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            if (!isRecordingVoice) {
                                isRecordingVoice = true
                                voiceSummary = "Audio Note recorded: 14s (Transcribed: 'Drive end bearing housing is running hot, metallic grinding sound detected.')"
                            } else {
                                isRecordingVoice = false
                            }
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isRecordingVoice) SafetyAmber else Color(0xFFCBD5E1)
                        )
                    ) {
                        Icon(
                            imageVector = if (isRecordingVoice) Icons.Default.Mic else Icons.Default.MicNone,
                            contentDescription = "Voice Note",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isRecordingVoice) "Voice Attached" else "Voice Note", fontSize = 12.sp)
                    }
                }

                if (voiceSummary != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = voiceSummary!!,
                        fontSize = 11.sp,
                        color = SafetyAmber,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Cancel", color = Color(0xFFCBD5E1))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (description.isBlank()) {
                                isError = true
                                return@Button
                            }
                            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                            val newNotif = MaintenanceNotificationEntity(
                                notificationNumber = "NOTIF-${System.currentTimeMillis() % 1000000}",
                                equipmentId = equipmentId,
                                description = description.trim(),
                                priority = priority,
                                status = "OPEN",
                                creationDate = dateStr,
                                reportedBy = "Alex Mercer (Mobile Tech)",
                                damage = damage.ifBlank { "Unspecified mechanical wear" },
                                cause = cause.ifBlank { "Operational stress" },
                                observation = observation.ifBlank { "Identified during equipment scan walkdown" },
                                photoUri = if (hasPhoto) "content://photos/demo_anomaly.jpg" else null,
                                voiceNoteSummary = voiceSummary
                            )
                            onSubmit(newNotif)
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("submit_notification_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Submit Notification", color = Slate900, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
