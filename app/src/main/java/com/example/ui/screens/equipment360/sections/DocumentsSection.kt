package com.example.ui.screens.equipment360.sections

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TechnicalDocumentEntity
import com.example.ui.theme.SafetyAmber
import com.example.ui.theme.Slate800

@Composable
fun DocumentsSection(
    documents: List<TechnicalDocumentEntity>,
    onSelectDocument: (TechnicalDocumentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TECHNICAL DOCUMENTATION & MANUALS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                letterSpacing = 0.8.sp
            )
            Text(
                text = "${documents.size} Documents",
                fontSize = 12.sp,
                color = SafetyAmber,
                fontWeight = FontWeight.SemiBold
            )
        }

        documents.forEach { doc ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectDocument(doc) }
                    .testTag("document_card_${doc.documentId}"),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFF0F766E), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = doc.documentName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(doc.type, fontSize = 11.sp, color = SafetyAmber, fontWeight = FontWeight.Bold)
                            Text("•", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(doc.revision, fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("•", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(doc.date, fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = "Open Document",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
