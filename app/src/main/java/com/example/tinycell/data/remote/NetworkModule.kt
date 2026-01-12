package com.example.tinycell.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * TODO: Network Configuration
 * 1. [BASE_URL]: Update with the real API production/staging URL.
 * 2. [DI]: In the next phase, move this to a Hilt @Module or a Singleton provider.
 */
object NetworkModule {
    private const val BASE_URL = "https://api.example.com/" // Fake endpoint

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
