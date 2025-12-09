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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ua.kulky.stok.data.models.InventoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    items: List<InventoryItem>,
    selectedManufacturer: String?,                  // поточно вибраний виробник (null = всі)
    knownManufacturers: List<String>,               // довідник виробників
    onSelectManufacturer: (String?) -> Unit,        // вибір виробника у фільтрі
    onEditBalloon: (
        balloonId: Long,
        code: String,
        size: String,
        color: String,
        price: Double,
        manufacturer: String
    ) -> Unit,
    onDeleteBalloon: (balloonId: Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Залишки", style = MaterialTheme.typography.titleLarge) }

        // Фільтр за виробником (дропдаун)
        item {
            var expanded by remember { mutableStateOf(false) }
            // Тримай у полі тексту те, що обрано (або "Усі виробники")
            val currentLabel = selectedManufacturer ?: "Усі виробники"

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                OutlinedTextField(
                    value = currentLabel,
                    onValueChange = { /* read-only, керуємо через меню */ },
                    label = { Text("Виробник") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // Пункт "Усі виробники"
                    DropdownMenuItem(
                        text = { Text("Усі виробники") },
                        onClick = {
                            onSelectManufacturer(null)
                            expanded = false
                        }
                    )
                    // Список виробників
                    knownManufacturers.forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m) },
                            onClick = {
                                onSelectManufacturer(m)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Перелік позицій
        items(items, key = { it.balloonId }) { e ->
            Card {
                Box(Modifier.fillMaxWidth().padding(12.dp)) {
                    Column(Modifier.fillMaxWidth().padding(end = 80.dp)) {
                        Text("${e.code} • ${e.size} • ${e.color}")
                        if (e.manufacturer.isNotBlank()) {
                            Text("Виробник: ${e.manufacturer}")
                        }
                        Text("Ціна: ${e.price}")
                        Text("Залишок: ${e.qtyIn - e.qtyOut} (прийшло: ${e.qtyIn}, продано: ${e.qtyOut})")
                    }
                    Row(
                        Modifier.align(Alignment.TopEnd),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        var showEdit by remember { mutableStateOf(false) }
                        var showDelete by remember { mutableStateOf(false) }

                        IconButton(onClick = { showEdit = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Редагувати позицію")
                        }
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Видалити позицію")
                        }

                        if (showEdit) {
                            var code by remember { mutableStateOf(e.code) }
                            var size by remember { mutableStateOf(e.size) }
                            var color by remember { mutableStateOf(e.color) }
                            var price by remember { mutableStateOf(e.price.toString()) }
                            var manufacturer by remember { mutableStateOf(e.manufacturer) }

                            AlertDialog(
                                onDismissRequest = { showEdit = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        onEditBalloon(
                                            e.balloonId,
                                            code.trim(),
                                            size.trim(),
                                            color.trim(),
                                            price.toDoubleOrNull() ?: e.price,
                                            manufacturer.trim()
                                        )
                                        showEdit = false
                                    }) { Text("Зберегти") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showEdit = false }) { Text("Скасувати") }
                                },
                                title = { Text("Редагувати позицію") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = code,
                                            onValueChange = { code = it },
                                            label = { Text("Код") }
                                        )
                                        OutlinedTextField(
                                            value = size,
                                            onValueChange = { size = it },
                                            label = { Text("Розмір") }
                                        )
                                        OutlinedTextField(
                                            value = color,
                                            onValueChange = { color = it },
                                            label = { Text("Колір") }
                                        )
                                        OutlinedTextField(
                                            value = manufacturer,
                                            onValueChange = { manufacturer = it },
                                            label = { Text("Виробник") }
                                        )
                                        OutlinedTextField(
                                            value = price,
                                            onValueChange = { price = it },
                                            label = { Text("Ціна") },
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
                                dismissButton = {
                                    TextButton(onClick = { showDelete = false }) { Text("Скасувати") }
                                },
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
