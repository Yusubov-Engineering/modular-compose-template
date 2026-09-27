package com.example.modularapp.core.network.impl

import com.example.modularapp.core.logger.AppLogger
import com.example.modularapp.core.network.NetworkClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/** Needs a [NetworkConfig] from the app and an [AppLogger]; provides [NetworkClient]. */
@Module
class NetworkKoinModule {
    @Single
    fun networkClient(config: NetworkConfig, logger: AppLogger): NetworkClient =
        KtorNetworkClient(HttpClientFactory.create(config, logger))
}
