package com.legendx.pokehexa.mainworkers.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.legendx.pokehexa.database.UserDataDao
import com.legendx.pokehexa.database.UserTable
import com.legendx.pokehexa.mainworkers.DataCache
import com.legendx.pokehexa.mainworkers.UserPokeBalls
import com.legendx.pokehexa.mainworkers.UserPokemon
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FightViewModel(private val userDao: UserDataDao) : ViewModel() {
    private val _userData = MutableStateFlow<UserTable?>(null)
    val userData = _userData.asStateFlow()
    private val _currentPokeBalls = MutableStateFlow(0)
    val currentPokeBalls = _currentPokeBalls.asStateFlow()
    private val _pokeCash = MutableStateFlow(0)
    val pokeCash = _pokeCash.asStateFlow()
    private val _myPokemons = MutableStateFlow<MutableList<UserPokemon>>(mutableListOf())
    val myPokemons = _myPokemons.asStateFlow()
    private val _myPokeBalls = MutableStateFlow<List<UserPokeBalls>>(emptyList())
    val myPokeBalls = _myPokeBalls.asStateFlow()
    private val _isFight = MutableStateFlow(false)
    val isFight = _isFight.asStateFlow()
    private val _isFightingProgress = MutableStateFlow(false)
    val isFightingProgress = _isFightingProgress.asStateFlow()
    private val _isCapturingProgress = MutableStateFlow(false)
    val isCapturingProgress = _isCapturingProgress.asStateFlow()
    private val _gameOver = MutableStateFlow(GameOver())
    val gameOver = _gameOver.asStateFlow()
    val enemyPoke = DataCache.pokemonList.random()

    init {
        viewModelScope.launch {
            userDao.getAllUserData().collect { users ->
                val user = users.firstOrNull()
                _userData.value = user
                user?.let {
                    _pokeCash.value = user.pokeCash
                    _myPokemons.value.addAll(user.totalPokemons)
                    _myPokeBalls.value = user.pokeBalls
                    _currentPokeBalls.value = user.pokeBalls.firstOrNull()?.quantity ?: 0
                }
            }
        }
    }

    data class GameOver(
        val isGameOver: Boolean = false,
        val isWin: Boolean = false,
        val isCaptured: Boolean = false
    )

    fun setIsFight(value: Boolean) {
        _isFight.value = value
    }

    fun setIsFightingProgress(value: Boolean) {
        _isFightingProgress.value = value
    }

    fun setIsCapturingProgress(value: Boolean) {
        _isCapturingProgress.value = value
    }

    fun setGameOver(gameOver: GameOver) {
        _gameOver.value = gameOver
    }

    suspend fun updatePokeCash(newPokeCash: Int) {
        _pokeCash.value = newPokeCash
        _userData.value?.let { user ->
            val updatedUser = user.copy(pokeCash = newPokeCash)
            userDao.addUserData(updatedUser)
        }
    }

    suspend fun updatePokeBalls(newQuantity: Int) {
        _currentPokeBalls.value = newQuantity
        _userData.value?.let { user ->
            val updatedPokeBalls = user.pokeBalls.map { pokeBall ->
                pokeBall.copy(quantity = newQuantity)
            }
            val updatedUser = user.copy(pokeBalls = updatedPokeBalls)
            userDao.addUserData(updatedUser)
        }
    }

    suspend fun addPokemon(newPokemon: UserPokemon) {
        val newList = _myPokemons.value
        newList.add(newPokemon)
        _userData.value?.let { user ->
            val updatedUser = user.copy(totalPokemons = newList)
            println("updated value: $updatedUser")
            userDao.addUserData(updatedUser)
            delay(1000L)
        }
    }

    fun purchasePokeBalls(quantity: Int, cost: Int, callBack: (isPurchased: Boolean) -> Unit) {
        viewModelScope.launch {
            val currentCash = _pokeCash.value
            if (currentCash >= cost) {
                val newCash = currentCash - cost
                updatePokeCash(newCash)
                delay(1000L)
                updatePokeBalls(_currentPokeBalls.value + quantity)
                callBack(true)
            } else {
                callBack(false)
            }
        }
    }

}


class FightViewModelFactory(private val userDao: UserDataDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(FightViewModel::class.java)) {
            return FightViewModel(userDao) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}