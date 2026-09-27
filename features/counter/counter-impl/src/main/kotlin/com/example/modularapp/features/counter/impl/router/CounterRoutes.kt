package com.example.modularapp.features.counter.impl.router

import com.example.modularapp.core.router.AppRoute
import kotlinx.serialization.Serializable

@Serializable
internal data object CounterRoute : AppRoute

@Serializable
internal data class CounterDetailsRoute(val count: Int) : AppRoute
