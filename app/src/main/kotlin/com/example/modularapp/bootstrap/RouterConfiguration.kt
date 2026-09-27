package com.example.modularapp.bootstrap

import com.example.modularapp.core.router.AppRoute
import com.example.modularapp.core.router.ModuleRouter
import com.example.modularapp.features.counter.CounterApi
import org.koin.core.Koin

/**
 * Assembles every feature's routes, and asks a feature's launcher where the
 * app opens — the start destination is never a hard-coded route.
 *
 * Every feature's Koin module binds its router as a [ModuleRouter], so
 * `getAll` finds them all without this file naming one.
 */
internal class RouterConfiguration(koin: Koin) {
    val moduleRouters: List<ModuleRouter> = koin.getAll()

    val initialRoute: AppRoute = koin.get<CounterApi>().launcher.counter()
}
