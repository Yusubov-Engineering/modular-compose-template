package com.example.modularapp

import android.app.Application
import com.example.modularapp.bootstrap.AppConfig
import com.example.modularapp.bootstrap.DependencyInjectionConfiguration
import com.example.modularapp.core.logger.AppLogger
import org.koin.core.Koin

/** Starts Koin once per process. */
class ModularApplication : Application() {
    internal lateinit var koin: Koin
        private set

    override fun onCreate() {
        super.onCreate()
        koin = DependencyInjectionConfiguration.initialize(AppConfig.fromBuildConfig())
        koin.get<AppLogger>().info("Started ${BuildConfig.FLAVOR} ${BuildConfig.VERSION_NAME}")
    }
}
