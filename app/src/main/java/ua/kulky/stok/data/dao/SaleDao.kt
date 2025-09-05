package ua.kulky.stock.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ua.kulky.stock.data.entities.Sale
import java.time.LocalDate

@Dao
interface SaleDao {
    @Insert
    suspend fun insert(sale: Sale): Long

    @Query("SELECT * FROM sales WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun observeInRange(from: LocalDate, to: LocalDate): Flow<List<Sale>>

    @Query("""
        SELECT * FROM sales 
        WHERE (:customer IS NULL OR customerName LIKE '%' || :customer || '%')
        ORDER BY date DESC, customerName COLLATE NOCASE
    """)
    fun observeByCustomer(customer: String?): Flow<List<Sale>>

    @Query("SELECT COALESCE(SUM(qty),0) FROM sales WHERE balloonId = :balloonId")
    suspend fun totalOut(balloonId: Long): Int
}
