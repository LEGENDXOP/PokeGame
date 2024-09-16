package com.legendx.pokehexa.mainworkers

import com.google.gson.annotations.SerializedName

data class Pokemon(
    val name: String,
    val id: Int,
    val height: Int,
    val weight: Int,
    val abilities: List<String>,
    val types: List<String>,
    val stats: Stats,
    val moves: List<Move>
)

data class Stats(
    val hp: Int,
    val attack: Int,
    val defense: Int,
    @SerializedName("special_attack") val specialAttack: Int,
    @SerializedName("special_defense") val specialDefense: Int,
    val speed: Int
)

data class Move(
    val move: String,
    @SerializedName("version_group") val versionGroup: String,
    @SerializedName("level_learned_at") val levelLearnedAt: Int,
    val power: Int,
)

data class Moves(
    val name: String,
    val id: Int,
    val type: String,
    val power: Int,
    val pp: Int,
    val accuracy: Int,
    val priority: Int,
    @SerializedName("damage_class") val damageClass: String,
    val effect: String,
    val target: String
)

data class PokemonSpecies(
    val name: String,
    val id: Int,
    @SerializedName("capture_rate") val captureRate: Int,
    val habitat: String,
    @SerializedName("is_legendary") val isLegendary: Boolean,
    @SerializedName("is_mythical") val isMythical: Boolean,
    val color: String,
    val shape: String,
    @SerializedName("base_happiness") val baseHappiness: Int
)