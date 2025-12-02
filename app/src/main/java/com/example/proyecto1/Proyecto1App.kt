package com.example.proyecto1

import android.app.Application
import android.util.Log
import com.example.proyecto1.data.remote.api.RetrofitClient
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class Proyecto1App : Application() {

    @Inject
    lateinit var retrofitClient: RetrofitClient

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            try {
                retrofitClient.initialize(this@Proyecto1App)
                Log.d("Proyecto1App", "✅ RetrofitClient inicializado")
            } catch (e: Exception) {
                Log.e("Proyecto1App", "❌ Error inicializando RetrofitClient", e)
            }
        }
    }

    companion object {
        private const val TAG = "Proyecto1App"
    }
}
