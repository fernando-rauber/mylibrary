package uk.fernando.logger

interface MyLogger {
    fun d(tag: String, msg: String)
    fun e(tag: String, msg: String)
    fun e(tag: String, msg: String, throwable: Throwable)

    fun isDebugEnabled() = logLevel.severity == LogLevel.DEBUG.severity
    fun isErrorEnabled() = logLevel.severity == LogLevel.ERROR.severity

    fun addMessageToCrashlytics(tag: String, msg: String)
    fun addExceptionToCrashlytics(throwable: Throwable)

    val logLevel: LogLevel

    enum class LogLevel(val severity: Int) {
        DEBUG(0), WARNING(1), ERROR(2)
    }
}