package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MaintenancePlanEntity
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PmChecklistDialog(
    plan: MaintenancePlanEntity,
    equipmentId: String,
    onDismiss: () -> Unit,
    onComplete: (MaintenancePlanEntity) -> Unit
) {
    val items = remember {
        val raw = plan.checklistItemsRaw.split(";").filter { it.isNotBlank() }
        mutableStateListOf<Pair<String, Boolean>>().apply {
            raw.forEach { add(it to false) }
        }
    }

    val completedCount = items.count { it.second }
    val progress = if (items.isNotEmpty()) completedCount.toFloat() / items.size else 1f

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("pm_checklist_dialog"),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = plan.planName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Equipment: $equipmentId • Task List: ${plan.taskList}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SafetyAmber
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "CHECKLIST PROGRESS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "$completedCount / ${items.size} Completed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (completedCount == items.size) StatusGreen else SafetyAmber
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = if (completedCount == items.size) StatusGreen else SafetyAmber,
                            trackColor = Slate700
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Maintenance Verification Steps",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.height(8.dp))

                items.indices.forEach { idx ->
                    val (taskText, isDone) = items[idx]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(if (isDone) Color(0xFF0F2E24) else Slate800, RoundedCornerShape(8.dp))
                            .clickable { items[idx] = taskText to !isDone }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = { items[idx] = taskText to it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = StatusGreen,
                                checkmarkColor = Slate900
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = taskText,
                            color = if (isDone) Color.White else Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            fontWeight = if (isDone) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) {
                        Text("Close", color = Color(0xFFCBD5E1))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                            val updatedPlan = plan.copy(
                                lastExecution = today,
                                status = "COMPLETED"
                            )
                            onComplete(updatedPlan)
                        },
                        enabled = completedCount > 0,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("complete_pm_checklist_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Slate900)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign-Off Walkdown", color = Slate900, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
