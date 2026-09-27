package com.example.modularapp.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import com.example.modularapp.core.designsystem.theme.AppTheme

/**
 * - [AppButtonVariant.Primary]: filled with the accent — one per screen.
 * - [AppButtonVariant.Secondary]: outlined.
 * - [AppButtonVariant.Quiet]: text only.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    val accent = AppTheme.accent
    val shape = RoundedCornerShape(AppTheme.radius.pill)
    val (background, content, border) = when (variant) {
        AppButtonVariant.Primary -> Triple(accent.base, colors.textOnAccent, Color.Transparent)
        AppButtonVariant.Secondary -> Triple(Color.Transparent, accent.base, colors.border)
        AppButtonVariant.Quiet -> Triple(Color.Transparent, accent.base, Color.Transparent)
    }

    AppPressable(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .background(background, shape)
            .border(AppTheme.sizes.hairline, border, shape),
    ) {
        AppText(
            text = text,
            style = AppTheme.typography.label,
            color = content,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = AppTheme.spacing.xl, vertical = AppTheme.spacing.md),
        )
    }
}

private const val DISABLED_ALPHA = 0.4f
