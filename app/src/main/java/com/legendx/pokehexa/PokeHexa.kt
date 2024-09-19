package com.legendx.pokehexa


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.legendx.pokehexa.mainworkers.FightMode
import com.legendx.pokehexa.setup.FilesSetup
import com.legendx.pokehexa.setup.screens.SelectData
import com.legendx.pokehexa.ui.theme.PokeHexaGameTheme

class PokeHexa : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeHexaGameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                    ) {
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
        Text(text = "Hello, Pokemon!",
            modifier = Modifier.clickable {
                Intent(context, SelectData::class.java).run {
                    context.startActivity(this)
                }
            })
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
                }
            }
        )
    }
}