package com.legendx.pokehexa


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.legendx.pokehexa.mainworkers.FightMode
import com.legendx.pokehexa.setup.FilesSetup
import com.legendx.pokehexa.ui.theme.PokeHexaGameTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt


class PokeHexa : ComponentActivity() {
    fun String.toast(context: Context) {
        Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeHexaGameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        PokemonBattleApp()
                    }
                }
            }
        }
    }
}


@Composable
fun PokemonBattleApp() {
    val context = LocalContext.current
    Column {
        Text(text = "Hello, Pokemon!")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Hello, Download!",
            modifier = Modifier.clickable {
                Intent(context, FilesSetup::class.java).also {
                    context.startActivity(it)
                }
            })
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Hello, Fight!", modifier = Modifier
            .clickable {
                Intent(context, FightMode::class.java).also {
                    context.startActivity(it)
                    ActivityCompat.finishAffinity(context as Activity)
                }
            }
        )
    }
}

@Composable
fun CustomButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onDoubleClick: () -> Unit = {},
    content: @Composable RowScope.() -> Unit
) {
    val viewConfiguration = LocalViewConfiguration.current
    val interactionSource = remember { MutableInteractionSource() }
    val buttonClicked = remember { mutableStateListOf<Int>() }
    var doubleClicked by remember { mutableStateOf(false) }
    var longClicked by remember { mutableStateOf(false) }
    LaunchedEffect(buttonClicked.size) {
        if (buttonClicked.size < 2) {
            delay(viewConfiguration.doubleTapTimeoutMillis)
            buttonClicked.clear()
            return@LaunchedEffect
        } else {
            doubleClicked = true
        }
        buttonClicked.clear()
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    buttonClicked.add(1)
                    longClicked = false
                    delay(viewConfiguration.longPressTimeoutMillis)
                    longClicked = true
                    onLongClick()
                }

                is PressInteraction.Release -> {
                    delay(viewConfiguration.doubleTapTimeoutMillis)
                    if (longClicked.not() and doubleClicked.not()) {
                        onClick()
                    }
                    else if (doubleClicked) {
                        onDoubleClick()
                        doubleClicked = false
                    }
                }
            }
        }
    }
    OutlinedButton(
        interactionSource = interactionSource,
        onClick = {}
    ) {
        content()
    }
}