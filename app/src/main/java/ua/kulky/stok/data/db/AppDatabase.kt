package ua.kulky.stok.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ua.kulky.stok.data.dao.BalloonDao
import ua.kulky.stok.data.dao.SaleDao
import ua.kulky.stok.data.dao.StockInDao
import ua.kulky.stok.data.entities.Balloon
import ua.kulky.stok.data.entities.Sale
import ua.kulky.stok.data.entities.StockIn

@Database(
    entities = [Balloon::class, StockIn::class, Sale::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun balloonDao(): BalloonDao
    abstract fun stockInDao(): StockInDao
    abstract fun saleDao(): SaleDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "balloons.db"
                ).build().also { INSTANCE = it }
            }
    }
}
