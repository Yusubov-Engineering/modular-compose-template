package com.example.modularapp.core.statemanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Handles a view model's effects while the screen is at least STARTED.
 *
 * ```
 * CollectEffects(viewModel.effects) { effect ->
 *     when (effect) {
 *         is PostsEffect.OpenDetails -> navigator.push(PostDetailsRoute(effect.id))
 *     }
 * }
 * ```
 *
 * Collected on `Dispatchers.Main.immediate`, so an effect is handled in the
 * same frame it was sent and cannot slip through a lifecycle change.
 */
@Composable
fun <F> CollectEffects(effects: Flow<F>, onEffect: suspend (F) -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val handler by rememberUpdatedState(onEffect)
    LaunchedEffect(effects, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                effects.collect { handler(it) }
            }
        }
    }
}
