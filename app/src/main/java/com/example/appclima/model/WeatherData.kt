#!/usr/bin/env kotlin
package com.example.appclima.model

data class WeatherData(
    val city: String,
    val temperature: Double,
    val description: String,
    val humidity: Int? = null,
    val pressure: Int? = null,
    val windSpeed: Double? = null,
)

