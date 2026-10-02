package com.example.ui.screens.dashboard

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.EquipmentEntity
import com.example.data.repository.IConfirmationRepository
import com.example.data.repository.IEquipmentRepository
import com.example.ui.components.ActiveJobBanner
import com.example.ui.components.StatusBadge
import com.example.ui.components.WorkCenterBadge
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: IEquipmentRepository,
    confirmationRepository: IConfirmationRepository? = null,
    onNavigateToScanner: () -> Unit,
    onNavigateToEquipment: (String) -> Unit,
    onNavigateToVisualInspection: (String) -> Unit = {},
    onNavigateToMyWork: () -> Unit = {},
    onNavigateToConfirmWork: (String) -> Unit = {}
) {
    val allEquipments by repository.getAllEquipments().collectAsStateWithLifecycle(initialValue = emptyList())
    val activeJob = confirmationRepository?.getActiveJob()?.collectAsStateWithLifecycle(initialValue = null)?.value

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val categoryFilters = listOf("All", "High Criticality", "Crushing", "Conveyors", "Active")

    val filteredList = remember(allEquipments, searchQuery, selectedCategoryFilter) {
        allEquipments.filter { eq ->
            val matchesQuery = searchQuery.isBlank() ||
                    eq.equipmentId.contains(searchQuery, ignoreCase = true) ||
                    eq.name.contains(searchQuery, ignoreCase = true) ||
                    eq.functionalLocation.contains(searchQuery, ignoreCase = true) ||
                    eq.area.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategoryFilter) {
                "High Criticality" -> eq.criticality in listOf("CRITICAL", "HIGH")
                "Crushing" -> eq.area.contains("Crushing", ignoreCase = true) || eq.equipmentId in listOf("121SC008", "CRU01", "AF01")
                "Conveyors" -> eq.name.contains("Conveyor", ignoreCase = true) || eq.equipmentId.startsWith("CV")
                "Active" -> eq.status == "ACTIVE"
                else -> true
            }

            matchesQuery && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(SafetyAmber, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Factory,
                                contentDescription = null,
                                tint = Slate900,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PLANTCARE AI",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Asset Maintenance Intelligence",
                                fontSize = 11.sp,
                                color = SafetyAmber
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToMyWork,
                        modifier = Modifier.testTag("topbar_my_work_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = "My Work",
                            tint = SafetyAmber
                        )
                    }
                    IconButton(
                        onClick = onNavigateToScanner,
                        modifier = Modifier.testTag("topbar_scan_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Equipment",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToScanner,
                containerColor = SafetyAmber,
                contentColor = Slate900,
                modifier = Modifier
                    .size(60.dp)
                    .testTag("fab_scan_equipment")
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan Equipment",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = Slate900
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Active Job Banner if present
            if (activeJob != null) {
                item {
                    val diffSec = maxOf(0L, (System.currentTimeMillis() - activeJob.startTimestamp) / 1000)
                    val m = (diffSec % 3600) / 60
                    val s = diffSec % 60
                    val timerStr = String.format(java.util.Locale.US, "%02d:%02d", m, s)

                    ActiveJobBanner(
                        activeJob = activeJob,
                        elapsedTimer = timerStr,
                        onOpenJob = { onNavigateToConfirmWork(activeJob.workOrder) }
                    )
                }
            }

            // Hero Scan Card (Prominent Call to Action)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_scan_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SafetyAmber.copy(alpha = 0.8f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(SafetyAmber, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = Slate900,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "FAST ASSET RECOGNITION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SafetyAmber,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Scan Equipment QR / Barcode",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Point camera at any equipment tag to immediately open Equipment 360°, inspect work orders, report anomalies, and access PM procedures.",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onNavigateToScanner,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("scan_equipment_primary_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = Slate900)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Scan Equipment",
                                color = Slate900,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Industrial KPI Counters
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activeCount = allEquipments.count { it.status == "ACTIVE" }
                    val criticalCount = allEquipments.count { it.criticality in listOf("CRITICAL", "HIGH") }

                    KpiCard(title = "Total Assets", count = "${allEquipments.size}", color = Color.White, modifier = Modifier.weight(1f))
                    KpiCard(title = "Operational", count = "$activeCount", color = StatusGreen, modifier = Modifier.weight(1f))
                    KpiCard(title = "Critical High", count = "$criticalCount", color = SafetyAmber, modifier = Modifier.weight(1f))
                }
            }

            // Search Equipment Header & Bar (Requirement 25)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "EQUIPMENT DIRECTORY & MANUAL SEARCH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("equipment_search_input"),
                        placeholder = { Text("Search by ID, Name, Functional Location...", color = Color(0xFF64748B), fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = SafetyAmber)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SafetyAmber,
                            unfocusedBorderColor = Slate700,
                            focusedContainerColor = Slate800,
                            unfocusedContainerColor = Slate800
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoryFilters) { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        Box(
                            modifier = Modifier
                                .background(if (isSelected) SafetyAmber else Slate800, RoundedCornerShape(16.dp))
                                .border(1.dp, if (isSelected) SafetyAmber else Slate700, RoundedCornerShape(16.dp))
                                .clickable { selectedCategoryFilter = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("cat_filter_$cat")
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Slate900 else Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Results count
            item {
                Text(
                    text = "${filteredList.size} equipment found",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }

            // Equipment Item Cards
            items(filteredList, key = { it.equipmentId }) { eq ->
                EquipmentCard(
                    equipment = eq,
                    onOpen360 = { onNavigateToEquipment(eq.equipmentId) },
                    onVisualInspect = { onNavigateToVisualInspection(eq.equipmentId) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(68.dp),
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
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = count,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun EquipmentCard(
    equipment: EquipmentEntity,
    onOpen360: () -> Unit,
    onVisualInspect: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen360() }
            .testTag("equipment_card_${equipment.equipmentId}"),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = equipment.equipmentId,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafetyAmber
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(status = equipment.status)
                }
                StatusBadge(status = equipment.criticality)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = equipment.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = equipment.functionalLocation,
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${equipment.area} • ${equipment.workCenter}",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onVisualInspect,
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("inspect_btn_${equipment.equipmentId}"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyAmber),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Inspect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onOpen360,
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("open_360_${equipment.equipmentId}"),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Equipment 360°", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
