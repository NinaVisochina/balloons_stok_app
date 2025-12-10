package ua.kulky.stok.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.kulky.stok.data.entities.Balloon
import ua.kulky.stok.data.models.InventoryItem
import ua.kulky.stok.data.models.OperationFilter
import ua.kulky.stok.data.models.SaleItem
import ua.kulky.stok.data.models.StockInItem
import ua.kulky.stok.repo.BalloonRepository

data class UiState(
        val balloons: List<Balloon> = emptyList(),
        val inventory: List<InventoryItem> = emptyList(),
        val stockIns: List<StockInItem> = emptyList(),
        val sales: List<SaleItem> = emptyList(),
        // Довідник виробників і поточно вибраний виробник для екрану "Залишки"
        val manufacturers: List<String> = emptyList(),
        val selectedManufacturer: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(private val repo: BalloonRepository) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    // Поточні фільтри для приходу та продажів
    private val _inFilter =
            MutableStateFlow(
                    OperationFilter(
                            dateFrom = null,
                            dateTo = null,
                            customer = null,
                            code = null,
                            size = null,
                            color = null,
                            manufacturer = null
                    )
            )
    private val _saleFilter =
            MutableStateFlow(
                    OperationFilter(
                            dateFrom = null,
                            dateTo = null,
                            customer = null,
                            code = null,
                            size = null,
                            color = null,
                            manufacturer = null
                    )
            )

    // Поточний виробник для списку залишків (null = всі)
    private val _inventoryManufacturer = MutableStateFlow<String?>(null)

    init {
        // Балони
        viewModelScope.launch {
            repo.observeBalloons().collect { balloons ->
                _state.update { it.copy(balloons = balloons) }
            }
        }

        // Довідник виробників
        viewModelScope.launch {
            repo.observeManufacturers().collect { mans ->
                _state.update { it.copy(manufacturers = mans) }
            }
        }

        // Залишки з урахуванням вибраного виробника
        viewModelScope.launch {
            _inventoryManufacturer.flatMapLatest { m -> repo.observeInventory(m) }.collect { inv ->
                _state.update { it.copy(inventory = inv) }
            }
        }

        // Прихід із фільтром
        viewModelScope.launch {
            _inFilter.flatMapLatest { f -> repo.observeStockInFiltered(f) }.collect { list ->
                _state.update { it.copy(stockIns = list) }
            }
        }

        // Продаж із фільтром (тут і фільтр за покупцем/виробником)
        viewModelScope.launch {
            _saleFilter.flatMapLatest { f -> repo.observeSalesFiltered(f) }.collect { list ->
                _state.update { it.copy(sales = list) }
            }
        }
    }

    // ----------- Публічні дії для UI -----------

    fun setStockInFilter(filter: OperationFilter) {
        _inFilter.value = filter
    }

    fun setSaleFilter(filter: OperationFilter) {
        _saleFilter.value = filter
    }

    fun setInventoryManufacturer(manufacturer: String?) {
        _inventoryManufacturer.value = manufacturer?.trim().takeUnless { it.isNullOrEmpty() }
        _state.update { it.copy(selectedManufacturer = _inventoryManufacturer.value) }
    }

    fun addStockSmart(
            code: String,
            size: String,
            color: String,
            price: Double,
            qty: Int,
            date: LocalDate,
            manufacturer: String
    ) =
            viewModelScope.launch {
                repo.addStockSmart(code, size, color, price, qty, date, manufacturer)
                refreshInventory()
            }

    fun addSaleSmart(
            code: String,
            size: String,
            color: String,
            price: Double,
            qty: Int,
            customer: String,
            date: LocalDate,
            manufacturer: String
    ) =
            viewModelScope.launch {
                repo.addSaleSmart(code, size, color, price, qty, customer, date, manufacturer)
                refreshInventory()
            }

    fun editStockIn(id: Long, qty: Int, date: LocalDate) =
            viewModelScope.launch {
                repo.updateStockIn(id, qty, date)
                refreshInventory()
            }

    fun removeStockIn(id: Long) =
            viewModelScope.launch {
                repo.deleteStockIn(id)
                refreshInventory()
            }

    fun editSale(id: Long, qty: Int, customer: String, date: LocalDate) =
            viewModelScope.launch {
                repo.updateSale(id, qty, customer, date)
                refreshInventory()
            }

    fun removeSale(id: Long) =
            viewModelScope.launch {
                repo.deleteSale(id)
                refreshInventory()
            }

    fun editBalloon(
            balloonId: Long,
            code: String,
            size: String,
            color: String,
            price: Double,
            manufacturer: String
    ) =
            viewModelScope.launch {
                repo.updateBalloon(
                        balloonId = balloonId,
                        code = code,
                        size = size,
                        color = color,
                        price = price,
                        manufacturer = manufacturer // ← додано
                )
                refreshInventory()
            }

    fun removeBalloon(balloonId: Long) =
            viewModelScope.launch {
                repo.deleteBalloonCascade(balloonId)
                refreshInventory()
            }
    fun canSell(
            code: String,
            size: String,
            color: String,
            manufacturer: String,
            qty: Int
    ): Boolean {
        if (qty <= 0) return false

        val inv =
                _state.value.inventory.firstOrNull { item ->
                    item.code == code.trim() &&
                            item.size == size.trim() &&
                            item.color == color.trim() &&
                            item.manufacturer == manufacturer.trim()
                }

        val available = inv?.let { it.qtyIn - it.qtyOut } ?: 0
        return available >= qty
    }

    private fun refreshInventory() {
        // Потоки самі оновлять стейт; метод залишено для симетрії викликів.
    }
}
