package com.karnadigital.vyoma.atlas.ui.detail

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.karnadigital.vyoma.atlas.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.karnadigital.vyoma.atlas.ui.viewmodel.DetailViewModel

import com.karnadigital.vyoma.atlas.ui.common.AstronomyImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun ObjectDetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onObjectClick: (String) -> Unit,
    onLocateClick: (String) -> Unit
) {
    // State for initial animation
    var isVisible by remember { mutableStateOf(false) }
    val astronomicalObject by viewModel.astronomicalObject.collectAsState()
    
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val children by viewModel.children.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SpaceGradient)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (astronomicalObject?.imageUrl != null) {
                    AstronomyImage(
                        imageUrl = astronomicalObject?.imageUrl,
                        objectName = astronomicalObject?.name ?: "Unknown",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay for text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                    startY = 200f
                                )
                            )
                    )
                } else {
                    Text(
                        text = astronomicalObject?.name ?: "LOADING...",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextSecondary.copy(alpha = 0.2f)
                    )
                }
            }

            // Animated Info Section
            val enterTransition = slideInVertically(
                initialOffsetY = { 100 }, 
                animationSpec = tween(500)
            ) + fadeIn(animationSpec = tween(500))

            AnimatedVisibility(
                visible = isVisible && astronomicalObject != null,
                enter = enterTransition,
                modifier = Modifier.padding(16.dp)
            ) {
                val obj = astronomicalObject
                if (obj != null) {
                    Column {
                        Text(
                            text = obj.name,
                            style = MaterialTheme.typography.displayMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = obj.type + (if (obj.constellation != null) " in ${obj.constellation}" else ""),
                            style = MaterialTheme.typography.titleMedium,
                            color = CyanData
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = obj.description ?: "No description available.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Data Grid
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DataCard(
                                label = "DISTANCE", 
                                value = if (obj.distanceAu != null && obj.distanceAu > 0) "${obj.distanceAu} AU" else "${obj.distanceLy} Ly", 
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            DataCard(
                                label = "RADIUS/SIZE", 
                                value = if (obj.radiusKm != null) "${obj.radiusKm} km" else "N/A", 
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DataCard(
                                label = "MAGNITUDE", 
                                value = obj.magnitude?.toString() ?: "N/A", 
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            // Placeholder for dynamic data
                            DataCard(
                                label = "RA / DEC", 
                                value = "${String.format("%.2f", obj.rightAscension ?: 0.0)} / ${String.format("%.2f", obj.declination ?: 0.0)}", 
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (children.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = if (obj.type == "PLANET") "SATELLITES" else "RELATED OBJECTS",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyanData
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(children.size) { index ->
                                    val child = children[index]
                                    MiniObjectCard(
                                        name = child.name,
                                        imageUrl = child.imageUrl,
                                        onClick = { onObjectClick(child.id) }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = { onLocateClick(obj.id) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanData)
                        ) {
                            Text("LOCATE IN SKY MAP", color = Color.Black, style = MaterialTheme.typography.titleMedium)
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
        
        // Back Button Overlay
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack, 
                contentDescription = "Back", 
                tint = TextPrimary
            ) 
        } 
    }
}

@Composable
fun MiniObjectCard(
    name: String,
    imageUrl: String?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null) {
                AstronomyImage(
                    imageUrl = imageUrl,
                    objectName = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.BrightnessLow,
                    contentDescription = null,
                    tint = TextSecondary.copy(alpha = 0.3f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DataCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyLarge, 
            color = MonospaceData
        )
    }
}
