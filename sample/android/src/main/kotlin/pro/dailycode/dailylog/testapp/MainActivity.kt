package pro.dailycode.dailylog.testapp

import android.app.Activity
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import pro.dailycode.dailylog.DailyLogger
import pro.dailycode.dailylog.LogLevel

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            setPadding(32, 32, 32, 32)
            textSize = 14f
            typeface = android.graphics.Typeface.MONOSPACE
        }

        val scrollView = ScrollView(this).apply {
            addView(textView)
        }

        setContentView(scrollView)

        val output = StringBuilder()

        fun log(msg: String) {
            output.appendLine(msg)
        }

        log("======================================")
        log("     DailyLogger TestApp (Android)")
        log("======================================\n")

        log("All log output goes to Logcat.\n")

        runDemo("Basic usage", ::log) {
            DailyLogger.tag = "DailyLogger"
            DailyLogger.prefix = null
            DailyLogger.minLevel = LogLevel.DEBUG
            DailyLogger.d("Application started")
            DailyLogger.i("User logged in")
            DailyLogger.w("Memory usage is high")
            DailyLogger.e("Failed to save data")
        }

        runDemo("With prefix", ::log) {
            DailyLogger.tag = "DailyLogger"
            DailyLogger.prefix = "MyApp"
            DailyLogger.d("Application started")
            DailyLogger.i("User logged in")
            DailyLogger.w("Memory usage is high")
            DailyLogger.e("Failed to save data")
        }

        runDemo("Custom default tag", ::log) {
            DailyLogger.tag = "Network"
            DailyLogger.prefix = null
            DailyLogger.d("Preparing request")
            DailyLogger.i("GET /api/users -> 200 OK")
            DailyLogger.w("Slow response: 3200ms")
            DailyLogger.e("Connection timeout after 30s")
        }

        runDemo("Per-message tag override", ::log) {
            DailyLogger.tag = "App"
            DailyLogger.prefix = null
            DailyLogger.i("Starting module initialization")
            DailyLogger.i("Database connected", tag = "Database")
            DailyLogger.i("Cache warmed up", tag = "Cache")
            DailyLogger.w("Feature flag missing", tag = "Config")
            DailyLogger.e("Migration failed", tag = "Database")
        }

        runDemo("Message prefix", ::log) {
            DailyLogger.tag = "App"
            DailyLogger.prefix = null
            DailyLogger.minLevel = LogLevel.DEBUG
            DailyLogger.d("Connecting to db", messagePrefix = "Init")
            DailyLogger.i("User authenticated", messagePrefix = "Auth")
            DailyLogger.w("Token expires soon", tag = "Network", messagePrefix = "JWT")
            DailyLogger.e("Table not found", tag = "Database", messagePrefix = "SQL")
        }

        runDemo("Min level filtering (WARNING)", ::log) {
            DailyLogger.tag = "Filtered"
            DailyLogger.prefix = null
            DailyLogger.minLevel = LogLevel.WARNING
            log("  (DEBUG and INFO should NOT appear in Logcat)")
            DailyLogger.d("This should NOT appear")
            DailyLogger.i("This should NOT appear either")
            DailyLogger.w("This SHOULD appear")
            DailyLogger.e("This SHOULD also appear")
        }

        runDemo("Per-message tags", ::log) {
            DailyLogger.tag = "App"
            DailyLogger.prefix = null
            DailyLogger.minLevel = LogLevel.DEBUG
            DailyLogger.i("Sending request", tag = "Network")
            DailyLogger.d("Query: SELECT * FROM users", tag = "Database")
            DailyLogger.i("Screen rendered in 16ms", tag = "UI")
            DailyLogger.w("Retry attempt 2/3", tag = "Network")
            DailyLogger.e("Deadlock detected!", tag = "Database")
        }

        log("======================================")
        log("     All demos completed!")
        log("     Check Logcat for log output.")
        log("======================================")

        textView.text = output.toString()
    }

    private fun runDemo(name: String, log: (String) -> Unit, block: () -> Unit) {
        val separator = "\u2500".repeat(maxOf(0, 40 - name.length))
        log("-- $name $separator")
        try {
            block()
        } catch (e: Exception) {
            log("  FAILED: ${e.message}")
        }
        log("")
    }
}
