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
data class ForecastResponse(
    val forecast: Forecast
)

data class Forecast(
    val forecastday: List<ForecastDay>
)

data class ForecastDay(
    val date: String,
    val day: Day
)

data class Day(
    val maxtemp_c: Float,
    val mintemp_c: Float,
    val avgtemp_c: Float,
    val condition: Condition
)