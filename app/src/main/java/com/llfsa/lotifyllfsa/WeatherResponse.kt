package com.llfsa.lotifyllfsa

data class WeatherResponse(
    val current: CurrentWeather
)

data class CurrentWeather(
    val temp_c: Float,
    val condition: Condition
)

data class Condition(
    val text: String
)