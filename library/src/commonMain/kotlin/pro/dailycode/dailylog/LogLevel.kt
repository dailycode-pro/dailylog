package pro.dailycode.dailylog

/**
 * Logging severity levels, ordered from least to most severe.
 */
public enum class LogLevel(public val priority: Int) {
    DEBUG(0),
    INFO(1),
    WARNING(2),
    ERROR(3)
}
