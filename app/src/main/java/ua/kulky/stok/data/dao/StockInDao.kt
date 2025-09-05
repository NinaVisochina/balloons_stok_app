package ua.kulky.stock.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ua.kulky.stock.data.entities.StockIn
import java.time.LocalDate

@Dao
interface StockInDao {
    @Insert
    suspend fun insert(entry: StockIn): Long

    @Query("SELECT * FROM stock_in WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun observeInRange(from: LocalDate, to: LocalDate): Flow<List<StockIn>>

    @Query("SELECT COALESCE(SUM(qty),0) FROM stock_in WHERE balloonId = :balloonId")
    suspend fun totalIn(balloonId: Long): Int
}
