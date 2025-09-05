package ua.kulky.stock.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ua.kulky.stock.data.db.AppDatabase
import ua.kulky.stock.repo.BalloonRepository

class App : Application() {
    lateinit var repository: BalloonRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        repository = BalloonRepository(db.balloonDao(), db.stockInDao(), db.saleDao())
    }
}

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(private val app: App) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(app.repository) as T
    }
}
