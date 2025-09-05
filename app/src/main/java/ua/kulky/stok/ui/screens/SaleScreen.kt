package ua.kulky.stock.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ua.kulky.stock.data.entities.Balloon
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleScreen(
    balloons: List<Balloon>,
    onSale: (balloonId: Long, qty: Int, customer: String, date: LocalDate) -> Unit,
    onFilter: (String?) -> Unit
) {
    var selectedId by remember { mutableStateOf<Long?>(balloons.firstOrNull()?.id) }
    var qty by remember { mutableStateOf("") }
    var customer by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Продаж", style = MaterialTheme.typography.titleLarge)

        // Фільтр за ім’ям покупця (для історії/списків — якщо додаси)
        OutlinedTextField(value = filter, onValueChange = {
            filter = it; onFilter(it)
        }, label = { Text("Фільтр: ім’я покупця") })

        // Вибір кульки
        ExposedDropdownMenuBox(expanded = false, onExpandedChange = {}) {
            Text("Оберіть кульку")
        }

        OutlinedTextField(
            value = customer, onValueChange = { customer = it },
            label = { Text("Ім'я покупця") }
        )
        OutlinedTextField(
            value = qty, onValueChange = { qty = it },
            label = { Text("Кількість (шт)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(enabled = selectedId != null && (qty.toIntOrNull() ?: 0) > 0 && customer.isNotBlank(), onClick = {
            onSale(selectedId!!, qty.toInt(), customer.trim(), LocalDate.now())
            qty = ""; customer = ""
        }) { Text("Зберегти продаж") }
    }
}
