package pro.dailycode.dailylog

import platform.Foundation.NSLog

/**
 * iOS log output with heart emojis representing log level severity.
 *
 * - DEBUG:   🤍 (white heart)
 * - INFO:    💙 (blue heart)
 * - WARNING: 💛 (yellow heart)
 * - ERROR:   ❤️ (red heart)
 */
public actual fun platformLogOutput(level: LogLevel, tag: String, message: String) {
    val heart = when (level) {
        LogLevel.DEBUG -> "🤍"
        LogLevel.INFO -> "💙"
        LogLevel.WARNING -> "💛"
        LogLevel.ERROR -> "❤️"
    }
    NSLog("$heart [$tag] $message")
}
