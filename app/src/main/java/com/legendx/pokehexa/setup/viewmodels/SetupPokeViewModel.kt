package com.legendx.pokehexa.setup.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.legendx.pokehexa.database.UserDataDao
import com.legendx.pokehexa.database.UserTable
import com.legendx.pokehexa.mainworkers.Pokemon
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SetupPokeViewModel : ViewModel() {
    var pokeData by mutableStateOf<Pokemon?>(null)
    val startersPokemons = mutableStateListOf<Pokemon>()
    val myPokes = mutableStateListOf<UserTable>()
    fun runApp(db: UserDataDao){
       viewModelScope.launch {
           db.getAllUserData().first().forEach{
               println(it)
           }
       }
    }
}
