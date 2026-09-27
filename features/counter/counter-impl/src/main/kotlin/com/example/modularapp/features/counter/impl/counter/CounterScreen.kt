package com.example.modularapp.features.counter.impl.counter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.modularapp.core.designsystem.component.AppButton
import com.example.modularapp.core.designsystem.component.AppButtonVariant
import com.example.modularapp.core.designsystem.component.AppScaffold
import com.example.modularapp.core.designsystem.component.AppText
import com.example.modularapp.core.designsystem.component.AppTopBar
import com.example.modularapp.core.designsystem.layout.AppAdaptiveLayout
import com.example.modularapp.core.designsystem.theme.AppTheme
import com.example.modularapp.core.router.LocalAppNavigator
import com.example.modularapp.core.statemanager.CollectEffects
import com.example.modularapp.features.counter.impl.R
import com.example.modularapp.features.counter.impl.router.CounterDetailsRoute
import com.example.modularapp.features.posts.PostsLauncher

@Composable
internal fun CounterScreen(viewModel: CounterViewModel, postsLauncher: PostsLauncher) {
    val navigator = LocalAppNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            // Inside this module: the route itself.
            is CounterEffect.OpenDetails -> navigator.push(CounterDetailsRoute(effect.count))
            // Another feature: only through its launcher. The caller picks the verb.
            CounterEffect.OpenPosts -> navigator.push(postsLauncher.posts())
        }
    }

    AppScaffold(topBar = { AppTopBar(title = stringResource(R.string.counter_title)) }) {
        AppAdaptiveLayout(
            portrait = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(AppTheme.spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxl, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CountDisplay(state.count)
                    CounterActions(onEvent = viewModel::dispatch)
                }
            },
            landscape = {
                Row(
                    modifier = Modifier.fillMaxSize().padding(AppTheme.spacing.xl),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxl),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CountDisplay(state.count, Modifier.weight(1f))
                    CounterActions(onEvent = viewModel::dispatch, modifier = Modifier.weight(1f))
                }
            },
        )
    }
}

@Composable
private fun CountDisplay(count: Int, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.counter_value_label)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        AppText(text = label, color = AppTheme.colors.textSecondary)
        AppText(
            text = count.toString(),
            style = AppTheme.typography.display,
            color = AppTheme.accent.base,
            modifier = Modifier.semantics {
                contentDescription = "$label: $count"
                liveRegion = LiveRegionMode.Polite
            },
        )
    }
}

@Composable
private fun CounterActions(onEvent: (CounterEvent) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            AppButton(
                text = stringResource(R.string.counter_decrement),
                onClick = { onEvent(CounterEvent.Decrement) },
                variant = AppButtonVariant.Secondary,
            )
            AppButton(
                text = stringResource(R.string.counter_increment),
                onClick = { onEvent(CounterEvent.Increment) },
            )
        }
        AppButton(
            text = stringResource(R.string.counter_open_details),
            onClick = { onEvent(CounterEvent.OpenDetails) },
            variant = AppButtonVariant.Secondary,
        )
        AppButton(
            text = stringResource(R.string.counter_open_posts),
            onClick = { onEvent(CounterEvent.OpenPosts) },
            variant = AppButtonVariant.Secondary,
        )
        AppButton(
            text = stringResource(R.string.counter_reset),
            onClick = { onEvent(CounterEvent.Reset) },
            variant = AppButtonVariant.Quiet,
        )
    }
}
