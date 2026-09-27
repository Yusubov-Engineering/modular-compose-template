package com.example.modularapp.features.counter.impl.di

import com.example.modularapp.core.router.AppRoute
import com.example.modularapp.features.counter.CounterApi
import com.example.modularapp.features.counter.CounterLauncher
import com.example.modularapp.features.counter.impl.router.CounterDetailsRoute
import com.example.modularapp.features.counter.impl.router.CounterRoute

internal class CounterApiImpl : CounterApi {
    override val launcher: CounterLauncher = CounterLauncherImpl()
}

internal class CounterLauncherImpl : CounterLauncher {
    override fun counter(): AppRoute = CounterRoute

    override fun details(count: Int): AppRoute = CounterDetailsRoute(count)
}
