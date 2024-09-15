package com.legendx.pokehexa.setup.screens

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.legendx.pokehexa.R
import com.legendx.pokehexa.database.DataBaseBuilder
import com.legendx.pokehexa.database.UserTable
import com.legendx.pokehexa.mainworkers.CodingHelpers
import com.legendx.pokehexa.mainworkers.DataCache
import com.legendx.pokehexa.mainworkers.PokeBalls
import com.legendx.pokehexa.mainworkers.PokeBallsCategory
import com.legendx.pokehexa.mainworkers.Pokemon
import com.legendx.pokehexa.mainworkers.UserPokeBalls
import com.legendx.pokehexa.mainworkers.UserPokemon
import com.legendx.pokehexa.setup.screens.ui.theme.PokeHexaGameTheme
import kotlinx.coroutines.launch
import java.io.File

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
        var pokeData by remember { mutableStateOf<Pokemon?>(null) }
        val startersPokemons = remember { mutableStateListOf<Pokemon>() }
        val myPokes = remember { mutableStateListOf<UserTable>() }

        LaunchedEffect(true) {
            startersPokemons.addAll(
                listOf(
                    DataCache.pokemonList.find { it.name == "bulbasaur" }!!,
                    DataCache.pokemonList.find { it.name == "charmander" }!!,
                    DataCache.pokemonList.find { it.name == "squirtle" }!!,
                    DataCache.pokemonList.find { it.name == "pikachu" }!!
                )
            )
            userDao.getAllUserData().collect{
                myPokes.clear()
                myPokes.addAll(it)
                println(it)
            }
        }

        LazyRow(state = scrollState) {
            items(startersPokemons.size) { poke ->
                ChoosePokemon(startersPokemons[poke]) { pokemon ->
                    pokeData = pokemon
                    println(pokeData)
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = {
            if (pokeData != null) {
                val pokeBall = UserPokeBalls(
                    name = PokeBallsCategory.PokeBall,
                    id = 1,
                    quantity = 20,
                )
                val pokeMine = UserPokemon(
                    name = pokeData!!.name,
                    id = pokeData!!.id,
                    level = 15,
                    experience = 21,
                    moves = pokeData!!.moves,
                    stats = pokeData!!.stats,
                    abilities = pokeData!!.abilities,
                    types = pokeData!!.types,
                    height = pokeData!!.height,
                    weight = pokeData!!.weight
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
        if (myPokes.isNotEmpty()) {
            val fullDetail = "name: ${myPokes[0].totalPokemons[0].name}\n" +
                    "level: ${myPokes[0].totalPokemons[0].level}\n" +
                    "experience: ${myPokes[0].totalPokemons[0].experience}\n"
            Text(text = fullDetail)

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

