package com.example.modularapp.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.example.modularapp.core.designsystem.theme.AppTheme

/**
 * A vector drawable, tinted. Icons are ordinary resources — the design
 * system's live in core/designsystem/src/main/res/drawable
 * (`com.example.modularapp.core.designsystem.R.drawable.ic_back`), and a
 * feature's own icons in its own res/drawable. Nothing to register: add the
 * XML (Android Studio's Vector Asset tool imports SVGs) and use its id.
 *
 * Draw icons in black; the tint gives them their colour in either theme.
 * [contentDescription] is null only for purely decorative icons.
 */
@Composable
fun AppIcon(
    @DrawableRes icon: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = AppTheme.colors.textPrimary,
) {
    Image(
        painter = painterResource(icon),
        contentDescription = contentDescription,
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier.size(AppTheme.sizes.icon),
    )
}
