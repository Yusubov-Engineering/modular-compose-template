package com.example.modularapp.core.logger

/**
 * The logging contract. Feature code logs through this and never names the
 * library behind it (Kermit, in `logger-impl`).
 *
 * Never log what the user typed or said, tokens, or anything personal —
 * logs end up in bug reports.
 */
interface AppLogger {
    fun debug(message: String, tag: String? = null)

    fun info(message: String, tag: String? = null)

    fun warning(message: String, throwable: Throwable? = null, tag: String? = null)

    fun error(message: String, throwable: Throwable? = null, tag: String? = null)

    /** A logger that drops everything — for tests and previews. */
    object Silent : AppLogger {
        override fun debug(message: String, tag: String?) = Unit

        override fun info(message: String, tag: String?) = Unit

        override fun warning(message: String, throwable: Throwable?, tag: String?) = Unit

        override fun error(message: String, throwable: Throwable?, tag: String?) = Unit
    }
}
