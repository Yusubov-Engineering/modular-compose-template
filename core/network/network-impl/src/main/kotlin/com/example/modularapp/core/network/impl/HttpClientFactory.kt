package com.example.modularapp.core.network.impl

import com.example.modularapp.core.logger.AppLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging

/**
 * Builds the one [HttpClient] the app uses. [engine] is a parameter so tests
 * can pass Ktor's `MockEngine` and exercise the real configuration.
 */
internal object HttpClientFactory {
    fun create(
        config: NetworkConfig,
        logger: AppLogger,
        engine: HttpClientEngine = OkHttp.create(),
    ): HttpClient = HttpClient(engine) {
        expectSuccess = true

        defaultRequest {
            // A trailing slash makes relative paths ("posts") resolve under it.
            url(config.baseUrl.trimEnd('/') + "/")
        }

        install(HttpTimeout) {
            requestTimeoutMillis = config.timeoutMillis
            connectTimeoutMillis = config.timeoutMillis
        }

        if (config.enableLogging) {
            install(Logging) {
                level = LogLevel.INFO
                this.logger = object : Logger {
                    override fun log(message: String) = logger.debug(message, tag = "HTTP")
                }
            }
        }
    }
}
