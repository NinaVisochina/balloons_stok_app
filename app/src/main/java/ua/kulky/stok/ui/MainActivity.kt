package ua.kulky.stok.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ua.kulky.stok.data.db.AppDatabase
import ua.kulky.stok.repo.BalloonRepository
import ua.kulky.stok.ui.screens.InventoryScreen
import ua.kulky.stok.ui.screens.SaleScreen
import ua.kulky.stok.ui.screens.StockInScreen

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels {
        val repo = (application as? App)?.repository ?: run {
            val db = AppDatabase.get(applicationContext)
            BalloonRepository(db.balloonDao(), db.stockInDao(), db.saleDao())
        }
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(repo) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by vm.state.collectAsStateWithLifecycle()
            AppScaffold(state, vm)
        }
    }
}

@Composable
fun AppScaffold(state: UiState, vm: MainViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Списки для автопідказок
    val knownCodes = remember(state.balloons) { state.balloons.map { it.code }.distinct().sorted() }
    val knownSizes = remember(state.balloons) { state.balloons.map { it.size }.distinct().sorted() }
    val knownColors = remember(state.balloons) { state.balloons.map { it.color }.distinct().sorted() }
    val knownCustomers = remember(state.sales) { state.sales.map { it.customer }.distinct().sorted() }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "inventory",
                    onClick = { navController.navigateSingleTopTo("inventory") },
                    label = { Text("Залишки") },
                    icon = { Icon(Icons.Filled.List, contentDescription = "Залишки") }
                )
                NavigationBarItem(
                    selected = currentRoute == "stockin",
                    onClick = { navController.navigateSingleTopTo("stockin") },
                    label = { Text("Прихід") },
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Прихід") }
                )
                NavigationBarItem(
                    selected = currentRoute == "sale",
                    onClick = { navController.navigateSingleTopTo("sale") },
                    label = { Text("Продаж") },
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Продаж") }
                )
            }
        }
    ) { padding ->
        AppNavHost(
            navController = navController,
            padding = padding,
            state = state,
            vm = vm,
            knownCodes = knownCodes,
            knownSizes = knownSizes,
            knownColors = knownColors,
            knownCustomers = knownCustomers
        )
    }
}

private fun NavHostController.navigateSingleTopTo(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.startDestinationId) { saveState = true }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    padding: PaddingValues,
    state: UiState,
    vm: MainViewModel,
    knownCodes: List<String>,
    knownSizes: List<String>,
    knownColors: List<String>,
    knownCustomers: List<String>
) {
    NavHost(
        navController = navController,
        startDestination = "inventory",
        modifier = Modifier.padding(padding)
    ) {
        composable("inventory") {
            InventoryScreen(
                items = state.inventory,
                onEditBalloon = vm::editBalloon,
                onDeleteBalloon = vm::removeBalloon
            )
        }
        composable("stockin") {
            StockInScreen(
                items = state.stockIns,
                onAddSmart = vm::addStockSmart,
                onFilter = vm::setStockInFilter,
                onEdit = vm::editStockIn,
                onDelete = vm::removeStockIn,
                codes = knownCodes,
                sizes = knownSizes,
                colors = knownColors
            )
        }
        composable("sale") {
            SaleScreen(
                items = state.sales,
                onSaleSmart = vm::addSaleSmart,
                onFilter = vm::setSaleFilter,
                onEdit = vm::editSale,
                onDelete = vm::removeSale,
                codes = knownCodes,
                sizes = knownSizes,
                colors = knownColors,
                customers = knownCustomers
            )
        }
    }
}
