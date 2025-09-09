package ua.kulky.stok.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val balloonId: Long,
    val qty: Int,
    val customerName: String,   // Ім'я покупця
    val date: LocalDate         // Дата продажу (для сортування)
)
