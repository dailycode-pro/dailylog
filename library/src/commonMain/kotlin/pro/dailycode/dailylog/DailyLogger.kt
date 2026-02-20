package pro.dailycode.dailylog

/**
 * A multiplatform singleton logger with tag support, optional prefix,
 * and configurable minimum log level.
 *
 * ## Usage
 *
 * ```kotlin
 * DailyLogger.minLevel = LogLevel.DEBUG
 * DailyLogger.prefix = "MyApp"
 *
 * DailyLogger.d("Request started")                                  // [MyApp/DailyLogger] Request started
 * DailyLogger.i("Response received", tag = "Network")               // [MyApp/Network] Response received
 * DailyLogger.w("Slow response: 3s")                                // [MyApp/DailyLogger] Slow response: 3s
 * DailyLogger.e("Connection failed", messagePrefix = "DB")          // [MyApp/DailyLogger] DB: Connection failed
 * ```
 */
public object DailyLogger {

    /**
     * Default tag used when no tag is specified per message.
     */
    public var tag: String = "DailyLogger"

    /**
     * Optional prefix prepended to the tag as `[prefix/tag]`.
     * When `null` or empty, the log format is `[tag]`.
     */
    public var prefix: String? = null

    /**
     * Minimum log level to output. Messages below this level are ignored.
     */
    public var minLevel: LogLevel = LogLevel.DEBUG

    /**
     * Log a message at [LogLevel.DEBUG] level.
     */
    public fun d(message: String, tag: String = this.tag, messagePrefix: String? = null) {
        log(LogLevel.DEBUG, message, tag, messagePrefix)
    }

    /**
     * Log a message at [LogLevel.INFO] level.
     */
    public fun i(message: String, tag: String = this.tag, messagePrefix: String? = null) {
        log(LogLevel.INFO, message, tag, messagePrefix)
    }

    /**
     * Log a message at [LogLevel.WARNING] level.
     */
    public fun w(message: String, tag: String = this.tag, messagePrefix: String? = null) {
        log(LogLevel.WARNING, message, tag, messagePrefix)
    }

    /**
     * Log a message at [LogLevel.ERROR] level.
     */
    public fun e(message: String, tag: String = this.tag, messagePrefix: String? = null) {
        log(LogLevel.ERROR, message, tag, messagePrefix)
    }

    /**
     * Log a message at the specified [level].
     *
     * Messages with a level below [minLevel] are silently ignored.
     */
    public fun log(level: LogLevel, message: String, tag: String = this.tag, messagePrefix: String? = null) {
        if (level.priority >= minLevel.priority) {
            val fullTag = if (!prefix.isNullOrEmpty()) "$prefix/$tag" else tag
            val fullMessage = if (!messagePrefix.isNullOrEmpty()) "$messagePrefix: $message" else message
            platformLogOutput(level, fullTag, fullMessage)
        }
    }
}
