package com.legendx.pokehexa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.legendx.pokehexa.learning.TestingActivity
import com.legendx.pokehexa.mainworkers.FightMode
import com.legendx.pokehexa.tools.DataStoreManager
import com.legendx.pokehexa.ui.theme.PokeHexaGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeHexaGameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    innerPadding.calculateTopPadding()
                    val context = LocalContext.current
                    LaunchedEffect(true) {
                        val isSetupDone = DataStoreManager.getSetup(context)
                        if (!isSetupDone) {
                            val intent = Intent(context, TestingActivity::class.java)
                            val activity = (context as Activity)
                            activity.startActivity(intent)
                            activity.finish()
                        } else {
                            val intent = Intent(context, FightMode::class.java)
                            val activity = (context as Activity)
                            activity.startActivity(intent)
                            activity.finish()
                        }
                    }
                }
            }
        }
    }
}

