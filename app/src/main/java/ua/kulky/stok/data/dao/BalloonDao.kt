package ua.kulky.stok.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ua.kulky.stok.data.entities.Balloon

@Dao
interface BalloonDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(balloon: Balloon): Long

    @Update
    suspend fun update(balloon: Balloon)

    @Delete
    suspend fun delete(balloon: Balloon)

    // Сортуємо: виробник → код → розмір
    @Query("SELECT * FROM balloons ORDER BY manufacturer, code, size")
    fun observeAll(): Flow<List<Balloon>>

    @Query("SELECT * FROM balloons WHERE id = :id")
    suspend fun getById(id: Long): Balloon?

    @Query("SELECT id FROM balloons WHERE code = :code LIMIT 1")
    suspend fun getIdByCode(code: String): Long?

    // Новий пошук з урахуванням виробника (унікальність по четвірці)
    @Query("""
        SELECT * FROM balloons
        WHERE code = :code AND size = :size AND color = :color AND manufacturer = :manufacturer
        LIMIT 1
    """)
    suspend fun findByAttrs(
        code: String,
        size: String,
        color: String,
        manufacturer: String
    ): Balloon?

    @Query("DELETE FROM balloons WHERE id = :id")
    suspend fun deleteById(id: Long)

    // Для автопідказок виробників
    @Query("SELECT DISTINCT manufacturer FROM balloons WHERE manufacturer <> '' ORDER BY manufacturer")
    fun observeManufacturers(): Flow<List<String>>
}
