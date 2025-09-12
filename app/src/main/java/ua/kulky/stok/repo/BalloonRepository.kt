package ua.kulky.stok.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ua.kulky.stok.data.dao.BalloonDao
import ua.kulky.stok.data.dao.SaleDao
import ua.kulky.stok.data.dao.StockInDao
import ua.kulky.stok.data.entities.Balloon
import ua.kulky.stok.data.entities.Sale
import ua.kulky.stok.data.entities.StockIn
import ua.kulky.stok.data.models.InventoryItem
import ua.kulky.stok.data.models.OperationFilter
import ua.kulky.stok.data.models.SaleItem
import ua.kulky.stok.data.models.StockInItem
import java.time.LocalDate

class BalloonRepository(
    private val balloonDao: BalloonDao,
    private val stockInDao: StockInDao,
    private val saleDao: SaleDao
) {
    // ----------------- Спостереження базових сутностей -----------------

    fun observeBalloons(): Flow<List<Balloon>> = balloonDao.observeAll()

    // ----------------- Додавання/оновлення сутностей -----------------

    suspend fun addBalloon(balloon: Balloon): Long = balloonDao.insert(balloon)

    suspend fun addStockIn(balloonId: Long, qty: Int, date: LocalDate) {
        stockInDao.insert(StockIn(balloonId = balloonId, qty = qty, date = date))
    }

    suspend fun addSale(balloonId: Long, qty: Int, customer: String, date: LocalDate) {
        saleDao.insert(Sale(balloonId = balloonId, qty = qty, customerName = customer, date = date))
    }

    /**
     * Якщо кульки (code+size+color) нема — створюємо; якщо є й ціна > 0 та змінилась — оновлюємо.
     * Повертає id кульки.
     */
    suspend fun ensureBalloon(code: String, size: String, color: String, price: Double): Long {
        val existing = balloonDao.findByAttrs(code.trim(), size.trim(), color.trim())
        return if (existing != null) {
            val newPrice = if (price > 0.0) price else existing.price
            if (newPrice != existing.price) {
                balloonDao.update(existing.copy(price = newPrice))
            }
            existing.id
        } else {
            balloonDao.insert(
                Balloon(
                    code = code.trim(),
                    size = size.trim(),
                    color = color.trim(),
                    price = price.coerceAtLeast(0.0)
                )
            )
        }
    }

    /** Зручне додавання приходу за атрибутами кульки */
    suspend fun addStockSmart(code: String, size: String, color: String, price: Double, qty: Int, date: LocalDate) {
        val id = ensureBalloon(code, size, color, price)
        addStockIn(id, qty, date)
    }

    /** Зручне додавання продажу за атрибутами кульки */
    suspend fun addSaleSmart(
        code: String,
        size: String,
        color: String,
        price: Double,
        qty: Int,
        customer: String,
        date: LocalDate
    ) {
        val id = ensureBalloon(code, size, color, price)
        addSale(id, qty, customer, date)
    }

    // ----------------- Інвентар (залишки) -----------------
    // Обчислюємо Σ(in) та Σ(out) у пам'яті з потоків і сортуємо: код ↑, розмір 10→12→...
    fun observeInventory(): Flow<List<InventoryItem>> =
        combine(
            balloonDao.observeAll(),
            stockInDao.observeAll(),
            saleDao.observeAll()
        ) { balloons, ins, sales ->
            val inById = ins.groupBy { it.balloonId }.mapValues { (_, list) -> list.sumOf { it.qty } }
            val outById = sales.groupBy { it.balloonId }.mapValues { (_, list) -> list.sumOf { it.qty } }

            balloons.map { b ->
                InventoryItem(
                    balloonId = b.id,
                    code = b.code,
                    size = b.size,
                    color = b.color,
                    price = b.price,
                    qtyIn = inById[b.id] ?: 0,
                    qtyOut = outById[b.id] ?: 0
                )
            }.sortedWith(compareBy<InventoryItem>({ codeKey(it.code) }, { sizeKey(it.size) }))
        }

    // ----------------- Історія приходу з фільтрами -----------------

    fun observeStockInFiltered(filter: OperationFilter): Flow<List<StockInItem>> =
        combine(stockInDao.observeAll(), balloonDao.observeAll()) { ins, balloons ->
            val byId = balloons.associateBy { it.id }

            ins.mapNotNull { si ->
                val b = byId[si.balloonId] ?: return@mapNotNull null
                StockInItem(
                    id = si.id,
                    date = si.date,
                    qty = si.qty,
                    code = b.code,
                    size = b.size,
                    color = b.color,
                    price = b.price
                )
            }.filter { item ->
                (filter.dateFrom == null || !item.date.isBefore(filter.dateFrom)) &&
                (filter.dateTo == null || !item.date.isAfter(filter.dateTo)) &&
                (filter.code.isNullOrBlank() || item.code.startsWith(filter.code, ignoreCase = true)) &&
                (filter.size.isNullOrBlank() || item.size.equals(filter.size, ignoreCase = true)) &&
                (filter.color.isNullOrBlank() || item.color.equals(filter.color, ignoreCase = true))
            }.sortedByDescending { it.date }
        }

    // ----------------- Історія продажів з фільтрами (включно з покупцем) -----------------

    fun observeSalesFiltered(filter: OperationFilter): Flow<List<SaleItem>> =
        combine(saleDao.observeAll(), balloonDao.observeAll()) { sales, balloons ->
            val byId = balloons.associateBy { it.id }

            sales.mapNotNull { s ->
                val b = byId[s.balloonId] ?: return@mapNotNull null
                SaleItem(
                    id = s.id,
                    date = s.date,
                    qty = s.qty,
                    customer = s.customerName,
                    code = b.code,
                    size = b.size,
                    color = b.color,
                    price = b.price
                )
            }.filter { item ->
                (filter.dateFrom == null || !item.date.isBefore(filter.dateFrom)) &&
                (filter.dateTo == null || !item.date.isAfter(filter.dateTo)) &&
                (filter.customer.isNullOrBlank() || item.customer.contains(filter.customer, ignoreCase = true)) &&
                (filter.code.isNullOrBlank() || item.code.startsWith(filter.code, ignoreCase = true)) &&
                (filter.size.isNullOrBlank() || item.size.equals(filter.size, ignoreCase = true)) &&
                (filter.color.isNullOrBlank() || item.color.equals(filter.color, ignoreCase = true))
            }.sortedByDescending { it.date }
        }

    // ----------------- Редагування / Видалення операцій -----------------

    suspend fun updateStockIn(id: Long, qty: Int, date: LocalDate) {
        val cur = stockInDao.getById(id) ?: return
        stockInDao.update(cur.copy(qty = qty, date = date))
    }

    suspend fun deleteStockIn(id: Long) {
        val cur = stockInDao.getById(id) ?: return
        stockInDao.delete(cur)
    }

    suspend fun updateSale(id: Long, qty: Int, customer: String, date: LocalDate) {
        val cur = saleDao.getById(id) ?: return
        saleDao.update(cur.copy(qty = qty, customerName = customer, date = date))
    }

    suspend fun deleteSale(id: Long) {
        val cur = saleDao.getById(id) ?: return
        saleDao.delete(cur)
    }

    // ----------------- Редагування / Видалення позицій -----------------

    suspend fun updateBalloon(
        balloonId: Long,
        code: String,
        size: String,
        color: String,
        price: Double
    ) {
        val cur = balloonDao.getById(balloonId) ?: return
        balloonDao.update(
            cur.copy(
                code = code.trim(),
                size = size.trim(),
                color = color.trim(),
                price = price.coerceAtLeast(0.0)
            )
        )
    }

    /**
     * Видалити позицію разом з операціями (каскад).
     * Якщо в тебе в @Entity вже стоїть onDelete=CASCADE — тоді достатньо balloonDao.deleteById.
     */
    suspend fun deleteBalloonCascade(balloonId: Long) {
        saleDao.deleteByBalloonId(balloonId)
        stockInDao.deleteByBalloonId(balloonId)
        balloonDao.deleteById(balloonId)
    }

    // ----------------- Допоміжні ключі сортування -----------------

    private fun codeKey(code: String): Int =
        code.trim().takeWhile { it.isDigit() }.toIntOrNull() ?: Int.MAX_VALUE

    private fun sizeKey(size: String): Int =
        Regex("(\\d+)").find(size)?.groupValues?.get(1)?.toIntOrNull() ?: Int.MAX_VALUE
}
