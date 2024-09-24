package com.legendx.pokehexa.setup

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.legendx.pokehexa.setup.screens.DownloadUi
import com.legendx.pokehexa.ui.theme.PokeHexaGameTheme
import java.util.Locale

class FilesSetup : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeHexaGameTheme {
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { granted ->
                    granted.forEach {
                        println("Permission: $it")
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DownloadUi(modifier = Modifier.padding(innerPadding))
                }
                RequestPermissionsFile(context, permissionLauncher)
            }
        }
    }
}


@Composable
fun RequestPermissionsFile(
    context: Context,
    permissionLauncher: ActivityResultLauncher<Array<String>>
) {
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            val writePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                )
            }
        } else {
            val manageAllFilesPermission = Environment.isExternalStorageManager()
            if (!manageAllFilesPermission) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri =
                    Uri.parse(String.format(Locale.ENGLISH, "package:%s", context.packageName))
                intent.data = uri
                Toast.makeText(
                    context,
                    "Please enable the manage all files permission",
                    Toast.LENGTH_LONG
                ).show()
                context.startActivity(intent)
            }
        }
    }
}


