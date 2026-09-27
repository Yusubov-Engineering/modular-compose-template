package com.example.modularapp.bootstrap

import com.example.modularapp.BuildConfig

/**
 * The flavor's settings, from config/<flavor>.properties via BuildConfig.
 * The one place BuildConfig is read.
 */
internal data class AppConfig(
    val apiBaseUrl: String,
    val enableLogs: Boolean,
) {
    companion object {
        fun fromBuildConfig(): AppConfig = AppConfig(
            apiBaseUrl = BuildConfig.API_BASE_URL,
            enableLogs = BuildConfig.ENABLE_LOGS.toBoolean() || BuildConfig.DEBUG,
        )
    }
}
