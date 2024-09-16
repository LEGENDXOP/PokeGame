package com.legendx.pokehexa.mainworkers

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader


object DataCache {
    val pokemonList = mutableListOf<Pokemon>()
    val movesList = mutableListOf<Moves>()
    val speciesList = mutableListOf<PokemonSpecies>()

    fun loadDataFromJson(context: Context) {
        val gson = Gson()

        val inputStreamPokemon = context.assets.open("pokemon.json")
        val inputStreamMoves = context.assets.open("moves.json")
        val inputStreamSpecies = context.assets.open("pokemon_species.json")

        val readerPokemon = BufferedReader(inputStreamPokemon.reader())
        val readerMoves = BufferedReader(inputStreamMoves.reader())
        val readerSpecies = BufferedReader(inputStreamSpecies.reader())

        val pokemonJson = readerPokemon.use { it.readText() }
        val movesJson = readerMoves.use { it.readText() }
        val speciesJson = readerSpecies.use { it.readText() }

        val pokemonType = object : TypeToken<List<Pokemon>>() {}.type
        val movesType = object : TypeToken<List<Moves>>() {}.type
        val speciesType = object : TypeToken<List<PokemonSpecies>>() {}.type

        pokemonList.addAll(gson.fromJson(pokemonJson, pokemonType))
        movesList.addAll(gson.fromJson(movesJson, movesType))
        speciesList.addAll(gson.fromJson(speciesJson, speciesType))
    }
}