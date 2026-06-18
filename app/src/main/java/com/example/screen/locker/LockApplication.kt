package com.example.screen.locker

import android.app.Application
import com.example.screen.locker.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@LockApplication)
            modules(appModule)
        }
    }
}
