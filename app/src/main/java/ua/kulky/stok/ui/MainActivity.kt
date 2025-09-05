package ua.kulky.stock.ui

import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingCart
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ua.kulky.stock.ui.screens.InventoryScreen
import ua.kulky.stock.ui.screens.SaleScreen
import ua.kulky.stock.ui.screens.StockInScreen

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels {
        MainViewModelFactory(application as App)
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
    val nav = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { nav.navigate("inventory") },
                    label = { Text("Залишки") },
                    icon = { Icon(Icons.Filled.Inventory, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { nav.navigate("stockin") },
                    label = { Text("Прихід") },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { nav.navigate("sale") },
                    label = { Text("Продаж") },
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) }
                )
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = "inventory") {
            composable("inventory") { InventoryScreen(state.inventory) }
            composable("stockin") { StockInScreen(state.balloons, onAdd = vm::addStock, onAddBalloon = vm::addBalloon) }
            composable("sale") { SaleScreen(state.balloons, onSale = vm::addSale, onFilter = vm::setCustomerFilter) }
        }
    }
}
