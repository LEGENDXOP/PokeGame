package com.legendx.pokehexa.setup.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
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
import androidx.compose.ui.layout.ContentScale
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.legendx.pokehexa.database.DataBaseBuilder
import com.legendx.pokehexa.database.UserDataDao
import com.legendx.pokehexa.database.UserStart
import com.legendx.pokehexa.database.UserTable
import com.legendx.pokehexa.mainworkers.FightMode
import com.legendx.pokehexa.mainworkers.PokeBallsCategory
import com.legendx.pokehexa.mainworkers.UserPokeBalls
import com.legendx.pokehexa.mainworkers.UserPokemon
import com.legendx.pokehexa.setup.tools.DownloadHelper
import com.legendx.pokehexa.setup.tools.FileSys
import com.legendx.pokehexa.setup.viewmodels.SetupPokeViewModel
import com.legendx.pokehexa.setup.viewmodels.SetupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DownloadUi(modifier: Modifier) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val focus = LocalFocusManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val setupVM = viewModel<SetupViewModel>()
    val pokeModel = viewModel<SetupPokeViewModel>()
    val userDao = DataBaseBuilder.getDataBase(context).userDao()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height((screenHeight * 0.1).dp))
        Text(text = "Hello Dear User!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(50.dp))
        OutlinedTextField(
            value = setupVM.userName, onValueChange = { setupVM.userName = it },
            label = { Text(text = "Enter your name") },
            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = setupVM.userUName, onValueChange = { setupVM.userUName = it },
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
            value = setupVM.userPassword, onValueChange = { setupVM.userPassword = it },
            label = { Text(text = "Enter your password") },
            leadingIcon = { Icon(imageVector = Icons.Default.Password, contentDescription = null) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() })
        )
        Spacer(modifier = Modifier.height(20.dp))
        if (setupVM.selectedFile == 0) {
            OutlinedButton(onClick = { setupVM.showDialogText = true }) {
                Text(
                    text = "Details About Resource",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(onClick = { setupVM.showDialogFile = true }) {
                Text(text = "Choose Resource", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Selected File: ", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Resource ${setupVM.selectedFile}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(onClick = { setupVM.selectedFile = 0 }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        if (setupVM.checkDownloadAvailable()) {
            OutlinedButton(onClick = {
                scope.launch {
                    setupVM.startDownload(context)
                }
            }) {
                Text(text = "Download", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        if (setupVM.showDialogText) {
            DialogForDetails { setupVM.showDialogText = false }
        }
        if (setupVM.showDialogFile) {
            ChooseFile { confirm ->
                setupVM.showDialogFile = false
                if (confirm.isConfirmed) {
                    setupVM.selectedFile = confirm.selectedFile
                }
            }
        }
        if (setupVM.showDialogDownload) {
            DialogForDownload(context, setupVM.downloadId, setupVM) {
                setupVM.showDialogDownload = false
                setupVM.showDialogSelectPokemon = true
            }
        }
        if (setupVM.showDialogSelectPokemon) {
            ShowDialogSelectPoke(pokeModel, userDao) {
                setupVM.showDialogSelectPokemon = false
                Intent(context, FightMode::class.java).also {
                    context.startActivity(it)
                }
            }
        }
    }
}

@Composable
fun ShowDialogSelectPoke(
    pokeModel: SetupPokeViewModel,
    userDao: UserDataDao,
    onFinish: () -> Unit
) {
    var confirm by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var selectedPokemon by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    AlertDialog(
        shape = MaterialTheme.shapes.extraLarge,
        onDismissRequest = {},
        confirmButton = {
            if (confirm && !saving) {
                TextButton(onClick = {
                    saving = true
                    scope.launch {
                        delay(3000L)
                        pokeModel.pokeData =
                            pokeModel.startersPokemons.find { it.name == selectedPokemon }
                        val pokeBall = UserPokeBalls(
                            name = PokeBallsCategory.PokeBall,
                            id = 1,
                            quantity = 50,
                        )

                        val pokeMine = UserPokemon(
                            name = pokeModel.pokeData!!.name,
                            id = pokeModel.pokeData!!.id,
                            level = 15,
                            experience = 21,
                            moves = pokeModel.pokeData!!.moves,
                            stats = pokeModel.pokeData!!.stats,
                            abilities = pokeModel.pokeData!!.abilities,
                            types = pokeModel.pokeData!!.types,
                            height = pokeModel.pokeData!!.height,
                            weight = pokeModel.pokeData!!.weight
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
                        userDao.addUserData(pokeTable)
                    }.invokeOnCompletion {
                        onFinish()
                    }
                }) {
                    Text(text = "Next", fontSize = 14.sp)
                }
            }
        },
        title = { Text(text = "Select your Pokemon") },
        text = {
            if (!saving) {
                SelectPokemon(pokeModel = pokeModel) {
                    confirm = true
                    selectedPokemon = it
                }
            } else {
                LinearProgressIndicator()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectPokemon(pokeModel: SetupPokeViewModel, onSelected: (String) -> Unit) {
    val context = LocalContext.current
    val pokeStarters = pokeModel.getStarterPokemons(context)
    val state = rememberCarouselState { pokeStarters.count() }
    var selectedPokemon by remember { mutableStateOf("") }
    Column {
        Text(
            text = "Click to choose",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            textAlign = TextAlign.Center
        )
        HorizontalUncontainedCarousel(
            state = state,
            itemWidth = 150.dp,
            itemSpacing = 10.dp,
        ) { i ->
            val poke = pokeStarters[i]
            val imageModel = ImageRequest.Builder(context)
                .data(poke.imageFile)
                .crossfade(true)
                .build()
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .maskClip(shape = MaterialTheme.shapes.small)
                    .clickable {
                        selectedPokemon = poke.name
                        onSelected(poke.name)
                    }) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.height(150.dp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(text = poke.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        if (selectedPokemon.isNotEmpty()) {
            Text(
                text = "You have chosen $selectedPokemon",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp),
                textAlign = TextAlign.Center
            )
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
                        .clickable { selectedRadio = 1 }
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
                        .clickable { selectedRadio = 2 }
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
fun DialogForDownload(
    context: Context,
    downloadId: Long?,
    setupVM: SetupViewModel,
    onClick: () -> Unit
) {
    var downloadProgress by remember { mutableIntStateOf(0) }
    var downloadDone by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val userStartDao = DataBaseBuilder.getDataBase(context).userStartDao()
    LaunchedEffect(downloadId) {
        downloadId?.let { id ->
            while (downloadProgress < 100) {
                downloadProgress = DownloadHelper.trackDownloadProgress(context, id)
                delay(1000L)
            }
        }
        if (downloadProgress >= 100) {
            scope.launch(Dispatchers.IO) {
                processAfterDownload(context)
            }.invokeOnCompletion {
                scope.launch(Dispatchers.Main) {
                    downloadDone = true
                }
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
            if (downloadDone) {
                TextButton(onClick = {
                    val saveUserStart = UserStart(
                        1,
                        setupVM.userName,
                        setupVM.userUName,
                        setupVM.userPassword,
                        setupVM.selectedFile
                    )
                    scope.launch { userStartDao.saveStart(saveUserStart) }
                    onClick()
                }) {
                    Text(text = "Next", fontSize = 14.sp)
                }
            }
        },
        text = {
            Column {
                if (!downloadDone) {
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

    if (FileSys.isFileDownloaded(fileName1, context)) {
        val unzip = FileSys.unzipDirectlyInDocuments(fileName1, context)
        val delZip = FileSys.deleteFileFromDocuments(fileName1, context)
        if (unzip && delZip) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Resources optimized successfully", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    } else if (FileSys.isFileDownloaded(fileName2, context)) {
        val unzip = FileSys.unzipDirectlyInDocuments(fileName2, context)
        val delZip = FileSys.deleteFileFromDocuments(fileName2, context)
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