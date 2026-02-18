package com.karnadigital.vyoma.atlas.ui.common

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.io.File

/**
 * Loads images with offline-first strategy:
 * 1. Check if local asset exists
 * 2. Fall back to online URL
 * 3. Show placeholder if both fail
 */
@Composable
fun AstronomyImage(
    imageUrl: String?,
    objectName: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val finalUrl = resolveImageUrl(context, imageUrl)
    
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(finalUrl)
            .crossfade(true)
            .build()
    )
    
    when (painter.state) {
        is AsyncImagePainter.State.Success -> {
            Image(
                painter = painter,
                contentDescription = objectName,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        else -> {
            PlaceholderImage(objectName, modifier)
        }
    }
}

/**
 * Resolves image URL to local asset if available, otherwise returns original URL
 */
private fun resolveImageUrl(context: Context, imageUrl: String?): String? {
    if (imageUrl.isNullOrBlank()) return null
    
    // If it's a local asset path (e.g., "images/sun.webp")
    if (!imageUrl.startsWith("http")) {
        // Check if asset exists
        return try {
            context.assets.open(imageUrl).close()
            "file:///android_asset/$imageUrl"
        } catch (e: Exception) {
            // Asset doesn't exist, return null to show placeholder
            null
        }
    }
    
    // It's an HTTP URL - try to find local asset by extracting filename
    val filename = imageUrl.substringAfterLast("/").substringBefore("?")
    val assetPath = "images/$filename"
    
    // Check if we have a local copy
    return try {
        context.assets.open(assetPath).close()
        "file:///android_asset/$assetPath"
    } catch (e: Exception) {
        // No local copy, use original URL
        imageUrl
    }
}

/**
 * Generates a placeholder with gradient and initials
 */
@Composable
private fun PlaceholderImage(objectName: String, modifier: Modifier = Modifier) {
    val initials = objectName.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .take(2)
    
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
    )
    
    Box(
        modifier = modifier
            .background(
                brush = Brush.radialGradient(gradientColors),
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.displayLarge,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}
