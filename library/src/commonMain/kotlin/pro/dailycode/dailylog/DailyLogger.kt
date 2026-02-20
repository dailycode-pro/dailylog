package pro.dailycode.dailylog

/**
 * A multiplatform singleton logger with tag support and configurable minimum log level.
 *
 * ## Usage
 *
 * ```kotlin
 * DailyLogger.minLevel = LogLevel.DEBUG
 *
 * DailyLogger.debug("Network", "Request started")
 * DailyLogger.info("App", "Response received")
 * DailyLogger.warning("Slow response: 3s")
 * DailyLogger.error("Connection failed")
 * ```
 */
public object DailyLogger {

    /**
     * Default tag used when no tag is specified per message.
     */
    public var tag: String = "DailyLogger"

    /**
     * Minimum log level to output. Messages below this level are ignored.
     */
    public var minLevel: LogLevel = LogLevel.DEBUG

    /**
     * Log a message at [LogLevel.DEBUG] level.
     */
    public fun debug(tag: String = this.tag, message: String) {
        log(LogLevel.DEBUG, tag, message)
    }

    /**
     * Log a message at [LogLevel.INFO] level.
     */
    public fun info(tag: String = this.tag, message: String) {
        log(LogLevel.INFO, tag, message)
    }

    /**
     * Log a message at [LogLevel.WARNING] level.
     */
    public fun warning(tag: String = this.tag, message: String) {
        log(LogLevel.WARNING, tag, message)
    }

    /**
     * Log a message at [LogLevel.ERROR] level.
     */
    public fun error(tag: String = this.tag, message: String) {
        log(LogLevel.ERROR, tag, message)
    }

    /**
     * Log a message at the specified [level].
     *
     * Messages with a level below [minLevel] are silently ignored.
     */
    public fun log(level: LogLevel, tag: String = this.tag, message: String) {
        if (level.priority >= minLevel.priority) {
            platformLogOutput(level, tag, message)
        }
    }
}
