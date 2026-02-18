package com.karnadigital.vyoma.atlas.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.karnadigital.vyoma.atlas.ui.detail.ObjectDetailScreen
import com.karnadigital.vyoma.atlas.ui.home.HomeScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(
            route = Screen.Home.route,
            arguments = Screen.Home.arguments,
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -1000 },
                    animationSpec = tween(700)
                ) + fadeOut(animationSpec = tween(700))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -1000 },
                    animationSpec = tween(700)
                ) + fadeIn(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")
            android.util.Log.d("AppNavigation", "HomeScreen - category: $category")
            HomeScreen(
                category = category,
                onObjectClick = { userObjectId ->
                    android.util.Log.d("AppNavigation", "Navigating to detail: $userObjectId")
                    navController.navigate(Screen.Detail.createRoute(userObjectId))
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = Screen.Detail.arguments,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(700)
                ) + fadeIn(animationSpec = tween(700))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 1000 },
                    animationSpec = tween(700)
                ) + fadeOut(animationSpec = tween(700))
            }
        ) {
            ObjectDetailScreen(
                onBackClick = { navController.popBackStack() },
                onObjectClick = { id ->
                    navController.navigate(Screen.Detail.createRoute(id))
                },
                onLocateClick = { id ->
                    navController.navigate(Screen.SkyMap.createRoute(id))
                }
            )
        }
        
        // SkyMap
        composable(
            route = Screen.SkyMap.route,
            arguments = Screen.SkyMap.arguments
        ) {
             com.karnadigital.vyoma.atlas.ui.skymap.SkyMapScreen(
                 onClose = { navController.popBackStack() }
             )
        }
        
        // Settings
        composable(route = Screen.Settings.route) {
            com.karnadigital.vyoma.atlas.ui.settings.SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
