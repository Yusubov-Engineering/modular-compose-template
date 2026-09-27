package com.example.modularapp.bootstrap

import com.example.modularapp.core.logger.impl.LoggerConfig
import com.example.modularapp.core.logger.impl.LoggerKoinModule
import com.example.modularapp.core.network.impl.NetworkConfig
import com.example.modularapp.core.network.impl.NetworkKoinModule
import com.example.modularapp.features.counter.impl.CounterKoinModule
import com.example.modularapp.features.posts.impl.PostsKoinModule
import org.koin.core.Koin
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import org.koin.dsl.module
import org.koin.plugin.module.dsl.startKoin

/**
 * The one list of modules that exist at startup.
 *
 * A feature missing from `includes` still compiles — its routes are simply
 * never collected, and it has no screens. `./gradlew doctor` checks every
 * feature's module is listed here.
 *
 * The Koin compiler plugin validates this graph when the app compiles: a
 * definition asking for something no included module provides is a build
 * error.
 */
@Module(
    includes = [
        // Core
        LoggerKoinModule::class,
        NetworkKoinModule::class,
        // Features
        CounterKoinModule::class,
        PostsKoinModule::class,
        // <generated:koin-modules>
    ],
)
internal class AppKoinModule {
    // The flavor's settings enter the graph here, and nowhere else.
    // @Provided: AppConfig is declared at startKoin rather than by a module,
    // so compile-time validation must not look for a definition of it.
    @Single
    fun loggerConfig(@Provided config: AppConfig): LoggerConfig = LoggerConfig(enabled = config.enableLogs)

    @Single
    fun networkConfig(@Provided config: AppConfig): NetworkConfig =
        NetworkConfig(baseUrl = config.apiBaseUrl, enableLogging = config.enableLogs)
}

@KoinApplication(modules = [AppKoinModule::class])
internal object ModularKoinApplication

internal object DependencyInjectionConfiguration {
    fun initialize(config: AppConfig): Koin = startKoin<ModularKoinApplication> {
        modules(module { single { config } })
    }.koin
}
