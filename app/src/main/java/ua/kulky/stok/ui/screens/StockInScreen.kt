package ua.kulky.stok.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import ua.kulky.stok.data.models.OperationFilter
import ua.kulky.stok.data.models.StockInItem

@Composable
fun StockInScreen(
        items: List<StockInItem>,
        onAddSmart:
                (
                        code: String,
                        size: String,
                        color: String,
                        price: Double,
                        qty: Int,
                        date: LocalDate) -> Unit,
        onFilter: (OperationFilter) -> Unit,
        onEdit: (id: Long, qty: Int, date: LocalDate) -> Unit,
        onDelete: (id: Long) -> Unit
) {
    // Поля форми (збережемо введені дані між перезбірками)
    var code by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf(LocalDate.now().toString()) }

    // Фільтр-діалог
    var showFilter by remember { mutableStateOf(false) }
    var fDateFrom by remember { mutableStateOf("") }
    var fDateTo by remember { mutableStateOf("") }
    var fCode by remember { mutableStateOf("") }
    var fSize by remember { mutableStateOf("") }
    var fColor by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    // Якщо список поповнився — автоматично підскролимо до верху історії
    LaunchedEffect(items.size) { if (items.isNotEmpty()) listState.animateScrollToItem(0) }

    LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Заголовок
        item { Text("Прихід", style = MaterialTheme.typography.titleLarge) }

        // Форма — завжди всі поля
        item {
            OutlinedTextField(
                    code,
                    { code = it },
                    label = { Text("Код") },
                    modifier = Modifier.fillMaxSize()
            )
        }
        item {
            OutlinedTextField(
                    size,
                    { size = it },
                    label = { Text("Розмір (напр. 10\")") },
                    modifier = Modifier.fillMaxSize()
            )
        }
        item {
            OutlinedTextField(
                    color,
                    { color = it },
                    label = { Text("Колір") },
                    modifier = Modifier.fillMaxSize()
            )
        }
        item {
            OutlinedTextField(
                    price,
                    { price = it },
                    label = { Text("Ціна") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxSize()
            )
        }
        item {
            OutlinedTextField(
                    qty,
                    { qty = it },
                    label = { Text("Кількість") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxSize()
            )
        }
        item {
            OutlinedTextField(
                    dateStr,
                    { dateStr = it },
                    label = { Text("Дата (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxSize()
            )
        }
        item {
            Button(
                    enabled =
                            code.isNotBlank() &&
                                    size.isNotBlank() &&
                                    color.isNotBlank() &&
                                    (qty.toIntOrNull() ?: 0) > 0,
                    onClick = {
                        val p = price.toDoubleOrNull() ?: 0.0
                        val q = qty.toIntOrNull() ?: 0
                        val d =
                                runCatching { LocalDate.parse(dateStr) }
                                        .getOrDefault(LocalDate.now())
                        onAddSmart(code.trim(), size.trim(), color.trim(), p, q, d)
                        // очищаємо лише кількість (зручно вносити серіями)
                        qty = ""
                    }
            ) { Text("Зберегти прихід") }
        }

        // Кнопка фільтра
        item { Button(onClick = { showFilter = true }) { Text("Фільтр") } }

        // Роздільник і заголовок історії
        item { HorizontalDivider() }
        item { Text("Історія приходу", style = MaterialTheme.typography.titleMedium) }

        // Історія
        items(items, key = { it.id }) { e ->
            Card {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("${e.code} • ${e.size} • ${e.color}")
                    Text("Ціна: ${e.price}  Кількість: ${e.qty}")
                    Text("Дата: ${e.date}")

                    var showEdit by remember { mutableStateOf(false) }
                    var showDelete by remember { mutableStateOf(false) }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showEdit = true }) { Text("Редагувати") }
                        TextButton(onClick = { showDelete = true }) { Text("Видалити") }
                    }

                    if (showEdit) {
                        var eqty by remember { mutableStateOf(e.qty.toString()) }
                        var edate by remember { mutableStateOf(e.date.toString()) }
                        AlertDialog(
                                onDismissRequest = { showEdit = false },
                                confirmButton = {
                                    TextButton(
                                            onClick = {
                                                val q = eqty.toIntOrNull() ?: e.qty
                                                val d =
                                                        runCatching { LocalDate.parse(edate) }
                                                                .getOrDefault(e.date)
                                                onEdit(e.id, q, d)
                                                showEdit = false
                                            }
                                    ) { Text("Зберегти") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showEdit = false }) { Text("Скасувати") }
                                },
                                title = { Text("Редагувати прихід") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                                eqty,
                                                { eqty = it },
                                                label = { Text("Кількість") },
                                                keyboardOptions =
                                                        KeyboardOptions(
                                                                keyboardType = KeyboardType.Number
                                                        )
                                        )
                                        OutlinedTextField(
                                                edate,
                                                { edate = it },
                                                label = { Text("Дата (yyyy-MM-dd)") }
                                        )
                                    }
                                }
                        )
                    }

                    if (showDelete) {
                        AlertDialog(
                                onDismissRequest = { showDelete = false },
                                confirmButton = {
                                    TextButton(
                                            onClick = {
                                                onDelete(e.id)
                                                showDelete = false
                                            }
                                    ) { Text("Видалити") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDelete = false }) {
                                        Text("Скасувати")
                                    }
                                },
                                title = { Text("Видалити запис приходу?") },
                                text = { Text("Це дію не можна скасувати.") }
                        )
                    }
                }
            }
        }
    }

    if (showFilter) {
        AlertDialog(
                onDismissRequest = { showFilter = false },
                confirmButton = {
                    TextButton(
                            onClick = {
                                val from =
                                        fDateFrom.takeIf { it.isNotBlank() }?.let {
                                            runCatching { LocalDate.parse(it) }.getOrNull()
                                        }
                                val to =
                                        fDateTo.takeIf { it.isNotBlank() }?.let {
                                            runCatching { LocalDate.parse(it) }.getOrNull()
                                        }
                                onFilter(
                                        OperationFilter(
                                                dateFrom = from,
                                                dateTo = to,
                                                code = fCode.ifBlank { null },
                                                size = fSize.ifBlank { null },
                                                color = fColor.ifBlank { null }
                                        )
                                )
                                showFilter = false
                            }
                    ) { Text("Застосувати") }
                },
                dismissButton = {
                    TextButton(onClick = { showFilter = false }) { Text("Скасувати") }
                },
                title = { Text("Фільтрувати прихід") },
                text = {
                    androidx.compose.foundation.layout.Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                                fDateFrom,
                                { fDateFrom = it },
                                label = { Text("Від (yyyy-MM-dd)") }
                        )
                        OutlinedTextField(
                                fDateTo,
                                { fDateTo = it },
                                label = { Text("До (yyyy-MM-dd)") }
                        )
                        OutlinedTextField(fCode, { fCode = it }, label = { Text("Код (початок)") })
                        OutlinedTextField(fSize, { fSize = it }, label = { Text("Розмір") })
                        OutlinedTextField(fColor, { fColor = it }, label = { Text("Колір") })
                    }
                }
        )
    }
}
