package com.legendx.pokehexa.setup.viewmodels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legendx.pokehexa.mainworkers.DataCache
import com.legendx.pokehexa.mainworkers.Pokemon
import com.legendx.pokehexa.tools.CodingHelpers
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
        val imageFile: Uri,
        val contentDescription: String? = null
    )

    fun getStarterPokemons(context: Context): List<PokeItem> {
        val data = startersPokemons.map {
            val imageFile = CodingHelpers.getPokeImage(context, it.id)
            PokeItem(it.name, imageFile = imageFile, contentDescription = it.name)
        }
        return data
    }
}
