package com.llfsa.lotifyllfsa

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("current.json")
    fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("lang") lang: String // Parámetro para especificar el idioma
    ): Call<WeatherResponse>
}