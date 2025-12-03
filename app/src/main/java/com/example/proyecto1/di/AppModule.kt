package com.example.proyecto1.di

import android.content.Context
import com.example.proyecto1.data.remote.ApiService
import com.example.proyecto1.data.remote.api.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRetrofitClient(@ApplicationContext context: Context): RetrofitClient {
        return RetrofitClient(context)
    }


    @Singleton
    @Provides
    fun provideApiService(retrofitClient: RetrofitClient): ApiService {
        return retrofitClient.apiService
    }
}
