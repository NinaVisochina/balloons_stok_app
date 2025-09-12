package ua.kulky.stok.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ua.kulky.stok.data.entities.StockIn

@Dao
interface StockInDao {

    @Insert
    suspend fun insert(entry: StockIn): Long

    @Update
    suspend fun update(entry: StockIn)

    @Delete
    suspend fun delete(entry: StockIn)

    @Query("SELECT * FROM stock_in ORDER BY date DESC")
    fun observeAll(): Flow<List<StockIn>>

    @Query("SELECT * FROM stock_in WHERE id = :id")
    suspend fun getById(id: Long): StockIn?

    // 🔧 ВАЖЛИВО: використовуємо camelCase назву колонки balloonId
    @Query("DELETE FROM stock_in WHERE balloonId = :balloonId")
    suspend fun deleteByBalloonId(balloonId: Long): Int

    @Query("SELECT COUNT(*) FROM stock_in WHERE balloonId = :balloonId")
    suspend fun countByBalloon(balloonId: Long): Int

    // Якщо десь використовується підрахунок приходу
    @Query("SELECT COALESCE(SUM(qty), 0) FROM stock_in WHERE balloonId = :balloonId")
    suspend fun totalIn(balloonId: Long): Int
}
