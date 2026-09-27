package com.example.modularapp.core.logger.impl

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import com.example.modularapp.core.logger.AppLogger

/** [AppLogger] over Kermit, writing to Logcat. */
internal class KermitAppLogger(config: LoggerConfig) : AppLogger {
    private val logger = Logger(
        config = StaticConfig(
            minSeverity = if (config.enabled) Severity.Debug else Severity.Error,
            logWriterList = listOf(platformLogWriter()),
        ),
        tag = config.tag,
    )

    override fun debug(message: String, tag: String?) {
        logger.d(tag = tag ?: logger.tag) { message }
    }

    override fun info(message: String, tag: String?) {
        logger.i(tag = tag ?: logger.tag) { message }
    }

    override fun warning(message: String, throwable: Throwable?, tag: String?) {
        logger.w(throwable = throwable, tag = tag ?: logger.tag) { message }
    }

    override fun error(message: String, throwable: Throwable?, tag: String?) {
        logger.e(throwable = throwable, tag = tag ?: logger.tag) { message }
    }
}
