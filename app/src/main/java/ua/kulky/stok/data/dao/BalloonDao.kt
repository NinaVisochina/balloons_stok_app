package ua.kulky.stok.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ua.kulky.stok.data.entities.Balloon

@Dao
interface BalloonDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(balloon: Balloon): Long

    @Update suspend fun update(balloon: Balloon)

    @Delete suspend fun delete(balloon: Balloon)

    @Query("SELECT * FROM balloons ORDER BY code") fun observeAll(): Flow<List<Balloon>>

    @Query("SELECT * FROM balloons WHERE id = :id") suspend fun getById(id: Long): Balloon?

    @Query("SELECT id FROM balloons WHERE code = :code LIMIT 1")
    suspend fun getIdByCode(code: String): Long?

    @Query("SELECT * FROM balloons WHERE code = :code AND size = :size AND color = :color LIMIT 1")
    suspend fun findByAttrs(code: String, size: String, color: String): Balloon?

    @Query("DELETE FROM balloons WHERE id = :id") suspend fun deleteById(id: Long)
}
