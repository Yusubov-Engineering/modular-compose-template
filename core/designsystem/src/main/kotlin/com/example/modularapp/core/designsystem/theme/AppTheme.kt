package com.example.modularapp.core.designsystem.theme

import android.provider.Settings
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

// Static, not dynamic, composition locals: tokens change only when the theme
// does, and a static local skips per-read tracking — a change recomposes the
// whole subtree, which is what a theme change needs anyway.
internal val LocalAppColors = staticCompositionLocalOf<AppColors> { missingTheme("AppColors") }
internal val LocalAppAccent = staticCompositionLocalOf { AppAccent.Indigo }
internal val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }
internal val LocalAppRadius = staticCompositionLocalOf { AppRadius() }
internal val LocalAppSizes = staticCompositionLocalOf { AppSizes() }
internal val LocalAppTypography = staticCompositionLocalOf { AppTypography() }
internal val LocalAppMotion = staticCompositionLocalOf { AppMotion() }

private fun missingTheme(token: String): Nothing =
    error("No $token provided. Wrap the content in AppTheme { ... }.")

/**
 * Provides every token to [content]. The app wraps its root in this once;
 * previews and screenshot tests wrap what they render.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accent: AppAccent = AppAccent.Indigo,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) AppColors.dark() else AppColors.light()
    val motion = if (rememberAnimationsDisabled()) AppMotion.reduced() else AppMotion()
    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppAccent provides accent,
        LocalAppMotion provides motion,
        LocalIndication provides AppPressIndication,
        content = content,
    )
}

/**
 * Re-colours a subtree. Screens pick an accent; they never name a colour.
 */
@Composable
fun AppAccentScope(accent: AppAccent, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppAccent provides accent, content = content)
}

/** Read tokens with `AppTheme.colors`, `AppTheme.spacing.lg`, and so on. */
object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current

    val accent: AppAccent
        @Composable @ReadOnlyComposable
        get() = LocalAppAccent.current

    val spacing: AppSpacing
        @Composable @ReadOnlyComposable
        get() = LocalAppSpacing.current

    val radius: AppRadius
        @Composable @ReadOnlyComposable
        get() = LocalAppRadius.current

    val sizes: AppSizes
        @Composable @ReadOnlyComposable
        get() = LocalAppSizes.current

    val typography: AppTypography
        @Composable @ReadOnlyComposable
        get() = LocalAppTypography.current

    val motion: AppMotion
        @Composable @ReadOnlyComposable
        get() = LocalAppMotion.current
}

@Composable
private fun rememberAnimationsDisabled(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
