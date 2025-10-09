package ua.kulky.stok.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthHeader(ym: YearMonth, expanded: Boolean, onToggle: () -> Unit) {
    Surface(onClick = onToggle, tonalElevation = 2.dp, shape = MaterialTheme.shapes.medium) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${ym.monthValue}.${ym.year}")
            Text(if (expanded) "▲" else "▼")
        }
    }
}

@Composable
fun DayHeader(day: LocalDate, expanded: Boolean, onToggle: () -> Unit) {
    Surface(onClick = onToggle, tonalElevation = 1.dp, shape = MaterialTheme.shapes.small) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(day.toString(), style = MaterialTheme.typography.labelLarge)
            Text(if (expanded) "—" else "+")
        }
    }
}
