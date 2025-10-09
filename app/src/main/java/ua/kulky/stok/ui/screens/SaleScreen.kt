package ua.kulky.stok.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import java.time.LocalDate
import java.time.YearMonth
import ua.kulky.stok.data.models.OperationFilter
import ua.kulky.stok.data.models.SaleItem
import ua.kulky.stok.ui.components.DateField
import ua.kulky.stok.ui.components.DayHeader
import ua.kulky.stok.ui.components.MonthHeader
import ua.kulky.stok.ui.components.toEpochMilli
import ua.kulky.stok.ui.components.toLocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleScreen(
        items: List<SaleItem>,
        onSaleSmart:
                (
                        code: String,
                        size: String,
                        color: String,
                        price: Double,
                        qty: Int,
                        customer: String,
                        date: LocalDate) -> Unit,
        onFilter: (OperationFilter) -> Unit,
        onEdit: (id: Long, qty: Int, customer: String, date: LocalDate) -> Unit,
        onDelete: (id: Long) -> Unit,
        codes: List<String>,
        sizes: List<String>,
        colors: List<String>,
        customers: List<String>
) {
    var code by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var customer by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf(LocalDate.now().toString()) }

    var showFilter by remember { mutableStateOf(false) }
    var fDateFrom by remember { mutableStateOf<LocalDate?>(null) }
    var fDateTo by remember { mutableStateOf<LocalDate?>(null) }
    var fCustomer by remember { mutableStateOf("") }
    var fCode by remember { mutableStateOf("") }
    var fSize by remember { mutableStateOf("") }
    var fColor by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    LaunchedEffect(items.size) { if (items.isNotEmpty()) listState.scrollToItem(0) }

    val monthExpanded = remember { mutableStateMapOf<YearMonth, Boolean>() }
    val dayExpanded = remember { mutableStateMapOf<LocalDate, Boolean>() }
    val byMonth =
            remember(items) {
                items.groupBy { YearMonth.from(it.date) }.toSortedMap(compareByDescending { it })
            }
    LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Продаж", style = MaterialTheme.typography.titleLarge) }

        // Форма з автопідказками
        item {
            AutoCompleteTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = "Код",
                    suggestions = codes
            )
        }
        item {
            AutoCompleteTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = "Розмір",
                    suggestions = sizes
            )
        }
        item {
            AutoCompleteTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = "Колір",
                    suggestions = colors
            )
        }
        item {
            OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Ціна") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Кількість") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            AutoCompleteTextField(
                    value = customer,
                    onValueChange = { customer = it },
                    label = "Покупець",
                    suggestions = customers
            )
        }
        item {
            OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("Дата (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(
                    enabled =
                            code.isNotBlank() &&
                                    size.isNotBlank() &&
                                    color.isNotBlank() &&
                                    customer.isNotBlank() &&
                                    (qty.toIntOrNull() ?: 0) > 0,
                    onClick = {
                        val p = price.toDoubleOrNull() ?: 0.0
                        val q = qty.toIntOrNull() ?: 0
                        val d =
                                runCatching { LocalDate.parse(dateStr) }
                                        .getOrDefault(LocalDate.now())
                        onSaleSmart(
                                code.trim(),
                                size.trim(),
                                color.trim(),
                                p,
                                q,
                                customer.trim(),
                                d
                        )
                        qty = ""
                        customer = ""
                    }
            ) { Text("Зберегти продаж") }
        }

        item { Button(onClick = { showFilter = true }) { Text("Фільтр") } }

        item { HorizontalDivider() }
        item { Text("Історія продажів", style = MaterialTheme.typography.titleMedium) }

        byMonth.forEach { (ym, monthItems) ->
            item(key = "m-$ym") {
                val expanded = monthExpanded.getOrPut(ym) { false }
                MonthHeader(ym, expanded) { monthExpanded[ym] = !expanded }
            }
            val byDay = monthItems.groupBy { it.date }.toSortedMap(compareByDescending { it })
            byDay.forEach { (day, dayItems) ->
                item(key = "d-$day") {
                    val expanded = dayExpanded.getOrPut(day) { false }
                    DayHeader(day, expanded) { dayExpanded[day] = !expanded }
                }
                if (dayExpanded[day] == true) {
                    items(dayItems, key = { it.id }) { e ->
                        HistoryCardSale(e, onEdit = onEdit, onDelete = onDelete)
                    }
                }
            }
        }
    }

    if (showFilter) {
        FilterDialogSale(
                initialFrom = fDateFrom,
                initialTo = fDateTo,
                initialCustomer = fCustomer,
                initialCode = fCode,
                initialSize = fSize,
                initialColor = fColor,
                onDismiss = { showFilter = false },
                onApply = { from, to, cust, codeF, sizeF, colorF ->
                    fDateFrom = from
                    fDateTo = to
                    fCustomer = cust
                    fCode = codeF
                    fSize = sizeF
                    fColor = colorF
                    onFilter(
                            OperationFilter(
                                    dateFrom = from,
                                    dateTo = to,
                                    customer = cust.ifBlank { null },
                                    code = codeF.ifBlank { null },
                                    size = sizeF.ifBlank { null },
                                    color = colorF.ifBlank { null }
                            )
                    )
                    showFilter = false
                },
                codes = codes,
                sizes = sizes,
                colors = colors,
                customers = customers
        )
    }
}

@Composable
private fun HistoryCardSale(
        e: SaleItem,
        onEdit: (id: Long, qty: Int, customer: String, date: LocalDate) -> Unit,
        onDelete: (id: Long) -> Unit
) {
    Card {
        Box(Modifier.fillMaxWidth().padding(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(end = 80.dp)) {
                Text("${e.code} • ${e.size} • ${e.color} • покупець: ${e.customer}")
                Text("Ціна: ${e.price}  Кількість: ${e.qty}")
                Text("Дата: ${e.date}")
            }
            Row(
                    Modifier.align(Alignment.BottomEnd),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                var showEdit by remember { mutableStateOf(false) }
                var showDelete by remember { mutableStateOf(false) }
                IconButton(onClick = { showEdit = true }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Редагувати")
                }
                IconButton(onClick = { showDelete = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Видалити")
                }

                if (showEdit) {
                    var eqty by remember { mutableStateOf(e.qty.toString()) }
                    var ecust by remember { mutableStateOf(e.customer) }
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
                                            onEdit(e.id, q, ecust.trim(), d)
                                            showEdit = false
                                        }
                                ) { Text("Зберегти") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEdit = false }) { Text("Скасувати") }
                            },
                            title = { Text("Редагувати продаж") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                            ecust,
                                            { ecust = it },
                                            label = { Text("Покупець") }
                                    )
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
                                TextButton(onClick = { showDelete = false }) { Text("Скасувати") }
                            },
                            title = { Text("Видалити запис продажу?") },
                            text = { Text("Цю дію не можна скасувати.") }
                    )
                }
            }
        }
    }
}

/* ---------- Фільтр з календарем і автопідказками ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDialogSale(
        initialFrom: LocalDate?,
        initialTo: LocalDate?,
        initialCustomer: String,
        initialCode: String,
        initialSize: String,
        initialColor: String,
        onDismiss: () -> Unit,
        onApply:
                (
                        from: LocalDate?,
                        to: LocalDate?,
                        customer: String,
                        code: String,
                        size: String,
                        color: String) -> Unit,
        codes: List<String>,
        sizes: List<String>,
        colors: List<String>,
        customers: List<String>
) {
    var from by remember { mutableStateOf(initialFrom) }
    var to by remember { mutableStateOf(initialTo) }
    var customer by remember { mutableStateOf(initialCustomer) }
    var code by remember { mutableStateOf(initialCode) }
    var size by remember { mutableStateOf(initialSize) }
    var color by remember { mutableStateOf(initialColor) }

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = { onApply(from, to, customer, code, size, color) }) {
                    Text("Застосувати")
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Скасувати") } },
            title = { Text("Фільтрувати продажі") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateField("Від", from, onClick = { showFromPicker = true })
                    DateField("До", to, onClick = { showToPicker = true })
                    AutoCompleteTextField(customer, { customer = it }, "Покупець", customers)
                    AutoCompleteTextField(code, { code = it }, "Код", codes)
                    AutoCompleteTextField(size, { size = it }, "Розмір", sizes)
                    AutoCompleteTextField(color, { color = it }, "Колір", colors)
                }
            }
    )

    if (showFromPicker) {
        DatePickerDialog(
                onDismissRequest = { showFromPicker = false },
                confirmButton = { TextButton(onClick = { showFromPicker = false }) { Text("OK") } },
                dismissButton = {
                    TextButton(onClick = { showFromPicker = false }) { Text("Скасувати") }
                }
        ) {
            val state = rememberDatePickerState(initialSelectedDateMillis = from?.toEpochMilli())
            DatePicker(state = state)
            LaunchedEffect(state.selectedDateMillis) {
                from = state.selectedDateMillis?.toLocalDate()
            }
        }
    }
    if (showToPicker) {
        DatePickerDialog(
                onDismissRequest = { showToPicker = false },
                confirmButton = { TextButton(onClick = { showToPicker = false }) { Text("OK") } },
                dismissButton = {
                    TextButton(onClick = { showToPicker = false }) { Text("Скасувати") }
                }
        ) {
            val state = rememberDatePickerState(initialSelectedDateMillis = to?.toEpochMilli())
            DatePicker(state = state)
            LaunchedEffect(state.selectedDateMillis) {
                to = state.selectedDateMillis?.toLocalDate()
            }
        }
    }
}

/* ---------- Перевикористовувані елементи ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoCompleteTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        suggestions: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    val filtered =
            remember(value, suggestions) {
                if (value.length >= 1)
                        suggestions.filter { it.startsWith(value, ignoreCase = true) }.take(10)
                else emptyList()
            }
    ExposedDropdownMenuBox(
            expanded = expanded && filtered.isNotEmpty(),
            onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
                value = value,
                onValueChange = {
                    onValueChange(it)
                    expanded = it.isNotEmpty()
                },
                label = { Text(label) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                singleLine = true
        )
        ExposedDropdownMenu(
                expanded = expanded && filtered.isNotEmpty(),
                onDismissRequest = { expanded = false }
        ) {
            filtered.forEach { s ->
                DropdownMenuItem(
                        text = { Text(s) },
                        onClick = {
                            onValueChange(s)
                            expanded = false
                        }
                )
            }
        }
    }
}
