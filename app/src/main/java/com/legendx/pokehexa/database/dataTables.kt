package com.legendx.pokehexa.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.legendx.pokehexa.mainworkers.PokeBalls
import com.legendx.pokehexa.mainworkers.UserPokeBalls
import com.legendx.pokehexa.mainworkers.UserPokemon

@Entity(tableName = "userData")
data class UserTable (
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val name: String,
    val level: Int,
    val experience: Int,
    val pokeCash: Int,
    val pokeBalls: List<UserPokeBalls>,
    val totalPokemons: List<UserPokemon>,
)