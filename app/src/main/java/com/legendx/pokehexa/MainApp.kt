package com.legendx.pokehexa

import android.app.Application
import com.legendx.pokehexa.learning.appModule
import com.legendx.pokehexa.mainworkers.DataCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApp : Application() {
    init {
        startKoin{
            androidLogger()
            androidContext(this@MainApp)
            modules(appModule)
        }
    }
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            DataCache.loadDataFromJson(applicationContext)
            println("data loaded")
        }
    }
}

