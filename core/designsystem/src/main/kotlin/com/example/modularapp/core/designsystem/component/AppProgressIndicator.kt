package com.example.modularapp.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import com.example.modularapp.core.designsystem.theme.AppTheme

/**
 * An indeterminate spinner in the accent colour. It loops, so it checks
 * `motion.isReduced` itself and holds still when animations are off.
 *
 * A Canvas contributes nothing to the semantics tree; [label] is what a
 * screen reader announces.
 */
@Composable
fun AppProgressIndicator(label: String, modifier: Modifier = Modifier) {
    val color = AppTheme.accent.base
    val track = AppTheme.colors.border
    val motion = AppTheme.motion
    val rotation = if (motion.isReduced) {
        0f
    } else {
        val transition = rememberInfiniteTransition(label = "progress")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = FULL_TURN,
            animationSpec = infiniteRepeatable(tween(SPIN_MILLIS, easing = LinearEasing), RepeatMode.Restart),
            label = "progress-rotation",
        ).value
    }

    Canvas(
        modifier = modifier
            .size(AppTheme.sizes.progress)
            .semantics {
                contentDescription = label
                progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
            },
    ) {
        val stroke = Stroke(width = size.minDimension / STROKE_RATIO, cap = StrokeCap.Round)
        drawArc(track, 0f, FULL_TURN, useCenter = false, style = stroke)
        rotate(rotation) {
            drawArc(color, START_ANGLE, SWEEP, useCenter = false, style = stroke)
        }
    }
}

private const val FULL_TURN = 360f
private const val START_ANGLE = -90f
private const val SWEEP = 100f
private const val SPIN_MILLIS = 900
private const val STROKE_RATIO = 9f
