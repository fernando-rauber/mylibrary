package uk.fernando.library

import android.app.Application

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

//        CrashActivity.setUp(MainActivity::class.java, Color.Red)
//        GlobalExceptionHandler.initialize(this)
    }
}