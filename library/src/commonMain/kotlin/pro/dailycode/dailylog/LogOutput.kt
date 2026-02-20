package pro.dailycode.dailylog

/**
 * Platform-specific log output mechanism.
 */
public expect fun platformLogOutput(level: LogLevel, tag: String, message: String)
