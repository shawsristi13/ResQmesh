package com.meshmap.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meshmap.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

import androidx.lifecycle.viewmodel.compose.viewModel
import com.meshmap.app.viewmodel.MeshViewModel

@Composable
fun MeshMapScreen(viewModel: MeshViewModel = viewModel()) {
    val connectedPeersList by viewModel.connectedPeers.collectAsState()
    val connectedPeerCount = connectedPeersList.size

    // Rotation animation for the radar sweep effect
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        // Header
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
                        "Mesh Network",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        "$connectedPeerCount node${if (connectedPeerCount != 1) "s" else ""} in mesh",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (connectedPeerCount > 0) SafetyGreen else TextDim
                    )
                }

                IconButton(onClick = { viewModel.restartMesh() }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                        contentDescription = "Restart Scan",
                        tint = IcyBlue
                    )
                }
            }
        }

        // Canvas mesh visualization
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val maxRadius = minOf(centerX, centerY) * 0.85f

                // Draw range rings
                for (i in 1..3) {
                    val ringRadius = maxRadius * i / 3
                    drawCircle(
                        color = EdgeColor.copy(alpha = 0.3f),
                        radius = ringRadius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 1f)
                    )
                }

                // Draw radar sweep line
                val sweepRad = Math.toRadians(sweepAngle.toDouble())
                val sweepEndX = centerX + (maxRadius * cos(sweepRad)).toFloat()
                val sweepEndY = centerY + (maxRadius * sin(sweepRad)).toFloat()
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            IcyBlue.copy(alpha = 0.6f),
                            IcyBlue.copy(alpha = 0f)
                        ),
                        start = Offset(centerX, centerY),
                        end = Offset(sweepEndX, sweepEndY)
                    ),
                    start = Offset(centerX, centerY),
                    end = Offset(sweepEndX, sweepEndY),
                    strokeWidth = 2f
                )

                if (connectedPeerCount > 0) {
                    val angleStep = 360.0 / connectedPeerCount
                    connectedPeersList.forEachIndexed { index, peer ->
                        // Alternate distances between 0.4 and 0.8 to make it look scattered
                        val distFraction = 0.4f + (index % 3) * 0.2f
                        val angleDeg = index * angleStep
                        val color = NodeConnected

                        val rad = Math.toRadians(angleDeg)
                        val nodeX = centerX + (maxRadius * distFraction * cos(rad)).toFloat()
                        val nodeY = centerY + (maxRadius * distFraction * sin(rad)).toFloat()

                        // Draw edge to center
                        drawLine(
                            color = EdgeColor,
                            start = Offset(centerX, centerY),
                            end = Offset(nodeX, nodeY),
                            strokeWidth = 2f
                        )

                        // Draw node
                        drawCircle(
                            color = color,
                            radius = 12f,
                            center = Offset(nodeX, nodeY)
                        )
                        drawCircle(
                            color = color.copy(alpha = 0.3f),
                            radius = 20f,
                            center = Offset(nodeX, nodeY)
                        )
                    }
                }

                // Draw "YOU" center node
                drawCircle(
                    color = NodeSelf,
                    radius = 16f,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = NodeSelf.copy(alpha = 0.2f),
                    radius = 28f,
                    center = Offset(centerX, centerY)
                )
            }

            // "YOU" label overlay
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = 30.dp)
            ) {
                Text(
                    "YOU",
                    style = MaterialTheme.typography.labelSmall,
                    color = IcyBlue,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }

        // Legend
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = NodeSelf, label = "You")
                LegendItem(color = NodeConnected, label = "Connected")
                LegendItem(color = NodeRecentlySeen, label = "Seen")
                LegendItem(color = NodeSOS, label = "SOS Active")
            }
        }
    }
}

@Composable
private fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
