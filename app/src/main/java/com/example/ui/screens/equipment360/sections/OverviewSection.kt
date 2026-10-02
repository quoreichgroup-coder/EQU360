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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EquipmentEntity
import com.example.ui.components.StatusBadge
import com.example.ui.components.WorkCenterBadge
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800

@Composable
fun OverviewSection(
    equipment: EquipmentEntity,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Equipment Identification Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EQUIPMENT MASTER SPECIFICATIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.8.sp
                    )
                    StatusBadge(status = equipment.status)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = equipment.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = equipment.description,
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(status = equipment.criticality)
                    WorkCenterBadge(workCenter = equipment.workCenter)
                }
            }
        }

        // Location & Hierarchy Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "HIERARCHY & FUNCTIONAL LOCATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                InfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Functional Location",
                    value = equipment.functionalLocation,
                    highlight = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                InfoRow(
                    icon = Icons.Default.Factory,
                    label = "Site",
                    value = equipment.site
                )
                Spacer(modifier = Modifier.height(10.dp))
                InfoRow(
                    icon = Icons.Default.Info,
                    label = "Plant Area",
                    value = equipment.area
                )
                Spacer(modifier = Modifier.height(10.dp))
                InfoRow(
                    icon = Icons.Default.Build,
                    label = "Maintenance Work Center",
                    value = equipment.workCenter
                )
            }
        }

        // OEM & Nameplate Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "NAMEPLATE & MANUFACTURER DATA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        InfoRow(icon = Icons.Default.Factory, label = "Manufacturer", value = equipment.manufacturer)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        InfoRow(icon = Icons.Default.Build, label = "Model", value = equipment.model)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        InfoRow(icon = Icons.Default.Numbers, label = "Serial Number", value = equipment.serialNumber)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        InfoRow(icon = Icons.Default.CalendarToday, label = "Commissioned", value = equipment.installationDate)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                InfoRow(
                    icon = Icons.Default.QrCode,
                    label = "Tagged QR Payload",
                    value = equipment.qrCodePayload
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(Slate700, RoundedCornerShape(6.dp))
                .padding(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (highlight) SafetyAmber else Color(0xFF94A3B8),
                modifier = Modifier.padding(2.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
                color = if (highlight) SafetyAmber else Color.White
            )
        }
    }
}
