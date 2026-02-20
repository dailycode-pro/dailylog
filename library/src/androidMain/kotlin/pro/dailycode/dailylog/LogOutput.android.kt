package pro.dailycode.dailylog

import android.util.Log

public actual fun platformLogOutput(level: LogLevel, tag: String, message: String) {
    try {
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message)
            LogLevel.INFO -> Log.i(tag, message)
            LogLevel.WARNING -> Log.w(tag, message)
            LogLevel.ERROR -> Log.e(tag, message)
        }
    } catch (_: RuntimeException) {
        // Fallback for unit test environments where android.util.Log is stubbed
        val prefix = when (level) {
            LogLevel.DEBUG -> "DEBUG"
            LogLevel.INFO -> "INFO"
            LogLevel.WARNING -> "WARN"
            LogLevel.ERROR -> "ERROR"
        }
        println("$prefix [$tag] $message")
    }
}
