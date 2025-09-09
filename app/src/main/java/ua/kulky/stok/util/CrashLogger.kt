package ua.kulky.stok.util

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

object CrashLogger {
    fun init(ctx: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val dir = ctx.getExternalFilesDir(null) ?: ctx.filesDir
                val f = File(dir, "crash_${System.currentTimeMillis()}.log")
                val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                f.writeText(buildString {
                    appendLine("=== Crash ===")
                    appendLine("Time: $ts")
                    appendLine("Pkg: ${ctx.packageName}")
                    appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                    appendLine("Thread: ${t.name}")
                    appendLine()
                    appendLine(Log.getStackTraceString(e))
                })
            } catch (_: Exception) { /* ignore */ }
            // передати далі, щоб не зависнути у невідомому стані
            previous?.uncaughtException(t, e)
                ?: run { exitProcess(10) }
        }
    }
}
