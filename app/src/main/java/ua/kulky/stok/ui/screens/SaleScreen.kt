package ua.kulky.stok.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
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
                        date: LocalDate,
                        manufacturer: String) -> Unit,
        onFilter: (OperationFilter) -> Unit,
        onEdit: (id: Long, qty: Int, customer: String, date: LocalDate) -> Unit,
        onDelete: (id: Long) -> Unit,
        codes: List<String>,
        sizes: List<String>,
        colors: List<String>,
        customers: List<String>,
        manufacturers: List<String>
) {
    // -------- стан форми введення --------
    var code by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var customer by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    // -------- стан фільтра --------
    var showFilter by remember { mutableStateOf(false) }
    var fDateFrom by remember { mutableStateOf<LocalDate?>(null) }
    var fDateTo by remember { mutableStateOf<LocalDate?>(null) }
    var fCustomer by remember { mutableStateOf("") }
    var filterSummary by remember { mutableStateOf("") }
    var fCode by remember { mutableStateOf("") }
    var fSize by remember { mutableStateOf("") }
    var fColor by remember { mutableStateOf("") }
    var fManufacturer by remember { mutableStateOf("") }

    // -------- режим: історія / додавання --------
    var isAdding by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    LaunchedEffect(items.size) {
        if (items.isNotEmpty() && !isAdding) {
            listState.scrollToItem(0)
        }
    }

    val monthExpanded = remember { mutableStateMapOf<YearMonth, Boolean>() }
    val dayExpanded = remember { mutableStateMapOf<LocalDate, Boolean>() }
    val byMonth =
            remember(items) {
                items.groupBy { YearMonth.from(it.date) }.toSortedMap(compareByDescending { it })
            }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
                modifier =
                        Modifier.fillMaxSize()
                                .imePadding()
                                .navigationBarsPadding()
                                .padding(end = 8.dp), // трошки місця під бігунок
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("Продаж", style = MaterialTheme.typography.titleLarge) }

            if (isAdding) {
                // ---------- РЕЖИМ ДОДАВАННЯ ----------
                item {
                    Button(onClick = { isAdding = false }) {
                        Text("Повернутися до історії продажу")
                    }
                }

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
                    AutoCompleteTextField(
                            value = manufacturer,
                            onValueChange = { manufacturer = it },
                            label = "Виробник",
                            suggestions = manufacturers
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
                /* item {
                    OutlinedTextField(
                            value = dateStr,
                            onValueChange = { dateStr = it },
                            label = { Text("Дата (yyyy-MM-dd)") },
                            modifier = Modifier.fillMaxWidth()
                    )
                } */
                item { DateField(label = "Дата", date = date, onClick = { showDatePicker = true }) }

                item {
                    Button(
                            enabled = (qty.toIntOrNull() ?: 0) > 0,
                            onClick = {
                                val p = price.toDoubleOrNull() ?: 0.0
                                val q = qty.toIntOrNull() ?: 0
                                val d = date

                                onSaleSmart(
                                        code.trim(),
                                        size.trim(),
                                        color.trim(),
                                        p,
                                        q,
                                        customer.trim(),
                                        d,
                                        manufacturer.trim()
                                )

                                // обнуляємо поля
                                code = ""
                                size = ""
                                color = ""
                                manufacturer = ""
                                price = ""
                                qty = ""
                                customer = ""
                                date = LocalDate.now()
                            }
                    ) { Text("Зберегти продаж") }
                }
                item {
                    Spacer(Modifier.height(400.dp)) // можеш змінити на 320.dp / 480.dp, по відчуттю
                }
            } else {
                // ---------- РЕЖИМ ІСТОРІЇ ----------
                item { Button(onClick = { isAdding = true }) { Text("Додати продаж") } }

                item {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = { showFilter = true }) { Text("Фільтр") }
                        if (filterSummary.isNotBlank()) {
                            Text(text = filterSummary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                item { HorizontalDivider() }
                item { Text("Історія продажів", style = MaterialTheme.typography.titleMedium) }

                byMonth.forEach { (ym, monthItems) ->
                    item(key = "m-$ym") {
                        val mExpanded =
                                monthExpanded.getOrPut(ym) { false } // згорнуто за замовчуванням
                        MonthHeader(ym, mExpanded) { monthExpanded[ym] = !mExpanded }
                    }

                    // показуємо дні ТІЛЬКИ якщо місяць розгорнутий
                    if (monthExpanded[ym] == true) {
                        val byDay =
                                monthItems
                                        .groupBy { it.date }
                                        .toSortedMap(compareByDescending { it })
                        byDay.forEach { (day, dayItems) ->
                            item(key = "d-$day") {
                                val dExpanded = dayExpanded.getOrPut(day) { false }
                                DayHeader(day, dExpanded) { dayExpanded[day] = !dExpanded }
                            }
                            if (dayExpanded[day] == true) {
                                items(dayItems, key = { it.id }) { e ->
                                    HistoryCardSale(e, onEdit = onEdit, onDelete = onDelete)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 🔹 Сам бігунок справа
        LazyListScrollbar(
                listState = listState,
                modifier =
                        Modifier.align(Alignment.CenterEnd)
                                .padding(vertical = 8.dp, horizontal = 2.dp)
        )
    }
    if (showDatePicker) {
        DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Скасувати") }
                }
        ) {
            val state = rememberDatePickerState(initialSelectedDateMillis = date.toEpochMilli())
            DatePicker(state = state)
            LaunchedEffect(state.selectedDateMillis) {
                state.selectedDateMillis?.toLocalDate()?.let { picked -> date = picked }
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
                initialManufacturer = fManufacturer,
                onDismiss = {
                    // 🔹 Скидаємо фільтр
                    showFilter = false
                    fDateFrom = null
                    fDateTo = null
                    fCustomer = ""
                    fCode = ""
                    fSize = ""
                    fColor = ""
                    fManufacturer = ""
                    filterSummary = ""
                    onFilter(
                            OperationFilter(
                                    dateFrom = null,
                                    dateTo = null,
                                    customer = null,
                                    code = null,
                                    size = null,
                                    color = null,
                                    manufacturer = null
                            )
                    )
                },
                onApply = { from, to, cust, codeF, sizeF, colorF, manufacturerF ->
                    fDateFrom = from
                    fDateTo = to
                    fCustomer = cust
                    fCode = codeF
                    fSize = sizeF
                    fColor = colorF
                    fManufacturer = manufacturerF

                    // 🔹 Формуємо текст, у яких полях є фільтр
                    val parts = mutableListOf<String>()
                    if (from != null || to != null) parts.add("дата")
                    if (cust.isNotBlank()) parts.add("покупець")
                    if (codeF.isNotBlank()) parts.add("код")
                    if (sizeF.isNotBlank()) parts.add("розмір")
                    if (colorF.isNotBlank()) parts.add("колір")
                    if (manufacturerF.isNotBlank()) parts.add("виробник")

                    filterSummary =
                            if (parts.isEmpty()) "" else "Фільтр: " + parts.joinToString(", ")

                    onFilter(
                            OperationFilter(
                                    dateFrom = from,
                                    dateTo = to,
                                    customer = cust.ifBlank { null },
                                    code = codeF.ifBlank { null },
                                    size = sizeF.ifBlank { null },
                                    color = colorF.ifBlank { null },
                                    manufacturer = manufacturerF.ifBlank { null }
                            )
                    )
                    showFilter = false
                },
                codes = codes,
                sizes = sizes,
                colors = colors,
                customers = customers,
                manufacturers = manufacturers
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
                Text(
                        "${e.code} • ${e.size} • ${e.color}" +
                                if (e.manufacturer.isNotBlank()) " • ${e.manufacturer}" else ""
                )
                Text("Ціна: ${e.price}  Кількість: ${e.qty}")
                Text("Покупець: ${e.customer}")
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
        initialManufacturer: String, // 🔹 нове
        onDismiss: () -> Unit,
        onApply:
                (
                        from: LocalDate?,
                        to: LocalDate?,
                        customer: String,
                        code: String,
                        size: String,
                        color: String,
                        manufacturer: String) -> Unit,
        codes: List<String>,
        sizes: List<String>,
        colors: List<String>,
        customers: List<String>,
        manufacturers: List<String>
) {
    var from by remember { mutableStateOf(initialFrom) }
    var to by remember { mutableStateOf(initialTo) }
    var customer by remember { mutableStateOf(initialCustomer) }
    var code by remember { mutableStateOf(initialCode) }
    var size by remember { mutableStateOf(initialSize) }
    var color by remember { mutableStateOf(initialColor) }
    var manufacturer by remember { mutableStateOf(initialManufacturer) }

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                        onClick = { onApply(from, to, customer, code, size, color, manufacturer) }
                ) { Text("Застосувати") }
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
                    AutoCompleteTextField(
                            manufacturer,
                            { manufacturer = it },
                            "Виробник",
                            manufacturers
                    )
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

@Composable
private fun LazyListScrollbar(
        listState: LazyListState,
        modifier: Modifier = Modifier,
        thickness: Dp = 4.dp
) {
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier) {
        val totalItems = listState.layoutInfo.totalItemsCount
        val visibleItems = listState.layoutInfo.visibleItemsInfo.size

        if (totalItems == 0 || visibleItems == 0) return@BoxWithConstraints

        val barHeightPx = constraints.maxHeight.toFloat()
        val proportionVisible = visibleItems.toFloat() / totalItems.toFloat()
        val minThumbHeightPx = with(density) { 6.dp.toPx() }
        val thumbHeightPx = maxOf(barHeightPx * proportionVisible, minThumbHeightPx)

        val maxScrollIndex = (totalItems - visibleItems).coerceAtLeast(1)
        val scrollProgress by remember {
            derivedStateOf { listState.firstVisibleItemIndex.toFloat() / maxScrollIndex.toFloat() }
        }

        val maxOffsetPx = barHeightPx - thumbHeightPx
        val offsetPx = maxOffsetPx * scrollProgress

        val thumbHeightDp = with(density) { thumbHeightPx.toDp() }
        val offsetDp = with(density) { offsetPx.toDp() }

        Box(
                modifier =
                        Modifier.fillMaxHeight()
                                .width(thickness)
                                .background(
                                        color =
                                                MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.08f
                                                ),
                                        shape = RoundedCornerShape(100)
                                )
        ) {
            Box(
                    modifier =
                            Modifier.width(thickness)
                                    .height(thumbHeightDp)
                                    .offset(y = offsetDp)
                                    .background(
                                            color =
                                                    MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.9f
                                                    ),
                                            shape = RoundedCornerShape(100)
                                    )
            )
        }
    }
}
