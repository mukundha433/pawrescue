package com.example.util

import android.util.Log

object CrashLogger {
    private const val TAG = "PawRescue"

    fun logError(
        screen: String,
        action: String,
        throwable: Throwable?,
        contextInfo: Map<String, String>? = null
    ) {
        val safeContext = contextInfo?.entries
            ?.filterNot { (k, _) ->
                val lower = k.lowercase()
                lower.contains("password") || lower.contains("token") || lower.contains("key") || lower.contains("secret")
            }
            ?.joinToString(", ") { "${it.key}=${it.value}" } ?: "none"

        val msg = "[$screen] Error during '$action' (context: $safeContext): ${throwable?.localizedMessage ?: "Unknown error"}"
        Log.e(TAG, msg, throwable)
    }

    fun logWarning(screen: String, action: String, message: String) {
        Log.w(TAG, "[$screen] Warning during '$action': $message")
    }

    fun logInfo(screen: String, message: String) {
        Log.i(TAG, "[$screen] $message")
    }
}
