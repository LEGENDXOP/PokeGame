package com.legendx.pokehexa.mainworkers


data class UserData(
    val name: String,
    val id: Int,
    val level: Int,
    val experience: Int,
    val pokeCash: Int,
    val pokeBalls: List<UserPokeBalls>,
    val totalPokemons: List<UserPokemon>,
)

data class UserPokemon(
    val name: String,
    val id: Int,
    val level: Int,
    val experience: Int,
    val moves: List<Move>,
    val stats: Stats,
    val abilities: List<String>,
    val types: List<String>,
    val height: Int,
    val weight: Int,
    val baseCatchRate: Int? = null
)

data class UserPokeBalls(
    val name: PokeBallsCategory,
    val id: Int,
    val quantity: Int,
)

data class PokeBalls(
    val name: PokeBallsCategory,
    val id: Int,
    val price: Int,
    val description: String? = null,
)