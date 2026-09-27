package com.example.modularapp.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.example.modularapp.core.designsystem.R
import com.example.modularapp.core.designsystem.theme.AppTheme

/**
 * A title row with an optional back button. [backLabel] is the back button's
 * accessibility label — required whenever [onBack] is given, since the icon
 * is drawn and says nothing to a screen reader on its own.
 */
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backLabel: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = AppTheme.sizes.topBar)
            .padding(horizontal = AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        if (onBack != null) {
            AppPressable(onClick = onBack, shape = CircleShape, onClickLabel = backLabel) {
                AppIcon(icon = R.drawable.ic_back, contentDescription = backLabel)
            }
        }
        AppText(
            text = title,
            style = AppTheme.typography.title,
            maxLines = 1,
            modifier = Modifier
                .padding(horizontal = AppTheme.spacing.sm)
                .semantics { heading() },
        )
    }
}
