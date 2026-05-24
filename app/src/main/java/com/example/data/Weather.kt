package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val current: CurrentWeather
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    @Json(name = "temperature_2m") val temperature: Double,
    @Json(name = "weathercode") val weatherCode: Int,
    @Json(name = "windspeed_10m") val windSpeed: Double,
    val time: String? = null
)

interface WeatherService {
    @GET("forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,weathercode,windspeed_10m"
    ): WeatherResponse
}

fun getWeatherLabel(code: Int): Pair<String, String> {
    return when (code) {
        0 -> "☀️" to "Clear"
        1, 2, 3 -> "⛅" to "Cloudy"
        45, 48 -> "🌫️" to "Fog"
        51, 53, 55, 56, 57, 61, 63, 65, 66, 67 -> "🌧️" to "Rain"
        71, 73, 75, 77 -> "❄️" to "Snow"
        80, 81, 82 -> "🌦️" to "Showers"
        else -> "⛈️" to "Storm"
    }
}
