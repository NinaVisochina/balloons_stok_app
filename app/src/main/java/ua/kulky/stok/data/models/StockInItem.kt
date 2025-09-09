package ua.kulky.stok.data.models

import java.time.LocalDate

data class StockInItem(
    val id: Long,
    val date: LocalDate,
    val qty: Int,
    val code: String,
    val size: String,
    val color: String,
    val price: Double
)
