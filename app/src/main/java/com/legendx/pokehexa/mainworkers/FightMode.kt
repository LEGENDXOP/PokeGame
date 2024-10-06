package com.legendx.pokehexa.mainworkers

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
                    FightModeScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun FightModeScreen(modifier: Modifier) {
        val context = LocalContext.current
        val haptics = LocalHapticFeedback.current
        val view = LocalView.current
        val screenWidth = LocalConfiguration.current.screenWidthDp
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
        var chooseMyPokemon by remember { mutableStateOf(false) }
        var showPokeDetails by remember { mutableStateOf<UserPokemon?>(null) }
        var showMoveDetails by remember { mutableStateOf<Move?>(null) }
        val myPokemon by fightVM.myPokemon.collectAsStateWithLifecycle()
        val enemyPokeImage = remember(enemyPoke) {
            CodingHelpers.getPokeImage(context, enemyPoke.id)
        }
        val myModifier = modifier.then(
            if (screenWidth > 600) {
                Modifier.verticalScroll(rememberScrollState())
            } else {
                Modifier.verticalScroll(rememberScrollState())
            }
        )
        println("on the start of compose total pokes are: ${myPokemons.size}")
        Column(
            modifier = myModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isFight) {
                AsyncImage(
                    model = enemyPokeImage, contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                )
                Text(text = enemyPoke.name, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = {
                    fightVM.setIsFight(true)
                }) {
                    Text(text = "Fight")
                }
            } else if (chooseMyPokemon) {
                ChoosePokemonForFightDialog(myPokemons, onDismiss = { chooseMyPokemon = false }) {
                    fightVM.setMyPokemon(it)
                    chooseMyPokemon = false
                }
            } else if (showPokeDetails != null) {
                ShowUserPokemonDetailsDialog(showPokeDetails!!) {
                    showPokeDetails = null
                }
            } else if (showMoveDetails != null) {
                ShowUserMoveDetailsDialog(showMoveDetails!!) {
                    showMoveDetails = null
                }
            } else if (gameOver.isGameOver) {
                val (gameText, amount) = when {
                    gameOver.isWin -> "You Defeated the enemy\nyou won 50 PokeCash" to 50
                    gameOver.isCaptured -> "You caught the enemy Pokémon\nyou won 100 PokeCash" to 100
                    else -> "You lost the battle\nyou will still get 20 PokeCash" to 20
                }
                WinGameScreen(fightVM, amount, gameText)
            } else {
                val myPokeFirst = myPokemon ?: myPokemons.first()

                val myPokeMoves = remember(myPokeFirst) {
                    myPokeFirst.moves.filter { it.levelLearnedAt <= myPokeFirst.level }
                }
                val myPokeImage = remember(myPokeFirst) {
                    CodingHelpers.getPokeImage(context, myPokeFirst.id)
                }

                val enemyPokeFirst = remember(enemyPoke) {
                    CodingHelpers.convertToUserPokemon(enemyPoke, 35)
                }

                val enemyPokeMoves = remember(enemyPokeFirst) {
                    enemyPokeFirst.moves.filter { it.levelLearnedAt <= enemyPokeFirst.level }
                }

                val finalMovesMine = remember(myPokeMoves) { myPokeMoves.shuffled().take(4) }
                val finalMovesEnemy = remember(enemyPokeMoves) { enemyPokeMoves.shuffled().take(4) }

                val myPokeHp by fightVM.myPokemonHp.collectAsStateWithLifecycle()
                val enemyPokeHp by fightVM.enemyPokemonHp.collectAsStateWithLifecycle()

                LaunchedEffect(true) {
                    if (myPokeHp == 0 || enemyPokeHp == 0){
                        fightVM.setMyPokemonHp(myPokeFirst.stats.hp)
                        fightVM.setEnemyPokemonHp(enemyPokeFirst.stats.hp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AsyncImage(
                        model = myPokeImage,
                        contentDescription = null,
                        modifier = Modifier
                            .size(150.dp)
                            .scale(scaleX = -1f, scaleY = 1f)
                            .combinedClickable(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                    chooseMyPokemon = true
                                },
                                onLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showPokeDetails = myPokeFirst
                                }
                            )
                    )
                    Icon(
                        painter = painterResource(R.drawable.versus_icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(30.dp)
                            .alpha(0.8f)
                    )
                    AsyncImage(
                        model = enemyPokeImage,
                        contentDescription = null,
                        modifier = Modifier
                            .size(150.dp)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showPokeDetails = enemyPokeFirst
                                }
                            )
                    )
                }
                val fightDetailText =
                    "${myPokeFirst.name} (${myPokeFirst.level}) vs ${enemyPokeFirst.name} (${enemyPokeFirst.level})"
                Text(
                    text = fightDetailText,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(14.dp)
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    thickness = 1.dp
                )
                ShowPokeHp(
                    pokeName = myPokeFirst.name,
                    currentHp = myPokeHp,
                    totalHp = myPokeFirst.stats.hp,
                    modifier = Modifier.padding(14.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ShowPokeHp(
                    pokeName = enemyPokeFirst.name,
                    currentHp = enemyPokeHp,
                    totalHp = enemyPokeFirst.stats.hp,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                BattleButtons(
                    listOfMoves = finalMovesMine,
                    onLongClick = { move ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showMoveDetails = move
                    },
                ) { move ->
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
                        fightVM.setEnemyPokemonHp(it)
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
                                fightVM.setMyPokemonHp(it)
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
                        fightVM.resetFight()
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

    @Composable
    fun ShowUserMoveDetailsDialog(thisMove: Move, onDismiss: () -> Unit) {
        val moveFullDetail = DataCache.movesList.find { it.name == thisMove.move }
        Dialog(
            onDismissRequest = { onDismiss() },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .padding(horizontal = 16.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.White),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("NAME: ${thisMove.move}", style = MaterialTheme.typography.titleLarge)
                    Text("POWER: ${thisMove.power}", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "TYPE: ${moveFullDetail?.type}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "EFFECT: ${moveFullDetail?.effect}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "TARGET: ${moveFullDetail?.target}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    @Composable
    fun ShowUserPokemonDetailsDialog(thisPoke: UserPokemon, onDismiss: () -> Unit) {
        val context = LocalContext.current
        Dialog(
            onDismissRequest = { onDismiss() },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.White)
            ) {
                val pokeImage = CodingHelpers.getPokeImage(context, thisPoke.id)
                val imageRequester = ImageRequest.Builder(context).data(pokeImage)
                    .crossfade(true).build()
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = imageRequester,
                        contentDescription = null,
                        modifier = Modifier.size(200.dp)
                    )
                    Text("NAME: ${thisPoke.name}", style = MaterialTheme.typography.titleLarge)
                    Text("LEVEL: ${thisPoke.level}", style = MaterialTheme.typography.titleMedium)
                    Text("HP: ${thisPoke.stats.hp}", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "ATTACK: ${thisPoke.stats.attack}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "DEFENSE: ${thisPoke.stats.defense}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "TYPES: ${thisPoke.types.joinToString(", ")}",
                        style = MaterialTheme.typography.titleMedium
                    )

                }
            }
        }
    }

    @Composable
    fun ChoosePokemonForFightDialog(
        allPokemons: List<UserPokemon>,
        onDismiss: () -> Unit,
        onClick: (UserPokemon) -> Unit
    ) {
        Dialog(
            onDismissRequest = { onDismiss() },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.White)
            ) {
                val verticalState = rememberLazyGridState()
                val context = LocalContext.current
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Fixed(2),
                    state = verticalState,
                    contentPadding = PaddingValues(8.dp),
                ) {
                    items(allPokemons.size) { index ->
                        val pokeImage = CodingHelpers.getPokeImage(context, allPokemons[index].id)
                        val imageRequester = ImageRequest.Builder(context).data(pokeImage)
                            .crossfade(true).build()
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { onClick(allPokemons[index]) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = imageRequester,
                                contentDescription = null,
                            )
                            Text(
                                text = allPokemons[index].name,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ShowPokeHp(
        pokeName: String,
        currentHp: Int,
        totalHp: Int,
        modifier: Modifier
    ) {
        Column {
            Text(
                text = "Pokemon: $pokeName HP: $currentHp/$totalHp",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = modifier.fillMaxWidth()
            )
            LinearProgressIndicator(
                progress = { currentHp.toFloat() / totalHp },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .size(6.dp)
            )
        }
    }

    @Composable
    fun CustomButton(
        onClick: () -> Unit,
        onLongClick: () -> Unit = {},
        content: @Composable RowScope.() -> Unit
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val viewConfiguration = LocalViewConfiguration.current
        val isPressed by interactionSource.collectIsPressedAsState()
        LaunchedEffect(isPressed) {
            if (isPressed){
                delay(viewConfiguration.longPressTimeoutMillis)
                onLongClick()
            }
        }
        OutlinedButton(
            interactionSource = interactionSource,
            onClick = onClick
        ) {
            content()
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun BattleButtons(
        listOfMoves: List<Move>,
        onLongClick: (Move) -> Unit,
        onClick: (Move) -> Unit
    ) {
        FlowRow(
            maxItemsInEachRow = 2,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOfMoves.forEach { move ->
                CustomButton(
                    onClick = { onClick(move) },
                    onLongClick = { onLongClick(move) }
                ) {
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