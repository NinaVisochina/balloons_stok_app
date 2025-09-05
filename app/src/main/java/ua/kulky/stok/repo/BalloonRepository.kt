package ua.kulky.stock.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ua.kulky.stock.data.dao.BalloonDao
import ua.kulky.stock.data.dao.SaleDao
import ua.kulky.stock.data.dao.StockInDao
import ua.kulky.stock.data.entities.Balloon
import ua.kulky.stock.data.entities.Sale
import ua.kulky.stock.data.entities.StockIn
import ua.kulky.stock.data.models.InventoryItem
import java.time.LocalDate

class BalloonRepository(
    private val balloonDao: BalloonDao,
    private val stockInDao: StockInDao,
    private val saleDao: SaleDao
) {

    fun observeBalloons(): Flow<List<Balloon>> = balloonDao.observeAll()

    suspend fun addBalloon(balloon: Balloon): Long = balloonDao.insert(balloon)

    suspend fun addStockIn(balloonId: Long, qty: Int, date: LocalDate) {
        stockInDao.insert(StockIn(balloonId = balloonId, qty = qty, date = date))
    }

    suspend fun addSale(balloonId: Long, qty: Int, customer: String, date: LocalDate) {
        saleDao.insert(Sale(balloonId = balloonId, qty = qty, customerName = customer, date = date))
    }

    /**
     * Рахуємо залишки: sum(in) - sum(out) для кожної кульки.
     * Потік буде оновлюватись автоматично при змінах у таблицях.
     */
    fun observeInventory(): Flow<List<InventoryItem>> {
        val balloonsFlow = balloonDao.observeAll()
        // Хитрий, але простий шлях: при зміні списку кульок — разово тягнемо totals через suspend (не реактивно по рядках),
        // якщо хочеш повністю реактивно — потрібні об’єднані запити/вьюхи.
        return balloonsFlow.map { list ->
            list.map { b ->
                val qtyIn = stockInDao.totalIn(b.id)
                val qtyOut = saleDao.totalOut(b.id)
                InventoryItem(
                    balloonId = b.id,
                    code = b.code, size = b.size, color = b.color, price = b.price,
                    qtyIn = qtyIn, qtyOut = qtyOut
                )
            }
        }
    }

    fun observeSalesByCustomer(customer: String?): Flow<List<Sale>> =
        saleDao.observeByCustomer(customer)

    fun observeStockInRange(from: LocalDate, to: LocalDate) =
        stockInDao.observeInRange(from, to)

    fun observeSalesInRange(from: LocalDate, to: LocalDate) =
        saleDao.observeInRange(from, to)
}
