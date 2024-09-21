package com.legendx.pokehexa.mainworkers

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.legendx.pokehexa.PokeHexa
import com.legendx.pokehexa.R
import com.legendx.pokehexa.database.DataBaseBuilder
import com.legendx.pokehexa.mainworkers.viewmodels.FightViewModel
import com.legendx.pokehexa.mainworkers.viewmodels.FightViewModel.GameOver
import com.legendx.pokehexa.mainworkers.viewmodels.FightViewModelFactory
import com.legendx.pokehexa.tools.CodingHelpers
import com.legendx.pokehexa.tools.PokeHelpers
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


    @Composable
    fun FightModeScreen(modifier: Modifier) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val pokeDao = DataBaseBuilder.getDataBase(context).userDao()
        val fightVM = viewModel<FightViewModel>(
            factory = FightViewModelFactory(pokeDao)
        )
        val isFight by fightVM.isFight.collectAsStateWithLifecycle()
        val gameOver by fightVM.gameOver.collectAsStateWithLifecycle()
        val currentPokeBalls by fightVM.currentPokeBalls.collectAsStateWithLifecycle()
        val isFightingProgress by fightVM.isFightingProgress.collectAsStateWithLifecycle()
        val isCapturingProgress by fightVM.isCapturingProgress.collectAsStateWithLifecycle()
        val myPokemons by fightVM.myPokemons.collectAsStateWithLifecycle()
        val myPokeBalls by fightVM.myPokeBalls.collectAsStateWithLifecycle()
        val enemyPoke by fightVM.enemyPoke.collectAsStateWithLifecycle()
        println("on the start of compose total pokes are: ${myPokemons.size}")
        val imageFile = remember(enemyPoke) {
            CodingHelpers.getPokeImage(context, enemyPoke.id)
        }
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isFight) {
                AsyncImage(
                    model = imageFile, contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp)
                )
                Text(text = enemyPoke.name, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = { fightVM.setIsFight(true) }) {
                    Text(text = "Fight")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "My Pokemons (${myPokemons.size})", style = MaterialTheme.typography.headlineSmall)
                LazyRow {
                  items(myPokemons.size){
                      Box(modifier = Modifier.fillMaxWidth()
                      ){
                          Text(text = myPokemons[it].name, style = MaterialTheme.typography.headlineSmall)
                      }
                      Spacer(modifier = Modifier.width(16.dp))
                  }
                }
            } else if (gameOver.isGameOver) {
                val (gameText, amount) = when {
                    gameOver.isWin -> "You Defeated the enemy; you won 50 PokeCash" to 50
                    gameOver.isCaptured -> "You caught the enemy Pokémon; you won 100 PokeCash" to 100
                    else -> "You lost the battle; you will still get 20 PokeCash" to 20
                }
                WinGameScreen(fightVM, amount, gameText)
            } else {
                val myPokeFirst = myPokemons.first()

                val myPokeMoves = remember(myPokeFirst) {
                    myPokeFirst.moves.filter { it.levelLearnedAt <= myPokeFirst.level }
                }

                val enemyPokeFirst = remember(enemyPoke) {
                    CodingHelpers.convertToUserPokemon(enemyPoke, 20)
                }

                val enemyPokeMoves = remember(enemyPokeFirst) {
                    enemyPokeFirst.moves.filter { it.levelLearnedAt <= enemyPokeFirst.level }
                }

                val finalMovesMine = remember(myPokeMoves) { myPokeMoves.shuffled().take(4) }
                val finalMovesEnemy = remember(enemyPokeMoves) { enemyPokeMoves.shuffled().take(4) }

                var myPokeHp by remember(myPokeFirst) { mutableIntStateOf(myPokeFirst.stats.hp) }
                var enemyPokeHp by remember(enemyPokeFirst) { mutableIntStateOf(enemyPokeFirst.stats.hp) }

                AsyncImage(
                    model = imageFile,
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp)
                )
                val fightDetailText =
                    "${myPokeFirst.name} (${myPokeFirst.level}) vs ${enemyPokeFirst.name} (${enemyPokeFirst.level})\n\n${myPokeFirst.name} has $myPokeHp HP\n${enemyPokeFirst.name} has $enemyPokeHp HP"
                Text(text = fightDetailText, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                BattleButtons(listOfMoves = finalMovesMine) { move ->
                    if (isFightingProgress) return@BattleButtons
                    fightVM.setIsFightingProgress(true)
                    PokeHelpers.calculateNewHp(
                        enemyPokeHp,
                        move,
                        myPokeFirst,
                        enemyPokeFirst
                    ).also {
                        if (it == 0) {
                            fightVM.setGameOver(GameOver(isGameOver = true, isWin = true))
                        }
                        enemyPokeHp = it
                    }
                    scope.launch {
                        delay(500)
                        try {
                            PokeHelpers.calculateNewHp(
                                myPokeHp,
                                finalMovesEnemy.random(),
                                enemyPokeFirst,
                                myPokeFirst
                            ).also {
                                if (it == 0) {
                                    fightVM.setGameOver(GameOver(isGameOver = true, isWin = false))
                                }
                                myPokeHp = it
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }.invokeOnCompletion {
                        fightVM.setIsFightingProgress(false)
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
                Text(text = "Total PokeBalls: $currentPokeBalls", modifier = Modifier
                    .padding(16.dp)
                    .clickable {
                        buyPokeBallDialog(context, fightVM)
                    })
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
                        if (currentPokeBalls > 0) {
                            if (isCapturingProgress) return@TextButton
                            fightVM.setIsCapturingProgress(true)
                            scope.launch {
                                delay(500)
                                PokeHelpers.tryCatch(
                                    enemyPokeFirst,
                                    enemyPokeHp,
                                    myPokeBalls.first()
                                ).also {
                                    if (it) {
                                        fightVM.setGameOver(
                                            GameOver(
                                                isGameOver = true,
                                                isCaptured = true
                                            )
                                        )
                                        enemyPokeFirst.baseCatchRate = null
                                        fightVM.addPokemon(enemyPokeFirst)
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "Failed to catch",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                                scope.launch {
                                    fightVM.updatePokeBalls(currentPokeBalls - 1)
                                }
                            }.invokeOnCompletion {
                                fightVM.setIsCapturingProgress(false)
                            }
                        } else {
                            buyPokeBallDialog(context, fightVM)
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

    @Composable
    fun WinGameScreen(
        fightVM: FightViewModel,
        amount: Int,
        text: String
    ) {
        val pokeCash by fightVM.pokeCash.collectAsStateWithLifecycle()
        Column {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "total available PokeCash: $pokeCash",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = {
                fightVM.resetFight()
            }) {
                Text(text = "Next Pokemon")
            }
            LaunchedEffect(true) {
                fightVM.updatePokeCash(pokeCash + amount)
            }
        }
    }
}

fun buyPokeBallDialog(
    context: Context,
    fightVM: FightViewModel,
) {
    AlertDialog.Builder(context)
        .setTitle("Buy Poke Balls")
        .setMessage("Do you want to buy 5 PokeBalls?")
        .setPositiveButton("Yes") { dialog, _ ->
            fightVM.purchasePokeBalls(5, 100) { isPurchased ->
                if (isPurchased) {
                    Toast.makeText(context, "Purchased 5 PokeBalls", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Not enough PokeCash", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }
        .setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        .setIcon(R.drawable.mega_mewtwo_x)
        .show()
}