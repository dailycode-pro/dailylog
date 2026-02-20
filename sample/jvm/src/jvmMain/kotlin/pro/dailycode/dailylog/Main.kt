package pro.dailycode.dailylog.testapp

import pro.dailycode.dailylog.DailyLogger
import pro.dailycode.dailylog.LogLevel

fun main() {
    println("======================================")
    println("     DailyLogger TestApp v1.0.0")
    println("======================================\n")

    runDemo("Basic usage with default tag") {
        DailyLogger.tag = "DailyLogger"
        DailyLogger.prefix = null
        DailyLogger.minLevel = LogLevel.DEBUG
        DailyLogger.d("Application started")
        DailyLogger.i("User logged in")
        DailyLogger.w("Memory usage is high")
        DailyLogger.e("Failed to save data")
    }

    runDemo("With prefix") {
        DailyLogger.tag = "DailyLogger"
        DailyLogger.prefix = "MyApp"
        DailyLogger.minLevel = LogLevel.DEBUG
        DailyLogger.d("Application started")
        DailyLogger.i("User logged in")
        DailyLogger.w("Memory usage is high")
        DailyLogger.e("Failed to save data")
    }

    runDemo("Custom default tag") {
        DailyLogger.tag = "Network"
        DailyLogger.prefix = null
        DailyLogger.d("Preparing request")
        DailyLogger.i("GET /api/users -> 200 OK")
        DailyLogger.w("Slow response: 3200ms")
        DailyLogger.e("Connection timeout after 30s")
    }

    runDemo("Per-message tag override") {
        DailyLogger.tag = "App"
        DailyLogger.prefix = null
        DailyLogger.i("Starting module initialization")
        DailyLogger.i("Database connected", tag = "Database")
        DailyLogger.i("Cache warmed up", tag = "Cache")
        DailyLogger.w("Feature flag missing", tag = "Config")
        DailyLogger.e("Migration failed", tag = "Database")
    }

    runDemo("Prefix + per-message tag") {
        DailyLogger.tag = "App"
        DailyLogger.prefix = "MyApp"
        DailyLogger.i("Starting module initialization")
        DailyLogger.i("Database connected", tag = "Database")
        DailyLogger.w("Feature flag missing", tag = "Config")
        DailyLogger.e("Migration failed", tag = "Database")
    }

    runDemo("Message prefix (messagePrefix)") {
        DailyLogger.tag = "App"
        DailyLogger.prefix = null
        DailyLogger.minLevel = LogLevel.DEBUG
        DailyLogger.d("Connecting to db", messagePrefix = "Init")
        DailyLogger.i("User authenticated", messagePrefix = "Auth")
        DailyLogger.w("Token expires soon", tag = "Network", messagePrefix = "JWT")
        DailyLogger.e("Table not found", tag = "Database", messagePrefix = "SQL")
    }

    runDemo("Min level filtering (WARNING)") {
        DailyLogger.tag = "Filtered"
        DailyLogger.prefix = null
        DailyLogger.minLevel = LogLevel.WARNING
        println("  (DEBUG and INFO should NOT appear below)")
        DailyLogger.d("This should NOT appear")
        DailyLogger.i("This should NOT appear either")
        DailyLogger.w("This SHOULD appear")
        DailyLogger.e("This SHOULD also appear")
    }

    runDemo("Min level filtering (ERROR only)") {
        DailyLogger.tag = "ErrorsOnly"
        DailyLogger.minLevel = LogLevel.ERROR
        println("  (Only ERROR should appear below)")
        DailyLogger.d("Filtered out")
        DailyLogger.i("Filtered out")
        DailyLogger.w("Filtered out")
        DailyLogger.e("Critical system failure!")
    }

    runDemo("Per-message tags with singleton") {
        DailyLogger.tag = "App"
        DailyLogger.prefix = null
        DailyLogger.minLevel = LogLevel.DEBUG
        DailyLogger.i("Sending request", tag = "Network")
        DailyLogger.d("Query: SELECT * FROM users", tag = "Database")
        DailyLogger.i("Screen rendered in 16ms", tag = "UI")
        DailyLogger.w("Retry attempt 2/3", tag = "Network")
        DailyLogger.e("Deadlock detected!", tag = "Database")
    }

    runDemo("Edge cases") {
        DailyLogger.tag = "Edge"
        DailyLogger.prefix = null
        DailyLogger.minLevel = LogLevel.DEBUG
        DailyLogger.i("")
        DailyLogger.i("Unicode: АБВ 日本語 \uD83D\uDE80")
        DailyLogger.i("Long message: ${"A".repeat(200)}")
        DailyLogger.i("Logger with empty tag", tag = "")
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
