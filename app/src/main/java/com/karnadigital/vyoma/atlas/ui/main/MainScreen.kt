package com.karnadigital.vyoma.atlas.ui.main

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.karnadigital.vyoma.atlas.ui.common.PulsingRing
import com.karnadigital.vyoma.atlas.ui.home.HomeScreen
import com.karnadigital.vyoma.atlas.ui.theme.DeepCharcoalNavyTop
import com.karnadigital.vyoma.atlas.ui.theme.GlassBorder
import com.karnadigital.vyoma.atlas.ui.theme.SpaceGradient
import com.karnadigital.vyoma.atlas.ui.theme.TextPrimary
import com.karnadigital.vyoma.atlas.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController
import com.karnadigital.vyoma.atlas.ui.navigation.AppNavigation
import com.karnadigital.vyoma.atlas.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController() // Hoisted


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DeepCharcoalNavyTop, // Dark sidebar
                drawerContentColor = TextPrimary
            ) {
                // Header - App Logo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(16.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Vyoma Logo",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Vyoma",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close() 
                            navController.navigate(Screen.Home.createRoute("All")) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        } 
                    },
                    icon = { Icon(Icons.Default.Home, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Sky Map") },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close() 
                            navController.navigate(Screen.SkyMap.route)
                        } 
                    },
                    icon = { Icon(Icons.Default.Star, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Solar System") },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close() 
                            navController.navigate(Screen.Home.createRoute("Solar System"))
                        } 
                    },
                    icon = { Icon(Icons.Default.Place, null) } 
                )
                NavigationDrawerItem(
                    label = { Text("Deep Sky") },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close() 
                            navController.navigate(Screen.Home.createRoute("Deep Sky"))
                        } 
                    },
                    icon = { Icon(Icons.Default.Search, null) }
                )
                Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close()
                            navController.navigate(Screen.Settings.route)
                        } 
                    },
                    icon = { Icon(Icons.Default.Settings, null) }
                )
            }
        }
    ) {
        Scaffold(
            // Use transparent container so our gradient background shows through if we apply it to Box
            containerColor = Color.Transparent, 
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Astronomy", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = TextPrimary,
                        navigationIconContentColor = TextPrimary
                    )
                )
            },
            floatingActionButton = {
                PulsingRing {
                    FloatingActionButton(
                        onClick = { 
                            navController.navigate(Screen.SkyMap.route)
                        },
                        containerColor = MaterialTheme.colorScheme.primary, // Cyan
                        contentColor = Color.Black
                    ) {
                        Icon(Icons.Default.Place, contentDescription = "Explore Sky")
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SpaceGradient) // The Deep Space Gradient
                    .padding(paddingValues)
            ) {
                // Use hoisted controller
                AppNavigation(navController = navController)
            }
        }
    }
}
