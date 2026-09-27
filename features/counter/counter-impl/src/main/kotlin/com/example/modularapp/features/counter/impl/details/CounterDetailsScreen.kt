package com.example.modularapp.features.counter.impl.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.modularapp.core.designsystem.component.AppMessageView
import com.example.modularapp.core.designsystem.component.AppScaffold
import com.example.modularapp.core.designsystem.component.AppTopBar
import com.example.modularapp.core.designsystem.theme.AppAccent
import com.example.modularapp.core.designsystem.theme.AppAccentScope
import com.example.modularapp.core.router.LocalAppNavigator
import com.example.modularapp.features.counter.impl.R

/** Stateless: it shows what its route carries, so it needs no view model. */
@Composable
internal fun CounterDetailsScreen(count: Int) {
    val navigator = LocalAppNavigator.current
    // An accent recolours everything below it — no screen names a colour.
    AppAccentScope(accent = AppAccent.Teal) {
        AppScaffold(
            topBar = {
                AppTopBar(
                    title = stringResource(R.string.counter_details_title),
                    onBack = navigator::pop,
                    backLabel = stringResource(R.string.counter_back),
                )
            },
        ) {
            AppMessageView(
                title = count.toString(),
                message = stringResource(R.string.counter_details_message, count),
            )
        }
    }
}
