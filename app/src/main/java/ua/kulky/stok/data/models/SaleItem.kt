package ua.kulky.stok.data.models

import java.time.LocalDate

data class SaleItem(
    val id: Long,
    val date: LocalDate,
    val qty: Int,
    val customer: String,
    val code: String,
    val size: String,
    val color: String,
    val price: Double,
    val manufacturer: String
)
