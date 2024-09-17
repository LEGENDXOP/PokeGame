package com.legendx.pokehexa.tools

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

object DataStoreManager {
    private const val PREFERENCES_NAME = "pokehexa_preferences"
    private val SETUP_COMPLETED = stringPreferencesKey("setup_completed")
    private val Context.dataStore by preferencesDataStore(PREFERENCES_NAME)

    suspend fun saveSetup(context: Context, isSetupCompleted: Boolean){
        context.applicationContext.dataStore.edit {
            it[SETUP_COMPLETED] = isSetupCompleted.toString()
        }
    }

    suspend fun getSetup(context: Context): Boolean{
        val preferences = context.applicationContext.dataStore.data.first()
        return preferences[SETUP_COMPLETED]?.toBoolean() ?: false
    }

    suspend fun deleteSetup(context: Context){
        context.applicationContext.dataStore.edit {
            it.remove(SETUP_COMPLETED)
        }
    }
}