package ua.kulky.stok.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ua.kulky.stok.data.db.AppDatabase
import ua.kulky.stok.repo.BalloonRepository
import ua.kulky.stok.util.CrashLogger

class App : Application() {
    lateinit var repository: BalloonRepository
        private set

    override fun onCreate() {
        super.onCreate()
        CrashLogger.init(this)
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
