package uk.fernando.logger

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

class AndroidLogger(override val logLevel: MyLogger.LogLevel = MyLogger.LogLevel.DEBUG) : MyLogger {

    override fun d(tag: String, msg: String) {
        if (isDebugEnabled()) Log.d("$tag 👍", msg)
    }

    override fun e(tag: String, msg: String) {
        if (isDebugEnabled()) Log.e("$tag 💀", msg)
    }

    override fun e(tag: String, msg: String, throwable: Throwable) {
        if (isDebugEnabled()) Log.e("$tag 💀", msg, throwable)
    }

    override fun addMessageToCrashlytics(tag: String, msg: String) {
        FirebaseCrashlytics.getInstance().log("$tag : $msg")
    }

    override fun addExceptionToCrashlytics(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }
}