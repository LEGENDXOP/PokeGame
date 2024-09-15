package com.legendx.pokehexa.mainworkers

import com.google.gson.Gson
import kotlin.random.Random

object CodingHelpers {
    fun <T> changeDataToString(data: T): String{
        val gson = Gson()
        return gson.toJson(data)
    }

    fun <T> changeStringToData(data: String, clazz: Class<T>): T{
        val gson = Gson()
        return gson.fromJson(data, clazz)
    }

    fun convertToUserPokemon(pokemon: Pokemon, maxLevel: Int = 10): UserPokemon {
        return UserPokemon(
            name = pokemon.name,
            id = pokemon.id,
            level = Random.nextInt(1, maxLevel),
            experience = Random.nextInt(1, 100),
            moves = pokemon.moves,
            stats = pokemon.stats,
            abilities = pokemon.abilities,
            types = pokemon.types,
            height = pokemon.height,
            weight = pokemon.weight,
            baseCatchRate = Random.nextInt(180, 255)
        )
    }
}

object PokeHelpers{
    private val typeChart: Map<String, Map<String, Double>> = mapOf(
        "Normal" to mapOf(
            "Rock" to 0.5,
            "Ghost" to 0.0,
            "Steel" to 0.5
        ),
        "Fire" to mapOf(
            "Fire" to 0.5,
            "Water" to 0.5,
            "Grass" to 2.0,
            "Ice" to 2.0,
            "Bug" to 2.0,
            "Rock" to 0.5,
            "Dragon" to 0.5,
            "Steel" to 2.0
        )
    )
    private fun calculateTypeEffectiveness(moveType: String, opponentTypes: List<String>): Double {
        var effectiveness = 1.0
        for (opponentType in opponentTypes) {
            val typeEffect = typeChart[moveType]?.get(opponentType) ?: 1.0
            effectiveness *= typeEffect
        }
        return effectiveness
    }
    fun calculateNewHp(
        currentHp: Int,
        move: Move,
        userPokemon: UserPokemon,
        opponentPokemon: UserPokemon
    ): Int {
        val level = userPokemon.level.toDouble()
        val attackStat = userPokemon.stats.attack.toDouble()
        val defenseStat = opponentPokemon.stats.defense.toDouble()
        val moveType = DataCache.movesList.find { it.name == move.move }!!.type
        val levelFactor = (2.0 * level / 5.0) + 2.0

        val baseDamage = ((levelFactor * move.power * attackStat / defenseStat) / 50.0) + 2.0

        val stab = if (moveType in userPokemon.types) 1.5 else 1.0

        val typeEffectiveness = calculateTypeEffectiveness(moveType, opponentPokemon.types)

        val modifier = stab * typeEffectiveness
        val totalDamage = (baseDamage * modifier).toInt()

        val newHp = currentHp - totalDamage
        return newHp.coerceAtLeast(0)
    }

    fun attemptCatch(
        enemyPokemon: UserPokemon,
        currentHP: Int,
        pokeball: UserPokeBalls
    ): Boolean {
        val maxHp = enemyPokemon.stats.hp.toDouble()
        val currentHp = currentHP.toDouble()

        // Check if Master Ball is used
        if (pokeball.name == PokeBallsCategory.MasterBall) {
            return true // Master Ball always catches
        }

        val ballModifier = pokeball.name.catchModifier
        val baseCatchRate = enemyPokemon.baseCatchRate!!.toDouble()

        // Simplified catch rate formula
        val catchRate = ((3 * maxHp - 2 * currentHp) * baseCatchRate * ballModifier) / (3 * maxHp)

        // Normalize catch rate to a probability between 0 and 1
        val catchProbability = catchRate / 255.0
        val catchProbabilityClamped = catchProbability.coerceIn(0.0, 1.0)

        // Generate a random number between 0 and 1
        val randomValue = Math.random() // between 0.0 and 1.0

        return randomValue < catchProbabilityClamped
    }
}