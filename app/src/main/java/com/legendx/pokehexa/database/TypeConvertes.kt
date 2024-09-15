package com.legendx.pokehexa.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.legendx.pokehexa.mainworkers.Move
import com.legendx.pokehexa.mainworkers.PokeBallsCategory
import com.legendx.pokehexa.mainworkers.Stats
import com.legendx.pokehexa.mainworkers.UserPokeBalls
import com.legendx.pokehexa.mainworkers.UserPokemon

class Converters {

    private val gson = Gson()

    // Convert List<UserPokeBalls> to String
    @TypeConverter
    fun fromPokeBallsList(pokeBalls: List<UserPokeBalls>): String {
        return gson.toJson(pokeBalls)
    }

    // Convert String to List<UserPokeBalls>
    @TypeConverter
    fun toPokeBallsList(data: String): List<UserPokeBalls> {
        val listType = object : TypeToken<List<UserPokeBalls>>() {}.type
        return gson.fromJson(data, listType)
    }

    // Convert List<UserPokemon> to String
    @TypeConverter
    fun fromPokemonList(pokemons: List<UserPokemon>): String {
        return gson.toJson(pokemons)
    }

    // Convert String to List<UserPokemon>
    @TypeConverter
    fun toPokemonList(data: String): List<UserPokemon> {
        val listType = object : TypeToken<List<UserPokemon>>() {}.type
        return gson.fromJson(data, listType)
    }

    // Convert List<String> (for abilities and types) to String
    @TypeConverter
    fun fromStringList(strings: List<String>): String {
        return gson.toJson(strings)
    }

    // Convert String to List<String>
    @TypeConverter
    fun toStringList(data: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(data, listType)
    }

    // Convert PokeBallsCategory enum to String
    @TypeConverter
    fun fromPokeBallsCategory(category: PokeBallsCategory): String {
        return category.name
    }

    // Convert String to PokeBallsCategory enum
    @TypeConverter
    fun toPokeBallsCategory(category: String): PokeBallsCategory {
        return PokeBallsCategory.valueOf(category)
    }

    // Convert List<Move> to String
    @TypeConverter
    fun fromMoveList(moves: List<Move>): String {
        return gson.toJson(moves)
    }

    // Convert String to List<Move>
    @TypeConverter
    fun toMoveList(data: String): List<Move> {
        val listType = object : TypeToken<List<Move>>() {}.type
        return gson.fromJson(data, listType)
    }

    // Convert Stats to String
    @TypeConverter
    fun fromStats(stats: Stats): String {
        return gson.toJson(stats)
    }

    // Convert String to Stats
    @TypeConverter
    fun toStats(data: String): Stats {
        return gson.fromJson(data, Stats::class.java)
    }

}
