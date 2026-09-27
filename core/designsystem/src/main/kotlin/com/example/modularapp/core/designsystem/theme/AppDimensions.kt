package com.example.modularapp.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing scale. `AppTheme.spacing.lg`, never `16.dp`. */
@Immutable
data class AppSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
)

/** Corner radii. */
@Immutable
data class AppRadius(
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 20.dp,
    val pill: Dp = 999.dp,
)

/** Fixed sizes: touch targets, icons, bars. */
@Immutable
data class AppSizes(
    val minTouchTarget: Dp = 48.dp,
    val icon: Dp = 24.dp,
    val topBar: Dp = 56.dp,
    val progress: Dp = 32.dp,
    val hairline: Dp = 1.dp,
)
