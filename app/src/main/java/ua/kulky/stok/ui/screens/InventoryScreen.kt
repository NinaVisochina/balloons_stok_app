package ua.kulky.stok.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ua.kulky.stok.data.models.InventoryItem
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun InventoryScreen(
    items: List<InventoryItem>,
    onEditBalloon: (balloonId: Long, code: String, size: String, color: String, price: Double) -> Unit,
    onDeleteBalloon: (balloonId: Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Залишки", style = MaterialTheme.typography.titleLarge) }
        items(items, key = { it.balloonId }) { e ->
            Card {
                Box(Modifier.fillMaxWidth().padding(12.dp)) {
                    Column(Modifier.fillMaxWidth().padding(end = 80.dp)) {
                        Text("${e.code} • ${e.size} • ${e.color}")
                        Text("Ціна: ${e.price}")
                        Text("Залишок: ${e.qtyIn - e.qtyOut} (прийшло: ${e.qtyIn}, продано: ${e.qtyOut})")
                    }
                    Row(
                        Modifier.align(Alignment.TopEnd),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        var showEdit by remember { mutableStateOf(false) }
                        var showDelete by remember { mutableStateOf(false) }
                        IconButton(onClick = { showEdit = true }) { Icon(Icons.Filled.Edit, contentDescription = "Редагувати позицію") }
                        IconButton(onClick = { showDelete = true }) { Icon(Icons.Filled.Delete, contentDescription = "Видалити позицію") }

                        if (showEdit) {
                            var code by remember { mutableStateOf(e.code) }
                            var size by remember { mutableStateOf(e.size) }
                            var color by remember { mutableStateOf(e.color) }
                            var price by remember { mutableStateOf(e.price.toString()) }
                            AlertDialog(
                                onDismissRequest = { showEdit = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        onEditBalloon(e.balloonId, code.trim(), size.trim(), color.trim(), price.toDoubleOrNull() ?: e.price)
                                        showEdit = false
                                    }) { Text("Зберегти") }
                                },
                                dismissButton = { TextButton(onClick = { showEdit = false }) { Text("Скасувати") } },
                                title = { Text("Редагувати позицію") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(code, { code = it }, label = { Text("Код") })
                                        OutlinedTextField(size, { size = it }, label = { Text("Розмір") })
                                        OutlinedTextField(color, { color = it }, label = { Text("Колір") })
                                        OutlinedTextField(
                                            price, { price = it }, label = { Text("Ціна") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                    }
                                }
                            )
                        }

                        if (showDelete) {
                            AlertDialog(
                                onDismissRequest = { showDelete = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        onDeleteBalloon(e.balloonId)
                                        showDelete = false
                                    }) { Text("Видалити") }
                                },
                                dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Скасувати") } },
                                title = { Text("Видалити позицію?") },
                                text = { Text("Буде видалено також усі приходи й продажі цієї позиції.") }
                            )
                        }
                    }
                }
            }
        }
    }
}
