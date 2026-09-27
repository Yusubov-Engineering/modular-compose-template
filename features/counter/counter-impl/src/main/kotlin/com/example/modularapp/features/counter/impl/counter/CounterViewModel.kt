package com.example.modularapp.features.counter.impl.counter

import com.example.modularapp.core.logger.AppLogger
import com.example.modularapp.core.statemanager.AppStateViewModel

internal data class CounterState(val count: Int = 0)

internal sealed interface CounterEvent {
    data object Increment : CounterEvent

    data object Decrement : CounterEvent

    data object Reset : CounterEvent

    data object OpenDetails : CounterEvent

    data object OpenPosts : CounterEvent
}

/** Navigation is an effect: the view model says *what*, the screen does it. */
internal sealed interface CounterEffect {
    data class OpenDetails(val count: Int) : CounterEffect

    data object OpenPosts : CounterEffect
}

internal class CounterViewModel(
    private val logger: AppLogger,
) : AppStateViewModel<CounterState, CounterEvent, CounterEffect>(CounterState()) {

    override fun onEvent(event: CounterEvent) {
        when (event) {
            CounterEvent.Increment -> update { it.copy(count = it.count + 1) }
            CounterEvent.Decrement -> update { it.copy(count = it.count - 1) }
            CounterEvent.Reset -> emit(CounterState())
            CounterEvent.OpenDetails -> emitEffect(CounterEffect.OpenDetails(currentState.count))
            CounterEvent.OpenPosts -> {
                logger.debug("Opening posts", tag = TAG)
                emitEffect(CounterEffect.OpenPosts)
            }
        }
    }

    private companion object {
        const val TAG = "Counter"
    }
}
