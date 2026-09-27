package com.example.modularapp.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Text styles, colourless on purpose: a style says *how big*, the caller's
 * colour says *what it means*. [com.example.modularapp.core.designsystem.component.AppText]
 * fills in `textPrimary` when no colour is given.
 */
@Immutable
data class AppTypography(
    val display: TextStyle = TextStyle(fontSize = 48.sp, lineHeight = 56.sp, fontWeight = FontWeight.Bold),
    val title: TextStyle = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    val body: TextStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    val label: TextStyle = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    val caption: TextStyle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
)
