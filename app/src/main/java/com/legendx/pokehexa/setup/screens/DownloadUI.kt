package com.legendx.pokehexa.setup.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.legendx.pokehexa.PokeHexa
import com.legendx.pokehexa.setup.tools.DownloadHelper
import com.legendx.pokehexa.setup.tools.FileSys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DownloadUi(modifier: Modifier) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    var showDialogFile by remember { mutableStateOf(false) }
    var showDialogText by remember { mutableStateOf(false) }
    var showDialogDownload by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableIntStateOf(0) }
    var userName by remember { mutableStateOf("") }
    var userUName by remember { mutableStateOf("") }
    var userAge by remember { mutableStateOf("") }
    var downloadId by remember { mutableStateOf<Long?>(null) }
    val focus = LocalFocusManager.current
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height((screenHeight * 0.1).dp))
        Text(text = "Hello Dear User!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(50.dp))
        OutlinedTextField(
            value = userName, onValueChange = { userName = it },
            label = { Text(text = "Enter your name") },
            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = userUName, onValueChange = { userUName = it },
            label = { Text(text = "Enter your username") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AlternateEmail,
                    contentDescription = null
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = userAge, onValueChange = { userAge = it },
            label = { Text(text = "Enter your age") },
            leadingIcon = { Icon(imageVector = Icons.Default.Cake, contentDescription = null) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() })
        )
        Spacer(modifier = Modifier.height(20.dp))
        if (selectedFile == 0) {
            OutlinedButton(onClick = { showDialogText = true }) {
                Text(
                    text = "Details About Resource",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(onClick = { showDialogFile = true }) {
                Text(text = "Choose Resource", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Selected File: ", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Resource $selectedFile",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(onClick = { selectedFile = 0 }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        if (userName.isNotEmpty() && userUName.isNotEmpty() && userAge.isNotEmpty() && selectedFile != 0) {
            OutlinedButton(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    println("Downloading file...")
                    val isZipDownload = FileSys.isFileDownloaded("Resource$selectedFile.zip")
                    val folderExist = FileSys.isFileDownloaded("images")
                    if (!folderExist) {
                        if (isZipDownload) {
                            val unzip =
                                FileSys.unzipDirectlyInDocuments("Resource$selectedFile.zip")
                            val delZip =
                                FileSys.deleteFileFromDocuments("Resource$selectedFile.zip")
                            if (unzip && delZip) {
                                println("Downloaded successfully")
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "Downloaded successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            downloadId = DownloadHelper.downloadFile(
                                context,
                                "https://legendx.in/images.zip",
                                "Resource$selectedFile.zip"
                            )
                            showDialogDownload = true
                        }
                    } else {
                        println("Folder already exists")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Resource Already Downloaded",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }) {
                Text(text = "Download", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        if (showDialogText) {
            DialogForDetails { showDialogText = false }
        }
        if (showDialogFile) {
            ChooseFile { confirm ->
                showDialogFile = false
                if (confirm.isConfirmed) {
                    selectedFile = confirm.selectedFile
                }
            }
        }
        if (showDialogDownload) {
            DialogForDownload(context, downloadId)
        }
    }
}


@Composable
fun ChooseFile(dismiss: (confirm: FileResult) -> Unit) {
    var selectedRadio by remember { mutableIntStateOf(0) }
    AlertDialog(
        onDismissRequest = { dismiss(FileResult(false)) }, confirmButton = {
            TextButton(onClick = { dismiss(FileResult(true, selectedRadio)) }) {
                Text(text = "Confirm", fontSize = 14.sp)
            }
        },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                        .fillMaxWidth()
                ) {
                    RadioButton(selected = selectedRadio == 1, onClick = { selectedRadio = 1 })
                    Text(
                        text = "File 1: Size is 120MB",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                        .fillMaxWidth()
                ) {
                    RadioButton(selected = selectedRadio == 2, onClick = { selectedRadio = 2 })
                    Text(
                        text = "File 2: Size is 650MB",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    )
}


@Composable
fun DialogForDownload(context: Context, downloadId: Long?) {
    var downloadProgress by remember { mutableIntStateOf(0) }
    val intent = Intent(context, PokeHexa::class.java)
    val activity = context as Activity
    LaunchedEffect(downloadId) {
        downloadId?.let { id ->
            while (downloadProgress < 100) {
                downloadProgress = DownloadHelper.trackDownloadProgress(context, id)
                delay(1000L)
            }
        }
        if (downloadProgress >= 100) {
            withContext(Dispatchers.IO) {
                processAfterDownload(context)
            }
        }
    }
    AlertDialog(
        title = {
            Text(
                text = "Downloading Resource",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        icon = { Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null) },
        shape = MaterialTheme.shapes.medium,
        onDismissRequest = {},
        confirmButton = {
            if (downloadProgress >= 100) {
                TextButton(onClick = {
                    activity.startActivity(intent).also { activity.finish() }
                }) {
                    Text(text = "Next", fontSize = 14.sp)
                }
            }
        },
        text = {
            Column {
                if (downloadProgress < 100) {
                    LinearProgressIndicator(
                        progress = { downloadProgress / 100f },
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "$downloadProgress%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "Downloaded Successfully",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth(0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )
}

suspend fun processAfterDownload(context: Context) {
    val fileName1 = "Resource1.zip"
    val fileName2 = "Resource2.zip"

    if (FileSys.isFileDownloaded(fileName1)) {
        val unzip = FileSys.unzipDirectlyInDocuments(fileName1)
        val delZip = FileSys.deleteFileFromDocuments(fileName1)
        if (unzip && delZip) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Resources optimized successfully", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    } else if (FileSys.isFileDownloaded(fileName2)) {
        val unzip = FileSys.unzipDirectlyInDocuments(fileName2)
        val delZip = FileSys.deleteFileFromDocuments(fileName2)
        if (unzip && delZip) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Resources optimized successfully", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    } else {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
        }
    }

}

@Composable
fun DialogForDetails(dismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { dismiss() },
        confirmButton = {
            TextButton(onClick = { dismiss() }) {
                Text(text = "OK", fontSize = 14.sp)
            }
        },
        text = {
            Column {
                Text(text = longText(), fontSize = 16.sp)
            }
        }
    )
}


@Composable
fun longText(): AnnotatedString {
    val longText = buildAnnotatedString {
        append("We offer two resource packs for download to suit different devices and gaming preferences:\n\n")

        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("Low Quality Pack (120MB): ")
        }
        append("Ideal for low-end devices and casual players who want a lightweight and smooth gaming experience.\n\n")

        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("High Quality Pack (650MB): ")
        }
        append("Recommended for high-end devices and dedicated gamers seeking enhanced graphics and a more immersive experience.\n\n")

        append("Please choose the resource pack that best matches your device capabilities and your preferred style of play.")
    }

    return longText
}

data class FileResult(
    val isConfirmed: Boolean,
    val selectedFile: Int = 0
)