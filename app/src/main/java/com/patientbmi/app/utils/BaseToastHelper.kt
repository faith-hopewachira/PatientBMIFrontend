// BaseToastHelper.kt
package com.patientbmi.app.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.lang.ref.WeakReference

object BaseToastHelper {
    private const val TAG = "BaseToastHelper"
    private var toastRef: WeakReference<Toast>? = null
    private val handler = Handler(Looper.getMainLooper())

    /**
     * Shows a toast safely with built-in lifecycle protection
     */
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        try {
            // Cancel any existing toast to prevent overlap
            cancelCurrentToast()

            // Create and show new toast
            val toast = Toast.makeText(context.applicationContext, message, duration)
            toastRef = WeakReference(toast)
            toast.show()

            // Auto-cancel after duration to prevent leaks
            val cancelDelay = if (duration == Toast.LENGTH_LONG) 3500L else 2000L
            handler.postDelayed({
                cancelCurrentToast()
            }, cancelDelay)

        } catch (e: Exception) {
            Log.e(TAG, "Error showing toast: ${e.message}")
        }
    }

    /**
     * Shows a toast from Activity with built-in lifecycle check
     */
    fun showToastFromActivity(
        activity: android.app.Activity,
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "Activity destroyed, not showing toast: $message")
            return
        }

        showToast(activity, message, duration)
    }

    /**
     * Cancels current toast if exists
     */
    fun cancelCurrentToast() {
        try {
            toastRef?.get()?.cancel()
            toastRef = null
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling toast: ${e.message}")
        }
    }

    /**
     * Cleans up resources (call from Application class)
     */
    fun cleanup() {
        cancelCurrentToast()
        handler.removeCallbacksAndMessages(null)
    }
}