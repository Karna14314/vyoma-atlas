package com.karnadigital.vyoma.atlas.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.karnadigital.vyoma.atlas.R
import com.karnadigital.vyoma.atlas.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.karnadigital.vyoma.atlas.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    category: String? = null,
    // Inject ViewModel via Hilt
    viewModel: HomeViewModel = hiltViewModel(),
    onObjectClick: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val objects by viewModel.objects.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    LaunchedEffect(category) {
        if (category != null) {
            viewModel.filterByCategory(category)
        }
    }
    
    // Add logging for debugging
    LaunchedEffect(objects.size) {
        android.util.Log.d("HomeScreen", "Objects loaded: ${objects.size}")
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp) // Avoid FAB overlap
    ) {
        // Hero Section (Parallax)
        item {
            val scrollOffset = listState.firstVisibleItemScrollOffset
            val parallaxFactor = 0.5f // Moves slower than list
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .graphicsLayer {
                        translationY = scrollOffset * parallaxFactor
                    }
            ) {
                // Placeholder Image (e.g., from assets or resource)
                // Since we don't have a resource ID yet, using a colored Box + text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF3B2664), Color(0xFF1E1E2E)) // Placeholder Nebula colors
                            )
                        )
                ) {
                   // Mock Image Content
                   Column(
                       modifier = Modifier
                           .align(Alignment.BottomStart)
                           .padding(16.dp)
                   ) {
                       Text(
                           text = "NASA Picture of the Day", 
                           style = MaterialTheme.typography.labelSmall, 
                           color = CyanData
                       )
                       Text(
                           text = "The Pillars of Creation", 
                           style = MaterialTheme.typography.headlineMedium, 
                           color = TextPrimary
                       )
                   }
                }
            }
        }

        // Trending Chips
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Explore",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val categories = listOf("All", "Solar System", "Stars", "Deep Sky", "Galaxies", "Nebulae", "Black Holes") // Matching DB Types approximately
            
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.filterByCategory(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary,
                            selectedContainerColor = CyanData.copy(alpha = 0.2f),
                            selectedLabelColor = CyanData
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = GlassBorder,
                            selectedBorderColor = CyanData,
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(50) 
                    )
                }
            }
        }

        // Recent Discoveries List / Objects List
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Celestial Objects",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (objects.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = CyanData.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Loading Universe...",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "Initializing astronomical database",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(objects) { obj ->
                GlassCard(
                    obj = obj,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { 
                            android.util.Log.d("HomeScreen", "Clicked object: ${obj.id} - ${obj.name}")
                            onObjectClick(obj.id) 
                        }
                )
            }
        }
    }
}

@Composable
fun GlassCard(
    obj: com.karnadigital.vyoma.atlas.data.local.entity.AstronomicalObject,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val icon = when (obj.type) {
                "PLANET" -> Icons.Default.Place
                "STAR" -> Icons.Default.Star
                "GALAXY" -> Icons.Default.Refresh // Approximation
                "NEBULA" -> Icons.Default.Cloud
                else -> Icons.Default.Info
            }
            
            val iconColor = when (obj.type) {
                "PLANET" -> Color(0xFF4FC3F7)
                "STAR" -> GoldenrodStar
                "GALAXY" -> Color(0xFFB388FF)
                "NEBULA" -> Color(0xFFFF80AB)
                else -> TextSecondary
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = obj.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(
                    text = obj.constellation ?: obj.type.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            
            if (obj.magnitude != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Mag", style = MaterialTheme.typography.labelSmall, color = CyanData)
                    Text(text = String.format("%.1f", obj.magnitude), style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                }
            }
        }
    }
}
