package com.example.proyecto1.data.remote

import com.example.proyecto1.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // La URL base ahora se lee desde el archivo build.gradle.kts
    // Esto permite cambiar entre emulador y dispositivo físico fácilmente.
    private const val BASE_URL = BuildConfig.API_BASE_URL

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
