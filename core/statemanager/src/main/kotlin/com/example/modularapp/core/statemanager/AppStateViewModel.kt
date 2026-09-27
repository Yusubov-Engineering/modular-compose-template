package com.example.modularapp.core.statemanager

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * State / Event / Effect over a plain AndroidX [ViewModel].
 *
 * - **State** ([S]) is what the screen draws. There is always exactly one,
 *   read with [state] and changed with [emit] / [update].
 * - **Events** ([E]) are what the user did. The screen calls [dispatch]; the
 *   view model reacts in [onEvent]. Nothing else changes state.
 * - **Effects** ([F]) are one-shot instructions to the screen — navigate,
 *   show a message — sent with [emitEffect] and handled by [CollectEffects].
 *   **Navigation is an effect.** The view model never holds a navigator or a
 *   `Context`, which is what lets a plain unit test drive it.
 *
 * Effects are delivered once, to one collector. One sent while nobody is
 * collecting (the screen is stopped) waits in the buffer and is delivered on
 * return, rather than being lost.
 *
 * Do initial loading in an `init {}` block: the view model is created when
 * its screen first appears, which is what Flutter's `onInit` does.
 */
abstract class AppStateViewModel<S : Any, E : Any, F : Any>(initialState: S) : ViewModel() {
    private val mutableState = MutableStateFlow(initialState)
    private val effectChannel = Channel<F>(Channel.BUFFERED)

    val state: StateFlow<S> = mutableState.asStateFlow()

    val effects: Flow<F> = effectChannel.receiveAsFlow()

    /** The state right now. */
    protected val currentState: S
        get() = mutableState.value

    fun dispatch(event: E) = onEvent(event)

    protected abstract fun onEvent(event: E)

    protected fun emit(state: S) {
        mutableState.value = state
    }

    /** Atomic read-modify-write — safe when two coroutines update at once. */
    protected fun update(transform: (S) -> S) {
        mutableState.update(transform)
    }

    protected fun emitEffect(effect: F) {
        effectChannel.trySend(effect)
    }

    override fun onCleared() {
        effectChannel.close()
        super.onCleared()
    }
}
