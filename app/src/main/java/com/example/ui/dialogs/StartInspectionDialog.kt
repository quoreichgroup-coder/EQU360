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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.InspectionEntity
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StartInspectionDialog(
    equipmentId: String,
    equipmentName: String,
    onDismiss: () -> Unit,
    onSubmit: (InspectionEntity) -> Unit
) {
    var inspectionType by remember { mutableStateOf("Walkdown Operator Round") }
    var resultStatus by remember { mutableStateOf("OK") }
    var tempMeasurement by remember { mutableStateOf("62.5") }
    var vibrationMeasurement by remember { mutableStateOf("3.8") }
    var observations by remember { mutableStateOf("") }
    var anomalies by remember { mutableStateOf("") }

    val checkItems = remember {
        mutableStateListOf(
            "Mechanical mounting and structural bolts tight" to true,
            "Bearing lubricant level and sight glass clean" to true,
            "Motor and coupling alignment within tolerance" to true,
            "Safety guards, pull-wires and covers secured" to true
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("start_inspection_dialog"),
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
                            text = "Record Equipment Inspection",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$equipmentId - $equipmentName",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SafetyAmber
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Inspection Overall Result Selector
                Text(
                    text = "Overall Inspection Result",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("OK", "WARNING", "CRITICAL").forEach { res ->
                        val isSelected = resultStatus == res
                        val bgCol = when (res) {
                            "OK" -> if (isSelected) StatusGreen else Slate800
                            "WARNING" -> if (isSelected) SafetyAmber else Slate800
                            else -> if (isSelected) StatusRed else Slate800
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .background(bgCol, RoundedCornerShape(8.dp))
                                .border(1.dp, if (isSelected) Color.White.copy(alpha = 0.6f) else Slate700, RoundedCornerShape(8.dp))
                                .clickable { resultStatus = res }
                                .testTag("inspection_result_$res"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = res,
                                color = if (isSelected) (if (res == "WARNING") Slate900 else Color.White) else Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Checklist Points
                Text(
                    text = "Physical Verification Checks",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.height(6.dp))
                checkItems.indices.forEach { idx ->
                    val (label, checked) = checkItems[idx]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable { checkItems[idx] = label to !checked },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { checkItems[idx] = label to it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = SafetyAmber,
                                checkmarkColor = Slate900
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Telemetry Measurements (Temperature + Vibration)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bearing Temp (°C)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = tempMeasurement,
                            onValueChange = { tempMeasurement = it },
                            singleLine = true,
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
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vibration RMS (mm/s)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = vibrationMeasurement,
                            onValueChange = { vibrationMeasurement = it },
                            singleLine = true,
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
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Observations & Anomalies
                Text(
                    text = "Observations & Remarks",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = observations,
                    onValueChange = { observations = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    placeholder = { Text("E.g. Smooth rotation, normal operating sound", color = Color(0xFF64748B)) },
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

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Anomalies Detected (if any)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = anomalies,
                    onValueChange = { anomalies = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("E.g. Minor oil seepage around lower seal cap", color = Color(0xFF64748B)) },
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

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) {
                        Text("Cancel", color = Color(0xFFCBD5E1))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                            val inspection = InspectionEntity(
                                inspectionId = "INSP-${System.currentTimeMillis() % 1000000}",
                                equipmentId = equipmentId,
                                inspectionDate = dateStr,
                                technician = "Alex Mercer (Shift Mech)",
                                inspectionType = inspectionType,
                                result = resultStatus,
                                observations = observations.ifBlank { "Completed routine verification checks." },
                                measurements = "Temp: ${tempMeasurement}°C, Vibration: ${vibrationMeasurement} mm/s RMS",
                                anomaliesDetected = anomalies.ifBlank { "None detected." },
                                photos = null
                            )
                            onSubmit(inspection)
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("submit_inspection_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Inspection", color = Slate900, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
