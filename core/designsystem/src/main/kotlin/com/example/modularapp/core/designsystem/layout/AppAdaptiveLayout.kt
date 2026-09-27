package com.example.modularapp.core.designsystem.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Lays out for the room, not the device: picks [landscape] when the space it
 * is given is more than 1.2x wider than tall, so split screen and tablets get
 * the right arrangement too.
 */
@Composable
fun AppAdaptiveLayout(
    portrait: @Composable () -> Unit,
    landscape: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier) {
        if (maxWidth > maxHeight * LANDSCAPE_RATIO) landscape() else portrait()
    }
}

private const val LANDSCAPE_RATIO = 1.2f
