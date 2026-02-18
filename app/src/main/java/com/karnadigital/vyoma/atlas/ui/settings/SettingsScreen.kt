package com.karnadigital.vyoma.atlas.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.karnadigital.vyoma.atlas.ui.theme.DeepCharcoalNavyTop
import com.karnadigital.vyoma.atlas.ui.theme.SpaceGradient
import com.karnadigital.vyoma.atlas.ui.theme.TextPrimary
import com.karnadigital.vyoma.atlas.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    var showConstellationLines by remember { mutableStateOf(true) }
    var nightMode by remember { mutableStateOf(false) }
    var sensorsEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepCharcoalNavyTop
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SpaceGradient)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Display Options",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SettingItem(
                    title = "Show Constellation Lines",
                    description = "Display lines connecting stars in constellations",
                    checked = showConstellationLines,
                    onCheckedChange = { showConstellationLines = it }
                )

                SettingItem(
                    title = "Night Mode",
                    description = "Red tint overlay to preserve night vision",
                    checked = nightMode,
                    onCheckedChange = { nightMode = it }
                )

                Divider(
                    color = TextSecondary.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Text(
                    text = "Sensors",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                SettingItem(
                    title = "Enable Sensors",
                    description = "Use device sensors for AR sky mapping",
                    checked = sensorsEnabled,
                    onCheckedChange = { sensorsEnabled = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* TODO: Implement calibration */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Calibrate Sensors")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Text(
                    text = "Vyoma - Astronomy Atlas\nVersion 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}
