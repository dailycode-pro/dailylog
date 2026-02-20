package pro.dailycode.dailylog.testapp

import pro.dailycode.dailylog.DailyLogger
import pro.dailycode.dailylog.LogLevel

fun main() {
    println("======================================")
    println("     DailyLogger TestApp v1.0.0")
    println("======================================\n")

    runDemo("Basic usage with default tag") {
        DailyLogger.tag = "DailyLogger"
        DailyLogger.minLevel = LogLevel.DEBUG
        DailyLogger.debug(message = "Application started")
        DailyLogger.info(message = "User logged in")
        DailyLogger.warning(message = "Memory usage is high")
        DailyLogger.error(message = "Failed to save data")
    }

    runDemo("Custom default tag") {
        DailyLogger.tag = "Network"
        DailyLogger.debug(message = "Preparing request")
        DailyLogger.info(message = "GET /api/users -> 200 OK")
        DailyLogger.warning(message = "Slow response: 3200ms")
        DailyLogger.error(message = "Connection timeout after 30s")
    }

    runDemo("Per-message tag override") {
        DailyLogger.tag = "App"
        DailyLogger.info(message = "Starting module initialization")
        DailyLogger.info("Database", "Database connected")
        DailyLogger.info("Cache", "Cache warmed up")
        DailyLogger.warning("Config", "Feature flag missing")
        DailyLogger.error("Database", "Migration failed")
    }

    runDemo("Min level filtering (WARNING)") {
        DailyLogger.tag = "Filtered"
        DailyLogger.minLevel = LogLevel.WARNING
        println("  (DEBUG and INFO should NOT appear below)")
        DailyLogger.debug(message = "This should NOT appear")
        DailyLogger.info(message = "This should NOT appear either")
        DailyLogger.warning(message = "This SHOULD appear")
        DailyLogger.error(message = "This SHOULD also appear")
    }

    runDemo("Min level filtering (ERROR only)") {
        DailyLogger.tag = "ErrorsOnly"
        DailyLogger.minLevel = LogLevel.ERROR
        println("  (Only ERROR should appear below)")
        DailyLogger.debug(message = "Filtered out")
        DailyLogger.info(message = "Filtered out")
        DailyLogger.warning(message = "Filtered out")
        DailyLogger.error(message = "Critical system failure!")
    }

    runDemo("Per-message tags with singleton") {
        DailyLogger.tag = "App"
        DailyLogger.minLevel = LogLevel.DEBUG
        DailyLogger.info("Network", "Sending request")
        DailyLogger.debug("Database", "Query: SELECT * FROM users")
        DailyLogger.info("UI", "Screen rendered in 16ms")
        DailyLogger.warning("Network", "Retry attempt 2/3")
        DailyLogger.error("Database", "Deadlock detected!")
    }

    runDemo("Edge cases") {
        DailyLogger.tag = "Edge"
        DailyLogger.minLevel = LogLevel.DEBUG
        DailyLogger.info(message = "")
        DailyLogger.info(message = "Unicode: \u0410\u0411\u0412 \u65E5\u672C\u8A9E \uD83D\uDE80")
        DailyLogger.info(message = "Long message: ${"A".repeat(200)}")
        DailyLogger.info("", "Logger with empty tag")
    }

    println("\n======================================")
    println("     All demos completed!")
    println("======================================")
}

private fun runDemo(name: String, block: () -> Unit) {
    println("-- $name ${"─".repeat(maxOf(0, 40 - name.length))}")
    try {
        block()
    } catch (e: Exception) {
        println("  FAILED: ${e.message}")
    }
    println()
}
