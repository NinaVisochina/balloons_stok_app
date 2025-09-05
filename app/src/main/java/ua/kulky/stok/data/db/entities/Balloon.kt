package ua.kulky.stock.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "balloons")
data class Balloon(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,          // Код
    val size: String,          // Розмір
    val color: String,         // Колір
    val price: Double          // Ціна
)
