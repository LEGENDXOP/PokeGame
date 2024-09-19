package com.legendx.pokehexa.setup.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legendx.pokehexa.mainworkers.DataCache
import com.legendx.pokehexa.mainworkers.Pokemon
import kotlinx.coroutines.launch

class SetupPokeViewModel : ViewModel() {
    var pokeData by mutableStateOf<Pokemon?>(null)
    val startersPokemons = mutableStateListOf<Pokemon>()

    init {
        viewModelScope.launch {
            startersPokemons.addAll(
                listOf(
                    DataCache.pokemonList.find { it.name == "bulbasaur" }!!,
                    DataCache.pokemonList.find { it.name == "charmander" }!!,
                    DataCache.pokemonList.find { it.name == "squirtle" }!!,
                    DataCache.pokemonList.find { it.name == "pikachu" }!!
                )
            )
        }
    }

    data class PokeItem(
        val name: String,
        val imageID: String,
        val contentDescription: String? = null
    )

    fun getStarterPokemons(context: Context): List<PokeItem>{
        val data = startersPokemons.map {
            val fileID = context.getExternalFilesDir(null)?.absolutePath + "/images/${it.id}.png"
            PokeItem(it.name, imageID = fileID, contentDescription = it.name)
        }
        return data
    }
}
