package pro.dailycode.dailylog

public actual fun platformLogOutput(level: LogLevel, tag: String, message: String) {
    val prefix = when (level) {
        LogLevel.DEBUG -> "DEBUG"
        LogLevel.INFO -> "INFO"
        LogLevel.WARNING -> "WARN"
        LogLevel.ERROR -> "ERROR"
    }
    val output = "$prefix [$tag] $message"
    when (level) {
        LogLevel.ERROR, LogLevel.WARNING -> System.err.println(output)
        else -> println(output)
    }
}
