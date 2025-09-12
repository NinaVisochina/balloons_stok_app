package ua.kulky.stok.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import java.time.LocalDate

data class UiState(
    val balloons: List<Balloon> = emptyList(),
    val inventory: List<InventoryItem> = emptyList(),
    val stockIns: List<StockInItem> = emptyList(),
    val sales: List<SaleItem> = emptyList()
)

class MainViewModel(private val repo: BalloonRepository) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    // Поточні фільтри для приходу та продажів
    private val _inFilter = MutableStateFlow(
        OperationFilter(
            dateFrom = null, dateTo = null,
            customer = null, code = null, size = null, color = null
        )
    )
    private val _saleFilter = MutableStateFlow(
        OperationFilter(
            dateFrom = null, dateTo = null,
            customer = null, code = null, size = null, color = null
        )
    )

    init {
        // Балони
        viewModelScope.launch {
            repo.observeBalloons().collect { balloons ->
                _state.update { it.copy(balloons = balloons) }
            }
        }
        // Залишки
        viewModelScope.launch {
            repo.observeInventory().collect { inv ->
                _state.update { it.copy(inventory = inv) }
            }
        }
        // Прихід із фільтром
        viewModelScope.launch {
            _inFilter.flatMapLatest { f -> repo.observeStockInFiltered(f) }
                .collect { list -> _state.update { it.copy(stockIns = list) } }
        }
        // Продаж із фільтром (тут і фільтр за покупцем!)
        viewModelScope.launch {
            _saleFilter.flatMapLatest { f -> repo.observeSalesFiltered(f) }
                .collect { list -> _state.update { it.copy(sales = list) } }
        }
    }

    // ----------- Публічні дії для UI -----------

    fun setStockInFilter(filter: OperationFilter) {
        _inFilter.value = filter
    }

    fun setSaleFilter(filter: OperationFilter) {
        _saleFilter.value = filter
    }

    fun addStockSmart(
        code: String, size: String, color: String, price: Double, qty: Int, date: LocalDate
    ) = viewModelScope.launch {
        repo.addStockSmart(code, size, color, price, qty, date)
        refreshInventory()
    }

    fun addSaleSmart(
        code: String, size: String, color: String, price: Double, qty: Int,
        customer: String, date: LocalDate
    ) = viewModelScope.launch {
        repo.addSaleSmart(code, size, color, price, qty, customer, date)
        refreshInventory()
    }

    fun editStockIn(id: Long, qty: Int, date: LocalDate) = viewModelScope.launch {
        repo.updateStockIn(id, qty, date)
        refreshInventory()
    }

    fun removeStockIn(id: Long) = viewModelScope.launch {
        repo.deleteStockIn(id)
        refreshInventory()
    }

    fun editSale(id: Long, qty: Int, customer: String, date: LocalDate) = viewModelScope.launch {
        repo.updateSale(id, qty, customer, date)
        refreshInventory()
    }

    fun removeSale(id: Long) = viewModelScope.launch {
        repo.deleteSale(id)
        refreshInventory()
    }

    fun editBalloon(balloonId: Long, code: String, size: String, color: String, price: Double) =
        viewModelScope.launch {
            repo.updateBalloon(balloonId, code, size, color, price)
            refreshInventory()
        }

    fun removeBalloon(balloonId: Long) = viewModelScope.launch {
        repo.deleteBalloonCascade(balloonId)
        refreshInventory()
    }

    private fun refreshInventory() {
        // Залишимо порожнім: потік observeInventory() сам оновить state
        // Але цей метод зручно викликати після змін для симетрії.
    }
}
