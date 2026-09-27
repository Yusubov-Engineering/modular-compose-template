package com.example.modularapp.features.counter

import com.example.modularapp.core.router.AppRoute

/** Everything other modules may use from the counter feature. */
interface CounterApi {
    val launcher: CounterLauncher
}

/** The places this feature can be entered at. */
interface CounterLauncher {
    fun counter(): AppRoute

    fun details(count: Int): AppRoute
}
