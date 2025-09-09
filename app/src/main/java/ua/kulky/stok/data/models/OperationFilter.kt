package ua.kulky.stok.data.models

import java.time.LocalDate

data class OperationFilter(
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null,
    val customer: String? = null, // тільки для продажів (ігнорується в приході)
    val code: String? = null,
    val size: String? = null,
    val color: String? = null
)
