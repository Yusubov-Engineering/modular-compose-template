package com.example.modularapp.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import com.example.modularapp.core.designsystem.theme.AppTheme

/**
 * The base of everything tappable. It owns button semantics, the minimum
 * touch target and press feedback (a slight shrink plus the theme's veil),
 * so a component built on it cannot forget any of the three.
 */
@Composable
fun AppPressable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    onClickLabel: String? = null,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val motion = AppTheme.motion
    val scale by animateFloatAsState(
        targetValue = if (pressed && !motion.isReduced) PRESSED_SCALE else 1f,
        animationSpec = tween(motion.short, easing = motion.standard),
        label = "press-scale",
    )
    val minTarget = AppTheme.sizes.minTouchTarget

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = minTarget, minHeight = minTarget)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .clickable(
                interactionSource = interactions,
                indication = LocalIndication.current,
                enabled = enabled,
                onClickLabel = onClickLabel,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = contentAlignment,
        content = content,
    )
}

private const val PRESSED_SCALE = 0.97f
