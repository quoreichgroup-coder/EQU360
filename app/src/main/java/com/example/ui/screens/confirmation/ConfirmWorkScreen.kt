package com.example.ui.screens.confirmation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.JobCompletionType
import com.example.ui.components.StatusBadge
import com.example.ui.components.WorkCenterBadge
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmWorkScreen(
    viewModel: ConfirmationViewModel,
    onNavigateBack: () -> Unit,
    onViewWorkOrder: (String) -> Unit,
    onNextJob: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddMaterialDialog by remember { mutableStateOf(false) }
    var showAddTeamDialog by remember { mutableStateOf(false) }

    if (showAddMaterialDialog) {
        AddMaterialDialog(
            onDismiss = { showAddMaterialDialog = false },
            onAdd = { desc, qty, unit ->
                viewModel.addMaterial(desc, qty, unit)
                showAddMaterialDialog = false
            }
        )
    }

    if (showAddTeamDialog) {
        AddTeamMemberDialog(
            onDismiss = { showAddTeamDialog = false },
            onAdd = { name, hours ->
                viewModel.addTeamMember(name, hours)
                showAddTeamDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (uiState.isSuccess) "CONFIRMATION SUCCESS" else if (uiState.isReviewMode) "REVIEW CONFIRMATION" else "CONFIRM WORK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafetyAmber,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${uiState.workOrder} • Op ${uiState.operation}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.isReviewMode) viewModel.setReviewMode(false) else onNavigateBack()
                        },
                        modifier = Modifier.testTag("confirm_work_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        containerColor = Slate900
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isSuccess -> {
                    SuccessScreen(
                        uiState = uiState,
                        onViewWorkOrder = { onViewWorkOrder(uiState.workOrder) },
                        onNextJob = onNextJob
                    )
                }

                uiState.isReviewMode -> {
                    ReviewScreen(
                        uiState = uiState,
                        onEdit = { viewModel.setReviewMode(false) },
                        onConfirmWork = { viewModel.submitConfirmation() },
                        isSubmitting = uiState.isSubmitting
                    )
                }

                else -> {
                    ConfirmationInputContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        onProceedToReview = { viewModel.setReviewMode(true) },
                        onShowAddMaterial = { showAddMaterialDialog = true },
                        onShowAddTeam = { showAddTeamDialog = true }
                    )
                }
            }
        }
    }
}

/**
 * Main 4-Section Technician Input Form.
 * Designed for 30-60 second completion in the field.
 */
@Composable
fun ConfirmationInputContent(
    uiState: ConfirmationUiState,
    viewModel: ConfirmationViewModel,
    onProceedToReview: () -> Unit,
    onShowAddMaterial: () -> Unit,
    onShowAddTeam: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Error banner if any
        uiState.errorMessage?.let { err ->
            Card(
                colors = CardDefaults.cardColors(containerColor = StatusRed.copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed)
            ) {
                Text(
                    text = err,
                    color = StatusRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // ================= SECTION 1: JOB (READ-ONLY CONTEXT) =================
        Card(
            modifier = Modifier.fillMaxWidth().testTag("section_job_context"),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. JOB CONTEXT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafetyAmber,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("WO", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Text(uiState.workOrder, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("OPERATION", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Text(uiState.operation, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SafetyAmber)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("EQUIPMENT", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                Text("${uiState.equipmentId} • ${uiState.equipmentName}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("FL: ${uiState.functionalLocation}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    WorkCenterBadge(workCenter = uiState.workCenter)
                }
            }
        }

        // ================= SECTION 2: TIME (START, FINISH, ELAPSED) =================
        Card(
            modifier = Modifier.fillMaxWidth().testTag("section_time"),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. TIME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafetyAmber,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start Time Box
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("WORK START", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            Text(uiState.workStartDate, fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = uiState.workStartTime,
                                onValueChange = { viewModel.updateStartTime(it) },
                                modifier = Modifier.fillMaxWidth().testTag("input_work_start_time"),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SafetyAmber,
                                    unfocusedBorderColor = Slate700
                                )
                            )
                        }
                    }

                    // Finish Time Box
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("WORK FINISH", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                            Text(uiState.workFinishDate, fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = uiState.workFinishTime,
                                onValueChange = { viewModel.updateFinishTime(it) },
                                modifier = Modifier.fillMaxWidth().testTag("input_work_finish_time"),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SafetyAmber,
                                    unfocusedBorderColor = Slate700
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Calculated Elapsed Time Banner (Clearly distinct from Actual Work)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Calculated Elapsed Time:", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                    }
                    Text(
                        text = uiState.formattedElapsedTime,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusGreen
                    )
                }
            }
        }

        // ================= SECTION 3: ACTUAL WORK =================
        Card(
            modifier = Modifier.fillMaxWidth().testTag("section_actual_work"),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, SafetyAmber.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3. ACTUAL WORK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafetyAmber,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Suggested from elapsed: ${uiState.suggestedWorkFromElapsed} h",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Large Numeric Input + Unit Toggle (H / MIN)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.actualWorkInput,
                        onValueChange = { viewModel.updateActualWork(it) },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .testTag("input_actual_work"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SafetyAmber,
                            textAlign = TextAlign.Center
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SafetyAmber,
                            unfocusedBorderColor = Slate700,
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900
                        )
                    )

                    // Unit Selector
                    Row(
                        modifier = Modifier
                            .height(64.dp)
                            .background(Slate900, RoundedCornerShape(8.dp))
                            .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(
                                    if (uiState.actualWorkUnit == "H") SafetyAmber else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.updateActualWorkUnit("H") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "H",
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.actualWorkUnit == "H") Slate900 else Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(
                                    if (uiState.actualWorkUnit == "MIN") SafetyAmber else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.updateActualWorkUnit("MIN") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "MIN",
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.actualWorkUnit == "MIN") Slate900 else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Multiple Technicians / Team Labor Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TEAM LABOR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "+ Add Technician",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafetyAmber,
                        modifier = Modifier
                            .clickable { onShowAddTeam() }
                            .testTag("btn_add_technician")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Primary Technician Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(uiState.technicianId, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("${uiState.actualWorkInput} ${uiState.actualWorkUnit}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SafetyAmber)
                }

                // Additional Team members
                uiState.teamLabor.forEachIndexed { index, member ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate900, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(member.technicianName, fontSize = 12.sp, color = Color(0xFFE2E8F0))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${member.hours} H", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SafetyAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = StatusRed,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { viewModel.removeTeamMember(index) }
                            )
                        }
                    }
                }
            }
        }

        // ================= SECTION 4: WORK PERFORMED =================
        Card(
            modifier = Modifier.fillMaxWidth().testTag("section_work_performed"),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "4. WORK PERFORMED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafetyAmber,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick checklist chips
                val options = listOf(
                    "Inspection completed",
                    "Lubrication completed",
                    "Adjustment performed",
                    "Component replaced",
                    "Cleaning performed"
                )

                options.forEach { task ->
                    val isChecked = uiState.performedTasks.contains(task)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.toggleTaskPerformed(task) }
                            .testTag("check_task_$task"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(
                                    if (isChecked) SafetyAmber else Slate900,
                                    RoundedCornerShape(4.dp)
                                )
                                .border(1.dp, if (isChecked) SafetyAmber else Slate700, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isChecked) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Slate900, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = task,
                            fontSize = 13.sp,
                            color = if (isChecked) Color.White else Color(0xFF94A3B8),
                            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Voice Dictation / Note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("WORK NOTE", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier
                            .background(if (uiState.isRecordingVoice) StatusRed else Slate900, RoundedCornerShape(16.dp))
                            .border(1.dp, if (uiState.isRecordingVoice) StatusRed else SafetyAmber, RoundedCornerShape(16.dp))
                            .clickable { viewModel.toggleVoiceRecording() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("btn_voice_record"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (uiState.isRecordingVoice) Color.White else SafetyAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.isRecordingVoice) "Listening..." else "Describe with voice",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isRecordingVoice) Color.White else SafetyAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = uiState.workNote,
                    onValueChange = { viewModel.updateWorkNote(it) },
                    modifier = Modifier.fillMaxWidth().testTag("input_work_note"),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafetyAmber,
                        unfocusedBorderColor = Slate700,
                        focusedContainerColor = Slate900,
                        unfocusedContainerColor = Slate900
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Color.White)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Contextual Measurements
                Text(
                    text = "MEASUREMENTS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.measurements.forEach { m ->
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(m.name, fontSize = 9.sp, color = Color(0xFF94A3B8), maxLines = 1)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(m.value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(m.unit, fontSize = 10.sp, color = SafetyAmber)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Materials Used
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MATERIALS USED", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Text(
                        text = "+ Add Material",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafetyAmber,
                        modifier = Modifier
                            .clickable { onShowAddMaterial() }
                            .testTag("btn_add_material")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                uiState.materialsUsed.forEach { mat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .background(Slate900, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(mat.description, fontSize = 12.sp, color = Color(0xFFE2E8F0))
                        Text("Qty ${mat.quantity} ${mat.unit}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SafetyAmber)
                    }
                }
            }
        }

        // ================= SECTION 5: COMPLETION =================
        Card(
            modifier = Modifier.fillMaxWidth().testTag("section_completion"),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "5. IS THE JOB COMPLETE?",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafetyAmber,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // YES Button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clickable { viewModel.setJobComplete(true) }
                            .testTag("choice_job_complete_yes"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.isJobComplete) StatusGreen else Slate900
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = if (!uiState.isJobComplete) androidx.compose.foundation.BorderStroke(1.dp, Slate700) else null
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "YES — Completed",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (uiState.isJobComplete) Slate900 else Color.White
                            )
                        }
                    }

                    // NO Button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clickable { viewModel.setJobComplete(false) }
                            .testTag("choice_job_complete_no"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!uiState.isJobComplete) StatusOrange else Slate900
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = if (uiState.isJobComplete) androidx.compose.foundation.BorderStroke(1.dp, Slate700) else null
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "NO — More Work",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (!uiState.isJobComplete) Slate900 else Color.White
                            )
                        }
                    }
                }

                // If NO, show Why? options
                if (!uiState.isJobComplete) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Why is more work required?", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusOrange)

                    Spacer(modifier = Modifier.height(8.dp))

                    val reasons = listOf(
                        "Waiting for spare parts",
                        "Additional work identified",
                        "Equipment unavailable",
                        "Specialist required",
                        "Tools unavailable",
                        "Shift ended",
                        "Other"
                    )

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        reasons.forEach { r ->
                            val isSel = uiState.incompleteReason == r
                            FilterChip(
                                selected = isSel,
                                onClick = { viewModel.setIncompleteReason(r) },
                                label = { Text(r, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StatusOrange,
                                    selectedLabelColor = Slate900,
                                    containerColor = Slate900,
                                    labelColor = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.incompleteNotes,
                        onValueChange = { viewModel.setIncompleteNotes(it) },
                        placeholder = { Text("Describe follow-up action required...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StatusOrange,
                            unfocusedBorderColor = Slate700,
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Large Primary Button to Proceed to Review
        Button(
            onClick = onProceedToReview,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_proceed_to_review"),
            colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "REVIEW & CONFIRM",
                color = Slate900,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Compact Pre-Submission Review Screen (Requirement 20).
 * Displays a clean summary of Job, Time, Actual Work, Work Performed, Result.
 */
@Composable
fun ReviewScreen(
    uiState: ConfirmationUiState,
    onEdit: () -> Unit,
    onConfirmWork: () -> Unit,
    isSubmitting: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("review_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, SafetyAmber)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "CONFIRM WORK",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafetyAmber,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "WO ${uiState.workOrder} • Operation ${uiState.operation}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "${uiState.equipmentId} • ${uiState.equipmentName}",
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = Slate700)
                Spacer(modifier = Modifier.height(14.dp))

                // Time Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Start", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(uiState.workStartTime, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Finish", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(uiState.workFinishTime, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Elapsed", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(uiState.formattedElapsedTime, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StatusGreen)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actual Work Highlight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Actual Work:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFCBD5E1))
                        Text(
                            text = "${uiState.actualWorkInput} ${uiState.actualWorkUnit}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SafetyAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = Slate700)
                Spacer(modifier = Modifier.height(14.dp))

                // Work Completed summary
                Text("Work completed", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                uiState.performedTasks.forEach { task ->
                    Text("• $task", fontSize = 13.sp, color = Color.White)
                }

                if (uiState.workNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Note: \"${uiState.workNote}\"", fontSize = 12.sp, color = Color(0xFF94A3B8), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = Slate700)
                Spacer(modifier = Modifier.height(14.dp))

                // Result status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Result", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Text(
                        text = if (uiState.isJobComplete) "WORK COMPLETED" else "MORE WORK REQUIRED",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isJobComplete) StatusGreen else StatusOrange
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("btn_edit_review"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("EDIT", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onConfirmWork,
                enabled = !isSubmitting,
                modifier = Modifier
                    .weight(1.5f)
                    .height(52.dp)
                    .testTag("btn_final_confirm_work"),
                colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Slate900, modifier = Modifier.size(22.dp))
                } else {
                    Text("CONFIRM WORK", color = Slate900, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Success Screen after Confirmation (Requirement 21 & 28).
 */
@Composable
fun SuccessScreen(
    uiState: ConfirmationUiState,
    onViewWorkOrder: () -> Unit,
    onNextJob: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("confirmation_success_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(StatusGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Slate900,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "✓ WORK CONFIRMED",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("WO ${uiState.workOrder}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Operation ${uiState.operation}", fontSize = 13.sp, color = SafetyAmber, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Actual Work", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Text(
                    text = "${uiState.actualWorkInput} ${uiState.actualWorkUnit}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SafetyAmber
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${uiState.workFinishDate} • ${uiState.workFinishTime}",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sync status indicator
                Box(
                    modifier = Modifier
                        .background(Slate900, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (uiState.submittedResult?.syncStatus == "SYNCED") "Synced with SAP PM" else "Saved locally • Waiting to sync",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.submittedResult?.syncStatus == "SYNCED") StatusGreen else SafetyAmber
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onViewWorkOrder,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_view_work_order"),
            colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("VIEW WORK ORDER", color = Slate900, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNextJob,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_next_job"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Text("NEXT JOB", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AddMaterialDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, String) -> Unit
) {
    var desc by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1.0") }
    var unit by remember { mutableStateOf("PC") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Slate900,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Add Material Used", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Material Description (e.g. Bearing 6208)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qty,
                        onValueChange = { qty = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (PC/KG/L)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val q = qty.toDoubleOrNull() ?: 1.0
                            if (desc.isNotBlank()) onAdd(desc, q, unit)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber)
                    ) {
                        Text("Add", color = Slate900, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddTeamMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("1.5") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Slate900,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Add Team Technician", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Technician Name (e.g. TECHNICIAN 02)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Allocated Hours") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val h = hours.toDoubleOrNull() ?: 1.0
                            if (name.isNotBlank()) onAdd(name, h)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber)
                    ) {
                        Text("Add", color = Slate900, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
