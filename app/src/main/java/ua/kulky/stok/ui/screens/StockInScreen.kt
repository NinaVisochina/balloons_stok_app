package ua.kulky.stok.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ua.kulky.stok.data.entities.Balloon
import ua.kulky.stok.data.models.OperationFilter
import ua.kulky.stok.data.models.StockInItem
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockInScreen(
    balloons: List<Balloon>,
    items: List<StockInItem>,
    onAdd: (balloonId: Long, qty: Int, date: LocalDate) -> Unit,
    onAddBalloon: (code: String, size: String, color: String, price: Double) -> Unit,
    onFilter: (OperationFilter) -> Unit
) {
    var selectedId by remember { mutableStateOf<Long?>(balloons.firstOrNull()?.id) }
    var qty by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf(LocalDate.now().toString()) } // yyyy-MM-dd

    var code by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    var showFilter by remember { mutableStateOf(false) }
    var fDateFrom by remember { mutableStateOf("") }
    var fDateTo by remember { mutableStateOf("") }
    var fCode by remember { mutableStateOf("") }
    var fSize by remember { mutableStateOf("") }
    var fColor by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Прихід", style = MaterialTheme.typography.titleLarge)

        // Спрощений вибір (поки без dropdown-меню)
        if (balloons.isNotEmpty()) {
            Text("Обрана кулька: id=${selectedId ?: balloons.first().id}")
            selectedId = selectedId ?: balloons.first().id
        }

        OutlinedTextField(
            value = qty, onValueChange = { qty = it },
            label = { Text("Кількість (шт)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = dateStr, onValueChange = { dateStr = it },
            label = { Text("Дата (yyyy-MM-dd)") }
        )

        Button(enabled = selectedId != null && (qty.toIntOrNull() ?: 0) > 0, onClick = {
            val date = runCatching { LocalDate.parse(dateStr) }.getOrDefault(LocalDate.now())
            onAdd(selectedId!!, qty.toInt(), date)
            qty = ""
        }) { Text("Зберегти прихід") }

        if (balloons.isEmpty()) {
            Divider()
            Text("Додати нову номенклатуру")
            OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Код") })
            OutlinedTextField(value = size, onValueChange = { size = it }, label = { Text("Розмір") })
            OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Колір") })
            OutlinedTextField(
                value = price, onValueChange = { price = it },
                label = { Text("Ціна") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Button(onClick = {
                val p = price.toDoubleOrNull() ?: 0.0
                onAddBalloon(code.trim(), size.trim(), color.trim(), p)
            }) { Text("Додати кульку") }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showFilter = true }) { Text("Фільтр") }
        }

        if (showFilter) {
            AlertDialog(
                onDismissRequest = { showFilter = false },
                confirmButton = {
                    TextButton(onClick = {
                        val from = fDateFrom.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                        val to = fDateTo.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                        onFilter(OperationFilter(from, to, null, fCode.ifBlank { null }, fSize.ifBlank { null }, fColor.ifBlank { null }))
                        showFilter = false
                    }) { Text("Застосувати") }
                },
                dismissButton = { TextButton(onClick = { showFilter = false }) { Text("Скасувати") } },
                title = { Text("Фільтрувати прихід") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(fDateFrom, { fDateFrom = it }, label = { Text("Від (yyyy-MM-dd)") })
                        OutlinedTextField(fDateTo, { fDateTo = it }, label = { Text("До (yyyy-MM-dd)") })
                        OutlinedTextField(fCode, { fCode = it }, label = { Text("Код (початок)") })
                        OutlinedTextField(fSize, { fSize = it }, label = { Text("Розмір") })
                        OutlinedTextField(fColor, { fColor = it }, label = { Text("Колір") })
                    }
                }
            )
        }

        Divider()
        Text("Історія приходу (останні зверху)")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 72.dp)
        ) {
            items(items, key = { it.id }) { e ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${e.code} • ${e.size} • ${e.color}")
                        Text("Ціна: ${e.price}  Кількість: ${e.qty}")
                        Text("Дата: ${e.date}")
                    }
                }
            }
        }
    }
}
