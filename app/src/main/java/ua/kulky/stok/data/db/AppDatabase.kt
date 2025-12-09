package ua.kulky.stok.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ua.kulky.stok.data.dao.BalloonDao
import ua.kulky.stok.data.dao.SaleDao
import ua.kulky.stok.data.dao.StockInDao
import ua.kulky.stok.data.entities.Balloon
import ua.kulky.stok.data.entities.Sale
import ua.kulky.stok.data.entities.StockIn

@Database(
    entities = [Balloon::class, StockIn::class, Sale::class],
    version = 2,                    // ⬅️ було 1 — підняли до 2
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun balloonDao(): BalloonDao
    abstract fun stockInDao(): StockInDao
    abstract fun saleDao(): SaleDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // Міграція 1→2: додаємо колонку manufacturer з дефолтним порожнім рядком
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE balloons ADD COLUMN manufacturer TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "balloons.db"
                )
                    .addMigrations(MIGRATION_1_2) // ⬅️ підключили міграцію
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
