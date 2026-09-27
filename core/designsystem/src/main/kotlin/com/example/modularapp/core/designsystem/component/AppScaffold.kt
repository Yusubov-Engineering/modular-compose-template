package com.example.modularapp.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.modularapp.core.designsystem.theme.AppTheme

/**
 * A screen: background, optional top bar, and the body.
 *
 * It owns the system insets (status bar, navigation bar, cutouts, keyboard)
 * and consumes them, so a body never pads for them a second time.
 */
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .consumeWindowInsets(WindowInsets.safeDrawing),
    ) {
        if (topBar != null) {
            Box(Modifier.fillMaxWidth()) { topBar() }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) { content() }
    }
}
