package com.legendx.pokehexa.mainworkers

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.legendx.pokehexa.PokeHexa
import com.legendx.pokehexa.R
import com.legendx.pokehexa.database.DataBaseBuilder
import com.legendx.pokehexa.ui.theme.PokeHexaGameTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FightMode : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeHexaGameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FightModeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}


@Composable
fun FightModeScreen(modifier: Modifier) {
    val context = LocalContext.current
    val db = DataBaseBuilder.getDataBase(context)
    val pokeDao = db.userDao()
    val scope = rememberCoroutineScope()
    val pokeList by pokeDao.getAllUserData().collectAsState(initial = emptyList())
    var isFight by remember { mutableStateOf(false) }
    val enemyPoke = remember { DataCache.pokemonList.random() }

    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (!isFight) {
            Image(
                painter = painterResource(R.drawable.mega_mewtwo_x), contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
            )
            try {
                println(pokeList.first().totalPokemons)
            }catch (e: Exception){
                Unit
            }
            Text(text = enemyPoke.name, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = { isFight = true }) {
                Text(text = "Fight")
            }
        } else {
            val myPokeFirst = pokeList[0].totalPokemons[0]
            val myPokeMoves = myPokeFirst.moves.filter { it.levelLearnedAt <= myPokeFirst.level }
            val enemyPokeFirst = remember { CodingHelpers.convertToUserPokemon(enemyPoke) }
            val enemyPokeMoves =
                enemyPokeFirst.moves.filter { it.levelLearnedAt <= enemyPokeFirst.level }
            val finalMovesMine = remember { myPokeMoves.shuffled().take(4) }
            val finalMovesEnemy = remember { enemyPokeMoves.shuffled().take(4) }
            var myPokeHp by remember { mutableIntStateOf(myPokeFirst.stats.hp) }
            var enemyPokeHp by remember { mutableIntStateOf(enemyPokeFirst.stats.hp) }
            var isGameOver by remember { mutableStateOf(false) }
            val pokeBalls = remember { pokeList.first().pokeBalls.first().takeIf { it.quantity > 0 } }
            var pokeBallCurrent by remember { mutableIntStateOf(pokeBalls?.quantity?: 0) }
            Image(
                painter = painterResource(R.drawable.mega_mewtwo_x), contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
            )
            val fightDetailText =
                "${myPokeFirst.name} (${myPokeFirst.level}) vs ${enemyPokeFirst.name} (${enemyPokeFirst.level})\n${myPokeFirst.name} has $myPokeHp HP\n${enemyPokeFirst.name} has $enemyPokeHp HP"
            Text(text = fightDetailText, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            BattleButtons(listOfMoves = finalMovesMine) { move ->
                PokeHelpers.calculateNewHp(
                    enemyPokeHp,
                    move,
                    myPokeFirst,
                    enemyPokeFirst
                ).also {
                    if (it == 0) {
                        isGameOver = true
                    }
                    enemyPokeHp = it
                }
                scope.launch {
                    delay(1000)
                    try {
                        PokeHelpers.calculateNewHp(
                            myPokeHp,
                            finalMovesEnemy.random(),
                            enemyPokeFirst,
                            myPokeFirst
                        ).also {
                            if (it == 0) {
                                isGameOver = true
                            }
                            myPokeHp = it
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            }
            Spacer(modifier = Modifier.height(50.dp))
            Text(text = "Total Poke Balls: $pokeBallCurrent", modifier = Modifier.padding(16.dp)
                .clickable { buyPokeBallDialog(context) })
            Row {
                TextButton(onClick = {
                    Intent(context, PokeHexa::class.java).also {
                        context.startActivity(it)
                    }
                }) {
                    Text(text = "Run Away")
                }
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(onClick = {
                    if (pokeBallCurrent > 0){
                        scope.launch {
                            println("PokeBall: $pokeBallCurrent")
                            PokeHelpers.tryCatch(
                                enemyPokeFirst,
                                enemyPokeHp,
                                pokeBalls!!
                            ).also {
                                if (it){
                                    isGameOver = true
                                    enemyPokeFirst.baseCatchRate = null
                                    try {
                                        val updatedValue = pokeList.first().copy(
                                            totalPokemons = pokeList.first().totalPokemons + enemyPokeFirst
                                        )
                                        pokeDao.addUserData(updatedValue)
                                    }catch (e: Exception){
                                        e.printStackTrace()
                                    }
                                    println("Pokemon caught")
                                    println(pokeList)
                                    pokeCaughtDialog(context, enemyPokeFirst)
                                }else{
                                    withContext(Dispatchers.Main){
                                        Toast.makeText(context, "Failed to catch", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            pokeBallCurrent -= 1
                            println("updated pokeBall: $pokeBallCurrent")
                           pokeDao.addUserData(pokeList.first().copy(pokeBalls = listOf(pokeBalls.copy(quantity = pokeBallCurrent))))
                            println("finished work")
                        }
                    }else{
                        buyPokeBallDialog(context)
                    }
                }) {
                    Text(text = "Catch")
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BattleButtons(listOfMoves: List<Move>, onClick: (Move) -> Unit) {
    FlowRow(
        maxItemsInEachRow = 2,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOfMoves.forEach { move ->
            OutlinedButton(onClick = { onClick(move) }) {
                Text(text = move.move)
            }
        }
    }
}

fun buyPokeBallDialog(context: Context) {
    AlertDialog.Builder(context)
        .setTitle("Buy Poke Balls")
        .setMessage("Do you want to buy PokeBalls?")
        .setPositiveButton("Yes") { dialog, _ ->
            Intent(context, PokeHexa::class.java).also {
                context.startActivity(it)
            }
            dialog.dismiss()
        }
        .setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        .setIcon(R.drawable.mega_mewtwo_x)
        .show()

}
fun pokeCaughtDialog(context: Context, pokemon: UserPokemon) {
    AlertDialog.Builder(context)
        .setTitle("Pokemon Caught")
        .setMessage("You caught ${pokemon.name} with level ${pokemon.level}")
        .setPositiveButton("Ok") { dialog, _ ->
            Intent(context, PokeHexa::class.java).also {
                context.startActivity(it)
            }
            dialog.dismiss()
        }
        .setIcon(R.drawable.mega_mewtwo_x)
        .show()
}