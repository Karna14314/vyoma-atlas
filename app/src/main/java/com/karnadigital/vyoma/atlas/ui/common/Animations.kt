package com.karnadigital.vyoma.atlas.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.karnadigital.vyoma.atlas.ui.theme.CyanData

/**
 * A modifier that adds a pulsing ring effect behind standard content.
 * Useful for FABs or highlights.
 */
@Composable
fun PulsingRing(
    modifier: Modifier = Modifier,
    pulseColor: Color = CyanData,
    initialScale: Float = 1f,
    targetScale: Float = 1.6f, // How restricted the pulse is
    durationMillis: Int = 2000,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = initialScale,
        targetValue = targetScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseScale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    Box(modifier = modifier) {
        // The Pulse Ring
        Box(
            modifier = Modifier
                .matchParentSize()
                .scale(scale)
                .graphicsLayer {
                    this.alpha = alpha
                    this.shape = CircleShape
                    this.clip = true
                }
                .background(pulseColor)
        )
        
        // The actual content (e.g., FAB) on top
        content()
    }
}
