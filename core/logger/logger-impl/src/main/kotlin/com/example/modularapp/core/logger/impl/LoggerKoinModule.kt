package com.example.modularapp.core.logger.impl

import com.example.modularapp.core.logger.AppLogger
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/** Needs a [LoggerConfig] from the app; provides [AppLogger]. */
@Module
class LoggerKoinModule {
    @Single
    fun logger(config: LoggerConfig): AppLogger = KermitAppLogger(config)
}
