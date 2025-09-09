package ua.kulky.stok.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import ua.kulky.stok.data.entities.StockIn

@Dao
interface StockInDao {
    @Insert suspend fun insert(entry: StockIn): Long

    @Query("SELECT * FROM stock_in WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun observeInRange(from: LocalDate, to: LocalDate): Flow<List<StockIn>>

    @Query("SELECT COALESCE(SUM(qty),0) FROM stock_in WHERE balloonId = :balloonId")
    suspend fun totalIn(balloonId: Long): Int

    @Query("SELECT * FROM stock_in ORDER BY date DESC") fun observeAll(): Flow<List<StockIn>>
}
