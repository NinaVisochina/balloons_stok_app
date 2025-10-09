package ua.kulky.stok.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun DateField(label: String, date: LocalDate?, onClick: () -> Unit) {
    OutlinedTextField(
        value = date?.toString() ?: "",
        onValueChange = {},
        label = { Text("$label (yyyy-MM-dd)") },
        modifier = Modifier.fillMaxWidth(),
        enabled = false,
        trailingIcon = { TextButton(onClick = onClick) { Text("Обрати") } }
    )
}

fun LocalDate.toEpochMilli(): Long =
    this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
