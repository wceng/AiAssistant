package com.wceng.app.aiassistant

import android.app.Application
import com.wceng.app.aiassistant.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class AiaApplication : Application() {

    companion object {
        @JvmStatic
        lateinit var instance: AiaApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initKoin {
            androidLogger()
            androidContext(this@AiaApplication)
        }
    }
}
