package ua.kulky.stok.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ua.kulky.stok.data.models.InventoryItem
import java.text.NumberFormat

@Composable
fun InventoryScreen(items: List<InventoryItem>) {
    val currency = NumberFormat.getCurrencyInstance()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentPadding = PaddingValues(bottom = 72.dp)
    ) {
        items(items, key = { it.balloonId }) { it ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("${it.code} • ${it.size} • ${it.color}", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text("Ціна: ${currency.format(it.price)}")
                    Text("Прихід: ${it.qtyIn}  Продаж: ${it.qtyOut}")
                    Text("Залишок: ${it.stock}")
                }
            }
        }
    }
}
