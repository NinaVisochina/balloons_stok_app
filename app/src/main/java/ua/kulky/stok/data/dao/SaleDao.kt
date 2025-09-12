package ua.kulky.stok.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ua.kulky.stok.data.entities.Sale

@Dao
interface SaleDao {

    @Insert
    suspend fun insert(entry: Sale): Long

    @Update
    suspend fun update(entry: Sale)

    @Delete
    suspend fun delete(entry: Sale)

    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun observeAll(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getById(id: Long): Sale?

    // 🔧 ВАЖЛИВО: balloonId (camelCase), а не balloon_id
    @Query("DELETE FROM sales WHERE balloonId = :balloonId")
    suspend fun deleteByBalloonId(balloonId: Long): Int

    @Query("SELECT COUNT(*) FROM sales WHERE balloonId = :balloonId")
    suspend fun countByBalloon(balloonId: Long): Int

    // Якщо десь використовується підрахунок продажів
    @Query("SELECT COALESCE(SUM(qty), 0) FROM sales WHERE balloonId = :balloonId")
    suspend fun totalOut(balloonId: Long): Int
}
