package com.example.modularapp.core.logger.impl

/** Set by the app from its flavor: `enabled = false` silences everything below errors. */
data class LoggerConfig(val enabled: Boolean, val tag: String = "ModularApp")
