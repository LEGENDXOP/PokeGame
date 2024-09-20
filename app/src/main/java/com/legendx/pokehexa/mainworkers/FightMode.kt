package com.legendx.pokehexa.mainworkers

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import coil.request.ImageRequest
import com.legendx.pokehexa.PokeHexa
import com.legendx.pokehexa.R
import com.legendx.pokehexa.database.DataBaseBuilder
import com.legendx.pokehexa.mainworkers.viewmodels.FightViewModel
import com.legendx.pokehexa.mainworkers.viewmodels.FightViewModel.GameOver
import com.legendx.pokehexa.mainworkers.viewmodels.FightViewModelFactory
import com.legendx.pokehexa.tools.CodingHelpers
import com.legendx.pokehexa.tools.PokeHelpers
import com.legendx.pokehexa.ui.theme.PokeHexaGameTheme
import kotlinx.coroutines.CoroutineScope
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

        println(myPokemons.size)
        if (myPokemons.size > 0) {
            myPokemons.forEach {
                println(it.name)
            }
        }
        val imageFile = remember {
            CodingHelpers.getPokeImage(context, fightVM.enemyPoke.id)
        }
        val imageModel = ImageRequest.Builder(context)
            .data(imageFile)
            .crossfade(true)
            .build()
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isFight) {
                AsyncImage(
                    model = imageModel, contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp)
                )
                Text(text = fightVM.enemyPoke.name, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = { fightVM.setIsFight(true) }) {
                    Text(text = "Fight")
                }
            } else if (gameOver.isGameOver) {
                if (gameOver.isWin) {
                    val gameText = "You Defeated the enemy you won 100 PokeCash"
                    WinGameScreen(context, scope, fightVM, 100, gameText)
                } else if (gameOver.isCaptured) {
                    val gameText = "You caught the enemy Pokemon you won 200 PokeCash"
                    WinGameScreen(context, scope, fightVM, 200, gameText)
                } else {
                    val gameText = "You lost the battle you will still get 50 PokeCash"
                    WinGameScreen(context, scope, fightVM, 50, gameText)
                }
            } else {
                val myPokeFirst = myPokemons.first()
                val myPokeMoves =
                    myPokeFirst.moves.filter { it.levelLearnedAt <= myPokeFirst.level }
                val enemyPokeFirst =
                    remember { CodingHelpers.convertToUserPokemon(fightVM.enemyPoke, 20) }
                val enemyPokeMoves =
                    enemyPokeFirst.moves.filter { it.levelLearnedAt <= enemyPokeFirst.level }
                val finalMovesMine = remember { myPokeMoves.shuffled().take(4) }
                val finalMovesEnemy = remember { enemyPokeMoves.shuffled().take(4) }
                var myPokeHp by remember { mutableIntStateOf(myPokeFirst.stats.hp) }
                var enemyPokeHp by remember { mutableIntStateOf(enemyPokeFirst.stats.hp) }
                AsyncImage(
                    model = imageModel,
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
        context: Context,
        scope: CoroutineScope,
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
                Intent(context, FightMode::class.java).also {
                    context.startActivity(it)
                }
            }) {
                Text(text = "Next Pokemon")
            }
            LaunchedEffect(true) {
                scope.launch {
                    delay(1000L)
                    fightVM.updatePokeCash(pokeCash + amount)
                }
            }
        }
    }
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
        .setCancelable(false)
        .show()
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