package com.example.modularapp.core.router

import androidx.navigation3.runtime.NavKey

/**
 * A destination. Every route is a `@Serializable` class **internal to its
 * feature's -impl module** — no other module can name it, let alone build
 * one. Other features reach a route only through that feature's launcher,
 * which returns it typed as [AppRoute].
 *
 * Serializable because the back stack survives process death: see
 * [ModuleRouter.registerRoutes].
 */
interface AppRoute : NavKey
