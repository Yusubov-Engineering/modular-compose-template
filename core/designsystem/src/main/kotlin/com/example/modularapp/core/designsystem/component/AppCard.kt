package com.example.modularapp.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.example.modularapp.core.designsystem.theme.AppTheme

/** A bordered surface. Tappable, through [AppPressable], when [onClick] is given. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(AppTheme.radius.md)
    val surface = modifier
        .background(AppTheme.colors.surface, shape)
        .border(AppTheme.sizes.hairline, AppTheme.colors.border, shape)
    val padding = AppTheme.spacing.lg
    val gap = AppTheme.spacing.xs

    if (onClick == null) {
        Box(surface) {
            CardColumn(padding, gap, content)
        }
    } else {
        AppPressable(onClick = onClick, shape = shape, modifier = surface, contentAlignment = Alignment.TopStart) {
            CardColumn(padding, gap, content)
        }
    }
}

@Composable
private fun CardColumn(
    padding: Dp,
    gap: Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(padding),
        verticalArrangement = Arrangement.spacedBy(gap),
        content = content,
    )
}
