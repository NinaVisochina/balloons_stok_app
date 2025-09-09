package ua.kulky.stok.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.kulky.stok.data.entities.Balloon
import ua.kulky.stok.data.models.*
import ua.kulky.stok.data.models.InventoryItem
import ua.kulky.stok.repo.BalloonRepository

data class UiState(
        val balloons: List<Balloon> = emptyList(),
        val inventory: List<InventoryItem> = emptyList(),
        val stockIns: List<StockInItem> = emptyList(), // історія приходу
        val sales: List<SaleItem> = emptyList(), // історія продажів
        val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(private val repo: BalloonRepository) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _customerFilter = MutableStateFlow<String?>(null)
    val salesByCustomer = _customerFilter.flatMapLatest { repo.observeSalesByCustomer(it) }

    private val _inFilter = MutableStateFlow(OperationFilter())
    private val _saleFilter = MutableStateFlow(OperationFilter())

    init {
        viewModelScope.launch {
            // Балони + залишки
            repo.observeBalloons().collect { list ->
                _state.update { it.copy(balloons = list) }
                val inv = repo.observeInventory().first()
                _state.update { it.copy(inventory = inv) }
            }
        }
        viewModelScope.launch {
            _inFilter.collect { f ->
                repo.observeStockInFiltered(f).collect { items ->
                    _state.update { it.copy(stockIns = items) }
                }
            }
        }
        viewModelScope.launch {
            _saleFilter.collect { f ->
                repo.observeSalesFiltered(f).collect { items ->
                    _state.update { it.copy(sales = items) }
                }
            }
        }
    }
    fun setStockInFilter(f: OperationFilter) {
        _inFilter.value = f
    }
    fun setSaleFilter(f: OperationFilter) {
        _saleFilter.value = f
    }

    fun setCustomerFilter(query: String?) {
        _customerFilter.value = query?.ifBlank { null }
    }

    fun addBalloon(code: String, size: String, color: String, price: Double) =
            viewModelScope.launch {
                try {
                    repo.addBalloon(Balloon(code = code, size = size, color = color, price = price))
                    refreshInventory()
                } catch (e: Exception) {
                    _state.update { it.copy(error = e.message) }
                }
            }

    fun addStock(balloonId: Long, qty: Int, date: LocalDate) =
            viewModelScope.launch {
                repo.addStockIn(balloonId, qty, date)
                refreshInventory()
            }

    fun addSale(balloonId: Long, qty: Int, customer: String, date: LocalDate) =
            viewModelScope.launch {
                repo.addSale(balloonId, qty, customer, date)
                refreshInventory()
            }

    private fun refreshInventory() =
            viewModelScope.launch {
                val inv = repo.observeInventory().first()
                _state.update { it.copy(inventory = inv) }
            }
}
