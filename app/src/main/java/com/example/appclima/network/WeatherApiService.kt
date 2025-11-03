package com.example.appclima.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ------------------ MODELOS DE RESPUESTA ------------------

data class WeatherResponse(
    @SerializedName("name") val cityName: String,
    @SerializedName("main") val main: MainData,
    @SerializedName("weather") val weather: List<WeatherDesc>,
    @SerializedName("wind") val wind: WindData
)

data class MainData(
    @SerializedName("temp") val temperature: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("pressure") val pressure: Int
)

data class WeatherDesc(
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class WindData(
    @SerializedName("speed") val speed: Double
)

// ------------------ INTERFAZ DE RETROFIT ------------------

interface WeatherApiService {

    // 🔹 Obtener clima por nombre de ciudad
    @GET("data/2.5/weather")
    suspend fun getWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "es"
    ): WeatherResponse

    // 🔹 NUEVO: Obtener clima por coordenadas (latitud/longitud)
    @GET("data/2.5/weather")
    suspend fun getWeatherByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "es"
    ): WeatherResponse
}

// ------------------ INSTANCIA RETROFIT ------------------

object RetrofitInstance {
    private const val BASE_URL = "https://api.openweathermap.org/"

    val api: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}

