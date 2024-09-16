package com.legendx.pokehexa

import android.app.Application
import com.legendx.pokehexa.mainworkers.DataCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            DataCache.loadDataFromJson(applicationContext)
            println("data loaded")
        }
    }
}

