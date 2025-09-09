package ua.kulky.stok.repo

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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

class BalloonRepository(
        private val balloonDao: BalloonDao,
        private val stockInDao: StockInDao,
        private val saleDao: SaleDao
) {
    // --- CRUD / дії ---
    fun observeBalloons(): Flow<List<Balloon>> = balloonDao.observeAll()

    suspend fun addBalloon(balloon: Balloon): Long = balloonDao.insert(balloon)

    suspend fun addStockIn(balloonId: Long, qty: Int, date: LocalDate) {
        stockInDao.insert(StockIn(balloonId = balloonId, qty = qty, date = date))
    }

    suspend fun addSale(balloonId: Long, qty: Int, customer: String, date: LocalDate) {
        saleDao.insert(Sale(balloonId = balloonId, qty = qty, customerName = customer, date = date))
    }

    // --- ЗАЛИШКИ: Σ(in) - Σ(out), відсортовано: код ↑, всередині розміри 10 → 12 → … ---
    fun observeInventory(): Flow<List<InventoryItem>> =
            combine(
                    balloonDao.observeAll(),
                    stockInDao
                            .observeAll(), // потребує @Query("SELECT * FROM stock_in ORDER BY date
                    // DESC") fun observeAll(): Flow<List<StockIn>>
                    saleDao.observeAll() // потребує @Query("SELECT * FROM sales ORDER BY date
                    // DESC")   fun observeAll(): Flow<List<Sale>>
                    ) { balloons, ins, sales ->
                val inById =
                        ins.groupBy { it.balloonId }.mapValues { (_, list) ->
                            list.sumOf { it.qty }
                        }
                val outById =
                        sales.groupBy { it.balloonId }.mapValues { (_, list) ->
                            list.sumOf { it.qty }
                        }

                balloons
                        .map { b ->
                            InventoryItem(
                                    balloonId = b.id,
                                    code = b.code,
                                    size = b.size,
                                    color = b.color,
                                    price = b.price,
                                    qtyIn = inById[b.id] ?: 0,
                                    qtyOut = outById[b.id] ?: 0
                            )
                        }
                        .sortedWith(
                                compareBy<InventoryItem>({ codeKey(it.code) }, { sizeKey(it.size) })
                        )
            }

    // --- ІСТОРІЯ ПРИХОДУ з фільтрами ---
    fun observeStockInFiltered(filter: OperationFilter): Flow<List<StockInItem>> =
            combine(stockInDao.observeAll(), balloonDao.observeAll()) { ins, balloons ->
                val byId = balloons.associateBy { it.id }

                ins
                        .mapNotNull { si ->
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
                        }
                        .filter { item ->
                            (filter.dateFrom == null || !item.date.isBefore(filter.dateFrom)) &&
                                    (filter.dateTo == null || !item.date.isAfter(filter.dateTo)) &&
                                    (filter.code.isNullOrBlank() ||
                                            item.code.startsWith(filter.code, ignoreCase = true)) &&
                                    (filter.size.isNullOrBlank() ||
                                            item.size.equals(filter.size, ignoreCase = true)) &&
                                    (filter.color.isNullOrBlank() ||
                                            item.color.equals(filter.color, ignoreCase = true))
                        }
                        .sortedByDescending { it.date }
            }

    // --- ІСТОРІЯ ПРОДАЖІВ з фільтрами (включно з покупцем) ---
    fun observeSalesFiltered(filter: OperationFilter): Flow<List<SaleItem>> =
            combine(saleDao.observeAll(), balloonDao.observeAll()) { sales, balloons ->
                val byId = balloons.associateBy { it.id }

                sales
                        .mapNotNull { s ->
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
                        }
                        .filter { item ->
                            (filter.dateFrom == null || !item.date.isBefore(filter.dateFrom)) &&
                                    (filter.dateTo == null || !item.date.isAfter(filter.dateTo)) &&
                                    (filter.customer.isNullOrBlank() ||
                                            item.customer.contains(
                                                    filter.customer,
                                                    ignoreCase = true
                                            )) &&
                                    (filter.code.isNullOrBlank() ||
                                            item.code.startsWith(filter.code, ignoreCase = true)) &&
                                    (filter.size.isNullOrBlank() ||
                                            item.size.equals(filter.size, ignoreCase = true)) &&
                                    (filter.color.isNullOrBlank() ||
                                            item.color.equals(filter.color, ignoreCase = true))
                        }
                        .sortedByDescending { it.date }
            }

    // --- допоміжні ключі сортування ---
    private fun codeKey(code: String): Int =
            code.trim().takeWhile { it.isDigit() }.toIntOrNull() ?: Int.MAX_VALUE

    private fun sizeKey(size: String): Int =
            Regex("(\\d+)").find(size)?.groupValues?.get(1)?.toIntOrNull() ?: Int.MAX_VALUE

    fun observeSalesByCustomer(customer: String?): Flow<List<Sale>> =
            saleDao.observeByCustomer(customer)

    fun observeStockInRange(from: LocalDate, to: LocalDate) = stockInDao.observeInRange(from, to)

    fun observeSalesInRange(from: LocalDate, to: LocalDate) = saleDao.observeInRange(from, to)
}
