package com.legendx.pokehexa.setup.screens

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.legendx.pokehexa.R
import com.legendx.pokehexa.database.DataBaseBuilder
import com.legendx.pokehexa.database.UserTable
import com.legendx.pokehexa.mainworkers.DataCache
import com.legendx.pokehexa.mainworkers.PokeBallsCategory
import com.legendx.pokehexa.mainworkers.Pokemon
import com.legendx.pokehexa.mainworkers.UserPokeBalls
import com.legendx.pokehexa.mainworkers.UserPokemon
import com.legendx.pokehexa.setup.screens.ui.theme.PokeHexaGameTheme
import com.legendx.pokehexa.setup.viewmodels.SetupPokeViewModel
import kotlinx.coroutines.launch

class SelectData : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeHexaGameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DataSelection(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DataSelection(modifier: Modifier) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        val context = LocalContext.current
        val userDao = DataBaseBuilder.getDataBase(context).userDao()
        val scope = rememberCoroutineScope()
        val scrollState = rememberLazyListState()
        val pokeModel = viewModel<SetupPokeViewModel>()

        LaunchedEffect(true) {
            pokeModel.startersPokemons.addAll(
                listOf(
                    DataCache.pokemonList.find { it.name == "bulbasaur" }!!,
                    DataCache.pokemonList.find { it.name == "charmander" }!!,
                    DataCache.pokemonList.find { it.name == "squirtle" }!!,
                    DataCache.pokemonList.find { it.name == "pikachu" }!!
                )
            )

        }

        LazyRow(state = scrollState) {
            items(pokeModel.startersPokemons.size) { poke ->
                ChoosePokemon(pokeModel.startersPokemons[poke]) { pokemon ->
                    pokeModel.pokeData = pokemon
                    println(pokeModel.pokeData)
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = {
            if (pokeModel.pokeData != null) {
                val pokeBall = UserPokeBalls(
                    name = PokeBallsCategory.PokeBall,
                    id = 1,
                    quantity = 20,
                )
                val pokeMine = UserPokemon(
                    name = pokeModel.pokeData!!.name,
                    id =  pokeModel.pokeData!!.id,
                    level = 15,
                    experience = 21,
                    moves =  pokeModel.pokeData!!.moves,
                    stats =  pokeModel.pokeData!!.stats,
                    abilities =  pokeModel.pokeData!!.abilities,
                    types =  pokeModel.pokeData!!.types,
                    height =  pokeModel.pokeData!!.height,
                    weight =  pokeModel.pokeData!!.weight
                )
                val pokeTable = UserTable(
                    id = 1,
                    name = "legendx",
                    level = 1,
                    experience = 1,
                    pokeCash = 100,
                    pokeBalls = listOf(pokeBall),
                    totalPokemons = listOf(pokeMine)
                )
                scope.launch {
                    println("Data: $pokeTable")
                    userDao.addUserData(pokeTable)
                    println("data added")
                }.invokeOnCompletion {
                    (context as Activity).finish()
                }
            }
        }) {
            Text(text = "Save Data")
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (pokeModel.pokeData != null){
            Text(text = "Selected Pokemon: ${pokeModel.pokeData!!.name}")
        }
    }
}


@Composable
fun ChoosePokemon(pokemon: Pokemon, pokeData: (Pokemon) -> Unit) {
    val image = painterResource(id = R.drawable.mega_mewtwo_x)
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = image,
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .clickable {
                    pokeData(pokemon)
                }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = pokemon.name)
    }
}

