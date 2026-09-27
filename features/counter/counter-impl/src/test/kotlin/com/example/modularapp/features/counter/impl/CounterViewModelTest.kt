package com.example.modularapp.features.counter.impl

import app.cash.turbine.test
import com.example.modularapp.core.logger.AppLogger
import com.example.modularapp.features.counter.impl.counter.CounterEffect
import com.example.modularapp.features.counter.impl.counter.CounterEvent
import com.example.modularapp.features.counter.impl.counter.CounterViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CounterViewModelTest {
    private val viewModel = CounterViewModel(AppLogger.Silent)

    @Test
    fun `increment, decrement and reset change the count`() {
        viewModel.dispatch(CounterEvent.Increment)
        viewModel.dispatch(CounterEvent.Increment)
        viewModel.dispatch(CounterEvent.Decrement)
        assertEquals(1, viewModel.state.value.count)

        viewModel.dispatch(CounterEvent.Reset)
        assertEquals(0, viewModel.state.value.count)
    }

    @Test
    fun `navigation is emitted as effects, carrying the current count`() = runTest {
        viewModel.dispatch(CounterEvent.Increment)
        viewModel.dispatch(CounterEvent.OpenDetails)
        viewModel.dispatch(CounterEvent.OpenPosts)

        viewModel.effects.test {
            assertEquals(CounterEffect.OpenDetails(1), awaitItem())
            assertEquals(CounterEffect.OpenPosts, awaitItem())
        }
    }
}
