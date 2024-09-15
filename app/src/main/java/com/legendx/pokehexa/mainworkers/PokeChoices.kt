package com.legendx.pokehexa.mainworkers


enum class PokeBallsCategory(val catchModifier: Double) {
    PokeBall(1.0),
    GreatBall(1.5),
    UltraBall(2.0),
    MasterBall(Double.POSITIVE_INFINITY)
}