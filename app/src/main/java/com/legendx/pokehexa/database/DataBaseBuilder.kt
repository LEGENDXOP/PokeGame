package com.legendx.pokehexa.database

import android.content.Context
import androidx.room.Room

object DataBaseBuilder {
    private var isInstanceCreated: AppDataBase? = null
    fun getDataBase(context: Context): AppDataBase {
        if (isInstanceCreated == null) {
            synchronized(AppDataBase::class.java) {
                isInstanceCreated = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "pokehexa"
                ).fallbackToDestructiveMigration().build()
            }
        }
        return isInstanceCreated!!
    }
}