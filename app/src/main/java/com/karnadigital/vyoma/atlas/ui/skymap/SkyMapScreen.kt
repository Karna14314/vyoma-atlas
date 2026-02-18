package com.karnadigital.vyoma.atlas.ui.skymap

import android.Manifest
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.karnadigital.vyoma.atlas.ui.theme.CyanData
import com.karnadigital.vyoma.atlas.ui.theme.DeepCharcoalNavyFull
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SkyMapScreen(
    viewModel: SkyMapViewModel = hiltViewModel(),
    onClose: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val textMeasurer = rememberTextMeasurer()
    val context = LocalContext.current
    
    // Permission State
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCharcoalNavyFull)
            .onSizeChanged { size ->
                viewModel.updateScreenSize(size.width.toFloat(), size.height.toFloat())
            }
    ) {
        // Check if permission is granted
        if (!locationPermissionState.status.isGranted) {
            // Permission Not Granted - Show Glassmorphism Card
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.1f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.2f),
                            androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                        )
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = CyanData
                    )
                    Text(
                        text = "Location Permission Required",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "To show you the stars above your head, we need your location. We do not store this data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { locationPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanData,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Grant Permission", fontWeight = FontWeight.Bold)
                    }
                    
                    if (locationPermissionState.status.shouldShowRationale) {
                        TextButton(
                            onClick = {
                                // Open app settings
                                val intent = android.content.Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    android.net.Uri.fromParts("package", "com.karnadigital.vyoma.atlas", null)
                                )
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Open App Settings", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }
            
            // Close button
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(32.dp)
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        } else {
            // Permission Granted - Show Sky Map
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Crosshair
            val chColor = if (state.target?.isVisible == true) Color(0xFF00FF00) else Color.White.copy(alpha = 0.3f)
            val chSize = if (state.target?.isVisible == true) 60f else 40f
            
            drawLine(
                color = chColor,
                start = Offset(centerX - chSize, centerY),
                end = Offset(centerX + chSize, centerY),
                strokeWidth = 2f
            )
            drawLine(
                color = chColor,
                start = Offset(centerX, centerY - chSize),
                end = Offset(centerX, centerY + chSize),
                strokeWidth = 2f
            )

            // Compass Points
            state.compassPoints.forEach { point ->
                drawCircle(
                     color = Color.Red.copy(alpha = 0.5f),
                     radius = 4.dp.toPx(),
                     center = point.position
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = point.name,
                    topLeft = Offset(point.position.x - 20, point.position.y - 20),
                    style = TextStyle(color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
            }

            // Stars
            state.points.forEach { point ->
                val radius = 2.dp.toPx() + (point.brightness * 6.dp.toPx())
                
                drawCircle(
                    color = point.color.copy(alpha = 0.3f * point.brightness),
                    radius = radius * 2,
                    center = point.position
                )
                drawCircle(
                    color = point.color.copy(alpha = 0.9f),
                    radius = radius,
                    center = point.position
                )
                
                if (point.brightness > 0.8f) {
                     drawCircle(
                        color = CyanData,
                        radius = radius + 10f,
                        center = point.position,
                        style = Stroke(width = 1f)
                    )
                }
            }
            
            // Target
            val target = state.target
            if (target != null) {
                if (target.isVisible) {
                    val tRadius = 50f
                    drawCircle(
                        color = Color.Green,
                        radius = tRadius,
                        center = target.position,
                        style = Stroke(width = 4f)
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = target.objectName,
                        topLeft = Offset(target.position.x + 40, target.position.y - 40),
                        style = TextStyle(color = Color.Green, fontSize = 14.sp)
                    )
                } else {
                    val arrowPos = target.position
                    val angle = target.directionAngle ?: 0f
                    
                    rotate(degrees = angle, pivot = arrowPos) {
                        val path = Path().apply {
                            moveTo(arrowPos.x, arrowPos.y - 20f)
                            lineTo(arrowPos.x - 20f, arrowPos.y + 20f)
                            lineTo(arrowPos.x + 20f, arrowPos.y + 20f)
                            close()
                        }
                        drawPath(path, color = CyanData)
                    }
                    
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "Turn to ${target.objectName}",
                        topLeft = Offset(arrowPos.x - 100, arrowPos.y + 40),
                        style = TextStyle(color = CyanData, fontSize = 14.sp)
                    )
                }
            }
        }
        
        // UI Overlays
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(32.dp)
        ) {
             IconButton(onClick = onClose) {
                 Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
             }
        }
        
        if (state.target != null) {
            Button(
                onClick = { viewModel.setTarget(null) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.6f))
            ) {
                Text("Cancel Target")
            }
        } else {
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)) {
                Text(text = "Point at sky", color = Color.White.copy(alpha = 0.7f))
            }
        }
        }
    }
}
