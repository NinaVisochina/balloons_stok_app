package ua.kulky.stock.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "stock_in")
data class StockIn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val balloonId: Long,
    val qty: Int,
    val date: LocalDate
)
