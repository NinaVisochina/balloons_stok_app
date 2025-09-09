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
import ua.kulky.stok.data.models.SaleItem
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleScreen(
    balloons: List<Balloon>,
    items: List<SaleItem>,
    onSale: (balloonId: Long, qty: Int, customer: String, date: LocalDate) -> Unit,
    onFilter: (OperationFilter) -> Unit
) {
    var selectedId by remember { mutableStateOf<Long?>(balloons.firstOrNull()?.id) }
    var qty by remember { mutableStateOf("") }
    var customer by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf(LocalDate.now().toString()) }

    var showFilter by remember { mutableStateOf(false) }
    var fDateFrom by remember { mutableStateOf("") }
    var fDateTo by remember { mutableStateOf("") }
    var fCustomer by remember { mutableStateOf("") }
    var fCode by remember { mutableStateOf("") }
    var fSize by remember { mutableStateOf("") }
    var fColor by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Продаж", style = MaterialTheme.typography.titleLarge)

        if (balloons.isNotEmpty()) {
            Text("Обрана кулька: id=${selectedId ?: balloons.first().id}")
            selectedId = selectedId ?: balloons.first().id
        }

        OutlinedTextField(value = customer, onValueChange = { customer = it }, label = { Text("Ім'я покупця") })
        OutlinedTextField(
            value = qty, onValueChange = { qty = it },
            label = { Text("Кількість (шт)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(value = dateStr, onValueChange = { dateStr = it }, label = { Text("Дата (yyyy-MM-dd)") })

        Button(enabled = selectedId != null && (qty.toIntOrNull() ?: 0) > 0 && customer.isNotBlank(), onClick = {
            val date = runCatching { LocalDate.parse(dateStr) }.getOrDefault(LocalDate.now())
            onSale(selectedId!!, qty.toInt(), customer.trim(), date)
            qty = ""; customer = ""
        }) { Text("Зберегти продаж") }

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
                        onFilter(OperationFilter(from, to, fCustomer.ifBlank { null }, fCode.ifBlank { null }, fSize.ifBlank { null }, fColor.ifBlank { null }))
                        showFilter = false
                    }) { Text("Застосувати") }
                },
                dismissButton = { TextButton(onClick = { showFilter = false }) { Text("Скасувати") } },
                title = { Text("Фільтрувати продажі") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(fDateFrom, { fDateFrom = it }, label = { Text("Від (yyyy-MM-dd)") })
                        OutlinedTextField(fDateTo, { fDateTo = it }, label = { Text("До (yyyy-MM-dd)") })
                        OutlinedTextField(fCustomer, { fCustomer = it }, label = { Text("Покупець") })
                        OutlinedTextField(fCode, { fCode = it }, label = { Text("Код (початок)") })
                        OutlinedTextField(fSize, { fSize = it }, label = { Text("Розмір") })
                        OutlinedTextField(fColor, { fColor = it }, label = { Text("Колір") })
                    }
                }
            )
        }

        Divider()
        Text("Історія продажів (останні зверху)")
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 72.dp)) {
            items(items, key = { it.id }) { e ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${e.code} • ${e.size} • ${e.color} • покупець: ${e.customer}")
                        Text("Ціна: ${e.price}  Кількість: ${e.qty}")
                        Text("Дата: ${e.date}")
                    }
                }
            }
        }
    }
}
