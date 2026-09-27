package com.example.modularapp.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable

/**
 * Durations and easing. `AppTheme.motion` returns [reduced] when the system
 * animator scale is off ("Remove animations"), so anything built from these
 * tokens honours that setting for free. Anything that *loops* must also
 * check [isReduced]: a zero duration has no sensible loop.
 */
@Immutable
data class AppMotion(
    val short: Int = 150,
    val medium: Int = 280,
    val long: Int = 450,
    val standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
    val isReduced: Boolean = false,
) {
    companion object {
        fun reduced(): AppMotion = AppMotion(short = 0, medium = 0, long = 0, isReduced = true)
    }
}
