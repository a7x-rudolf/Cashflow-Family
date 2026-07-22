package com.app.cashflowfamily.utils

import android.util.Log
import com.app.cashflowfamily.BuildConfig


@Suppress("unused")
object AppLogger {

    private val DEBUG_ENABLED = BuildConfig.DEBUG

    fun d(tag: String, message: String) {
        if (DEBUG_ENABLED) {
            Log.d(tag, message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (DEBUG_ENABLED) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }

    fun i(tag: String, message: String) {
        if (DEBUG_ENABLED) {
            Log.i(tag, message)
        }
    }

    fun w(tag: String, message: String) {
        if (DEBUG_ENABLED) {
            Log.w(tag, message)
        }
    }
}