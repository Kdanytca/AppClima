package com.example.appclima.utils

import android.content.Context

private const val PREFS_NAME = "weather_prefs"
private const val KEY_LAST_CITY = "last_city"
private const val KEY_LAST_SEARCHES = "last_searches"

fun saveLastCityToPrefs(context: Context, city: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_LAST_CITY, city).apply()
}

fun getLastCityFromPrefs(context: Context): String? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(KEY_LAST_CITY, null)
}

// 🔹 NUEVAS FUNCIONES PARA GUARDAR LAS ÚLTIMAS 6 CIUDADES
fun saveRecentCitiesToPrefs(context: Context, cities: List<String>) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_LAST_SEARCHES, cities.joinToString(",")).apply()
}

fun getRecentCitiesFromPrefs(context: Context): List<String> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val saved = prefs.getString(KEY_LAST_SEARCHES, "") ?: ""
    return if (saved.isNotEmpty()) saved.split(",") else emptyList()
}


