package ua.kulky.stock.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.kulky.stock.data.entities.Balloon
import ua.kulky.stock.data.models.InventoryItem
import ua.kulky.stock.repo.BalloonRepository
import java.time.LocalDate

data class UiState(
    val balloons: List<Balloon> = emptyList(),
    val inventory: List<InventoryItem> = emptyList(),
    val error: String? = null
)

class MainViewModel(private val repo: BalloonRepository) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _customerFilter = MutableStateFlow<String?>(null)
    val salesByCustomer = _customerFilter.flatMapLatest { repo.observeSalesByCustomer(it) }

    init {
        viewModelScope.launch {
            repo.observeBalloons().collect { list ->
                _state.update { it.copy(balloons = list) }
                // оновлюємо інвентар
                val inv = repo.observeInventory().first()
                _state.update { it.copy(inventory = inv) }
            }
        }
    }

    fun setCustomerFilter(query: String?) {
        _customerFilter.value = query?.ifBlank { null }
    }

    fun addBalloon(code: String, size: String, color: String, price: Double) = viewModelScope.launch {
        try {
            repo.addBalloon(Balloon(code = code, size = size, color = color, price = price))
            refreshInventory()
        } catch (e: Exception) {
            _state.update { it.copy(error = e.message) }
        }
    }

    fun addStock(balloonId: Long, qty: Int, date: LocalDate) = viewModelScope.launch {
        repo.addStockIn(balloonId, qty, date)
        refreshInventory()
    }

    fun addSale(balloonId: Long, qty: Int, customer: String, date: LocalDate) = viewModelScope.launch {
        repo.addSale(balloonId, qty, customer, date)
        refreshInventory()
    }

    private fun refreshInventory() = viewModelScope.launch {
        val inv = repo.observeInventory().first()
        _state.update { it.copy(inventory = inv) }
    }
}
