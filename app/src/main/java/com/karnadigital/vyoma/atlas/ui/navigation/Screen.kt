package com.karnadigital.vyoma.atlas.ui.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String, val arguments: List<NamedNavArgument> = emptyList()) {
    object Home : Screen(
        route = "home?category={category}",
        arguments = listOf(navArgument("category") { 
            type = NavType.StringType 
            nullable = true
        })
    ) {
        fun createRoute(category: String?) = if (category != null) "home?category=$category" else "home"
    }
    object Detail : Screen(
        route = "detail/{objectId}",
        arguments = listOf(navArgument("objectId") { type = NavType.StringType })
    ) {
        fun createRoute(objectId: String) = "detail/$objectId"
    }
    object SkyMap : Screen(
        route = "skymap?targetId={targetId}",
        arguments = listOf(navArgument("targetId") { 
            type = NavType.StringType 
            nullable = true
        })
    ) {
        fun createRoute(targetId: String?) = if (targetId != null) "skymap?targetId=$targetId" else "skymap"
    }
    object Settings : Screen(route = "settings")
}
