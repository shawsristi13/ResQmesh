package com.meshmap.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meshmap.app.mesh.MeshMessage
import com.meshmap.app.ui.theme.*
import com.meshmap.app.viewmodel.MeshViewModel

@Composable
fun SosScreen(viewModel: MeshViewModel = viewModel()) {
    val connectedPeersList by viewModel.connectedPeers.collectAsState()
    val connectedPeerCount = connectedPeersList.size
    val sosAlerts by viewModel.sosAlerts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        // Status Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Emergency Mesh",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        if (connectedPeerCount > 0) "$connectedPeerCount connected peer${if (connectedPeerCount != 1) "s" else ""}" else "Searching for peers...",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (connectedPeerCount > 0) SafetyGreen else TextDim
                    )
                }
                
                // Small indicator dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (connectedPeerCount > 0) SafetyGreen else WarningAmber)
                )
            }
        }

        // SOS Button Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            PulsingSosButton(onClick = {
                viewModel.sendSosAlert()
            })
        }

        // Recent Alerts Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(DarkSurface, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(top = 16.dp)
        ) {
            Text(
                "Recent Alerts",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            if (sosAlerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No active alerts nearby.", color = TextDim)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sosAlerts.reversed()) { alert ->
                        AlertCard(alert)
                    }
                }
            }
        }
    }
}

@Composable
fun AlertCard(alert: MeshMessage) {
    val borderColor = if (alert.urgency >= 2) AlertRed else WarningAmber
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardSurface,
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alert.originName,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = borderColor.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Text(
                        text = if (alert.urgency >= 2) "CRITICAL" else "URGENT",
                        color = borderColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = alert.payload,
                color = TextPrimary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hops: ${alert.hopCount}",
                color = TextDim,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun PulsingSosButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer pulsing ring
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(AlertRed.copy(alpha = 0.2f))
        )
        // Inner pulsing ring
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(AlertRed.copy(alpha = 0.4f))
        )
        // Actual Button
        Surface(
            modifier = Modifier
                .size(130.dp)
                .clickable { onClick() },
            shape = CircleShape,
            color = AlertRed,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "SOS",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
