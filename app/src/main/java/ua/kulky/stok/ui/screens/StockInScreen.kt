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
fun StockInScreen(
    balloons: List<Balloon>,
    onAdd: (balloonId: Long, qty: Int, date: LocalDate) -> Unit,
    onAddBalloon: (code: String, size: String, color: String, price: Double) -> Unit
) {
    var selectedId by remember { mutableStateOf<Long?>(balloons.firstOrNull()?.id) }
    var qty by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Додати прихід", style = MaterialTheme.typography.titleLarge)

        // Вибір кульки зі списку
        ExposedDropdownMenuBox(expanded = false, onExpandedChange = {}) {
            // спрощено: виводимо просто Label + підказка
            Text("Оберіть кульку (або додайте нижче)")
        }

        // Якщо кульок ще нема — блок додавання номенклатури
        if (balloons.isEmpty()) {
            Text("Немає кульок. Додайте нову:")
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
        } else {
            // Поле кількості та кнопка приходу
            OutlinedTextField(
                value = qty, onValueChange = { qty = it },
                label = { Text("Кількість (шт)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Button(enabled = selectedId != null && (qty.toIntOrNull() ?: 0) > 0, onClick = {
                onAdd(selectedId!!, qty.toInt(), LocalDate.now())
                qty = ""
            }) { Text("Зберегти прихід") }
        }
    }
}
