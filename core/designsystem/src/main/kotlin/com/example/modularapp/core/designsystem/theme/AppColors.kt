package com.example.modularapp.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * The whole palette. Screens read it through `AppTheme.colors`, never
 * `Color(0xFF...)` — so a dark theme, or a rebrand, is one edit here.
 *
 * The set is deliberately small. Adding a colour means adding it to both
 * [light] and [dark].
 */
@Immutable
data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textOnAccent: Color,
    val success: Color,
    val error: Color,
    val isDark: Boolean,
) {
    companion object {
        fun light(): AppColors = AppColors(
            background = Color(0xFFF7F7FA),
            surface = Color(0xFFFFFFFF),
            surfaceRaised = Color(0xFFEFEFF4),
            border = Color(0xFFDCDCE4),
            textPrimary = Color(0xFF15151A),
            textSecondary = Color(0xFF5E5E6B),
            textOnAccent = Color(0xFFFFFFFF),
            success = Color(0xFF1F8A4C),
            error = Color(0xFFC62B3B),
            isDark = false,
        )

        fun dark(): AppColors = AppColors(
            background = Color(0xFF0F0F13),
            surface = Color(0xFF18181E),
            surfaceRaised = Color(0xFF222229),
            border = Color(0xFF30303A),
            textPrimary = Color(0xFFF2F2F5),
            textSecondary = Color(0xFFA3A3B0),
            textOnAccent = Color(0xFFFFFFFF),
            success = Color(0xFF4CC27E),
            error = Color(0xFFFF6B78),
            isDark = true,
        )
    }
}

/**
 * The colour of *where you are*, as opposed to [AppColors]' fixed meanings
 * (success is green everywhere). Wrap a subtree in [AppAccentScope] and
 * everything under it that reads `AppTheme.accent` follows, in both themes.
 */
@Immutable
data class AppAccent(
    val base: Color,
    val container: Color,
) {
    companion object {
        val Indigo = AppAccent(base = Color(0xFF4F46E5), container = Color(0x224F46E5))
        val Teal = AppAccent(base = Color(0xFF0F9D8A), container = Color(0x220F9D8A))
        val Amber = AppAccent(base = Color(0xFFD97706), container = Color(0x22D97706))
    }
}
