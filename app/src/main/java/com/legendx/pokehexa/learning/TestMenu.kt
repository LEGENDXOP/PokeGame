package com.legendx.pokehexa.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import okhttp3.internal.userAgent

data class MenuItems(
    val title: String,
    val icon: ImageVector,
    val description: String
)

@Composable
fun TestMenu(
    menuItems: List<MenuItems>,
    itemClick: (MenuItems) -> Unit
) {
    var dropDownMenuExpanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { dropDownMenuExpanded = !dropDownMenuExpanded }
        ) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
        }

        DropdownMenu(
            expanded = dropDownMenuExpanded,
            onDismissRequest = { dropDownMenuExpanded = false }
        ) {
            menuItems.forEach { item ->
                Row(
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable {
                            itemClick(item)
                            dropDownMenuExpanded = false
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = item.icon, contentDescription = item.title)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item.title)
                }
            }
        }

    }
}

@Composable
fun RunTestMenu() {
    val menuItems = listOf(
        MenuItems("Item 1", Icons.Default.AddCard, "Adding a new card"),
        MenuItems("Item 2", Icons.Default.AddTask, "Adding a new task"),
    )
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        TestMenu(menuItems = menuItems, itemClick = {
            println("Item Clicked: ${it.description}")
        })
    }
}

@Composable
fun TestTwoScreen() {
    var showDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 120.dp)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Button(
            onClick = { showDialog = true },
        ) {
            Text(text = "Show Dialog")
        }
    }
    if (showDialog) {
        CustomDialog(
            onDismiss = { showDialog = false },
            onPositiveButtonClick = { showDialog = false }
        )
    }
}

@Composable
fun CustomDialog(
    onDismiss: (() -> Unit)? = null,
    onPositiveButtonClick: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = { onDismiss?.invoke() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column (
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ){
                    Text(text = "This is a dialog!", style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 16.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "This is a dialog message", style = MaterialTheme.typography.bodyLarge)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(vertical = 20.dp)
                            .padding(horizontal = 24.dp)
                        ,
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onDismiss?.invoke() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Close")
                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        Button(
                            onClick = { onPositiveButtonClick?.invoke() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Confirm")
                        }
                    }
                }
            }
        }
    }
}