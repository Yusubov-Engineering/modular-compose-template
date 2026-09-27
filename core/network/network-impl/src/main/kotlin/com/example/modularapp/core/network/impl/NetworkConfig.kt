package com.example.modularapp.core.network.impl

/** Set by the app from its flavor (config/<flavor>.properties). */
data class NetworkConfig(
    val baseUrl: String,
    val enableLogging: Boolean,
    val timeoutMillis: Long = 15_000,
)
