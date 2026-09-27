package com.example.modularapp.core.statemanager

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AppStateViewModelTest {
    private class Toggle : AppStateViewModel<Boolean, Unit, String>(initialState = false) {
        override fun onEvent(event: Unit) {
            update { !it }
            emitEffect(if (currentState) "on" else "off")
        }
    }

    @Test
    fun `dispatching an event updates state and emits an effect`() = runTest {
        val viewModel = Toggle()

        viewModel.dispatch(Unit)

        assertEquals(true, viewModel.state.value)
        viewModel.effects.test { assertEquals("on", awaitItem()) }
    }

    @Test
    fun `effects sent while nobody collects are delivered in order, once`() = runTest {
        val viewModel = Toggle()

        viewModel.dispatch(Unit)
        viewModel.dispatch(Unit)

        viewModel.effects.test {
            assertEquals("on", awaitItem())
            assertEquals("off", awaitItem())
            expectNoEvents()
        }
    }
}
