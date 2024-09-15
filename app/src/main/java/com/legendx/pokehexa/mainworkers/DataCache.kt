package com.legendx.pokehexa.mainworkers

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader


object DataCache {
    val pokemonList = mutableListOf<Pokemon>()
    val movesList = mutableListOf<Moves>()

    fun loadDataFromJson(context: Context){
        val gson = Gson()
        val inputStreamPokemon = context.assets.open("pokemon.json")
        val inputStreamMoves = context.assets.open("moves.json")
        val readerPokemon = BufferedReader(inputStreamPokemon.reader())
        val readerMoves = BufferedReader(inputStreamMoves.reader())
        val pokemonJson = readerPokemon.use { it.readText() }
        val movesJson = readerMoves.use { it.readText() }

        val pokemonType = object : TypeToken<List<Pokemon>>() {}.type
        val movesType = object : TypeToken<List<Moves>>() {}.type
        pokemonList.addAll(gson.fromJson(pokemonJson, pokemonType))
        movesList.addAll(gson.fromJson(movesJson, movesType))
    }
}