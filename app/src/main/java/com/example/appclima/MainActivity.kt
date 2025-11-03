package com.example.appclima

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.appclima.model.WeatherData
import com.example.appclima.network.RetrofitInstance
import com.example.appclima.ui.splash.AnimatedSplashScreen
import com.example.appclima.ui.theme.AppClimaTheme
import com.example.appclima.utils.getLastCityFromPrefs
import com.example.appclima.utils.saveLastCityToPrefs
import com.example.appclima.utils.getRecentCitiesFromPrefs
import com.example.appclima.utils.saveRecentCitiesToPrefs
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.FlowRow
import androidx.core.content.ContextCompat




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            installSplashScreen().apply {
                setKeepOnScreenCondition { false }
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppClimaTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    AnimatedSplashScreen { showSplash = false }
                } else {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        HomeScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val apiKey = "10f59b8a28f44ff6a0902440a033e964"
    val scope = rememberCoroutineScope()

    var cityToQuery by remember { mutableStateOf(getLastCityFromPrefs(context) ?: "San Salvador") }
    var weather by remember { mutableStateOf<WeatherData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var recentCities by remember { mutableStateOf(getRecentCitiesFromPrefs(context)) }

    // ✅ Inicializar el cliente de ubicación
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // 🔹 Pedir permiso de ubicación
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            getWeatherByLocation(
                context = context,
                fusedLocationClient = fusedLocationClient,
                scope = scope,
                onResult = { weather = it },
                onLoading = { isLoading = it }
            )
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado ❌", Toast.LENGTH_SHORT).show()
        }
    }

    // 🔹 Buscar ciudad manualmente
    suspend fun searchCity(city: String) {
        try {
            isLoading = true
            val response = RetrofitInstance.api.getWeatherByCity(city.trim(), apiKey)

            weather = WeatherData(
                city = response.cityName,
                temperature = response.main.temperature,
                description = response.weather.firstOrNull()?.description ?: "N/A",
                humidity = response.main.humidity,
                pressure = response.main.pressure,
                windSpeed = response.wind.speed
            )

            saveLastCityToPrefs(context, response.cityName)
            val updatedList = (listOf(response.cityName) + recentCities.filter { it != response.cityName }).take(6)
            recentCities = updatedList
            saveRecentCitiesToPrefs(context, updatedList)
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error al buscar ciudad: ${e.message}")
            weather = WeatherData("Error", 0.0, "No se pudo obtener el clima", 0, 0, 0.0)
        } finally {
            isLoading = false
        }
    }

    // 🔹 Interfaz principal
    DynamicWeatherBackground(weather?.description) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // 🔍 Campo de texto + botón Buscar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = cityToQuery,
                    onValueChange = { cityToQuery = it },
                    label = { Text("Ciudad") },
                    singleLine = true,
                    modifier = Modifier.weight(0.7f),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        scope.launch { searchCity(cityToQuery) }
                    })
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { scope.launch { searchCity(cityToQuery) } },
                    enabled = !isLoading,
                    modifier = Modifier.weight(0.3f)
                ) {
                    Text(if (isLoading) "..." else "Buscar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animación del clima (Lottie)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val animationFile = when {
                    weather?.description == null -> "sunny.json"
                    weather!!.description.contains("lluvia", ignoreCase = true) -> "rain.json"
                    weather!!.description.contains("nublado", ignoreCase = true)
                            || weather!!.description.contains("nubes", ignoreCase = true)
                            || weather!!.description.contains("muy nubloso", ignoreCase = true)
                            || weather!!.description.contains("muy nuboso", ignoreCase = true) -> "cloudy.json"
                    weather!!.description.contains("claro", ignoreCase = true)
                            || weather!!.description.contains("soleado", ignoreCase = true) -> "sunny.json"
                    else -> "sunny.json"
                }

                val composition by rememberLottieComposition(LottieCompositionSpec.Asset(animationFile))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            // 📍 Botón para usar la ubicación actual
            Button(onClick = {
                val permission = Manifest.permission.ACCESS_FINE_LOCATION
                val granted = ActivityCompat.checkSelfPermission(context, permission) ==
                        PackageManager.PERMISSION_GRANTED

                if (granted) {
                    getWeatherByLocation(
                        context = context,
                        fusedLocationClient = fusedLocationClient,
                        scope = scope,
                        onResult = { weather = it },
                        onLoading = { isLoading = it }
                    )
                } else {
                    permissionLauncher.launch(permission)
                }
            }) {
                Icon(Icons.Default.LocationOn, contentDescription = "Ubicación actual")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Usar mi ubicación")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🌤️ Datos del clima
            when {
                isLoading -> CircularProgressIndicator()
                weather != null -> {
                    Text(weather!!.city, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("${weather!!.temperature}°C", fontSize = 38.sp)
                    Text(weather!!.description.replaceFirstChar { it.uppercase() })
                    Text("Humedad: ${weather!!.humidity}%")
                    Text("Presión: ${weather!!.pressure} hPa")
                    Text("Viento: ${weather!!.windSpeed} m/s")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🔹 Búsquedas recientes
            if (recentCities.isNotEmpty()) {
                Text("Búsquedas recientes:", fontWeight = FontWeight.Bold, color = Color.Gray)
                FlowRow(horizontalArrangement = Arrangement.Center) {
                    recentCities.forEach { city ->
                        Button(
                            onClick = {
                                cityToQuery = city
                                scope.launch { searchCity(city) }
                            },
                            modifier = Modifier.padding(4.dp),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(city)
                        }
                    }
                }
            }
        }
    }

    // 🔹 Cargar automáticamente última ciudad
    LaunchedEffect(Unit) {
        scope.launch { searchCity(cityToQuery) }
    }
}


fun getWeatherByLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    scope: CoroutineScope,
    onResult: (WeatherData) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    // Verificar permisos
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Toast.makeText(context, "Permiso de ubicación no concedido ❌", Toast.LENGTH_SHORT).show()
        return
    }

    // 🚀 Intentar obtener la última ubicación conocida
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            // ✅ Ubicación válida: obtener clima
            scope.launch {
                try {
                    onLoading(true)
                    val apiKey = "10f59b8a28f44ff6a0902440a033e964"
                    val response = RetrofitInstance.api.getWeatherByCoordinates(
                        location.latitude,
                        location.longitude,
                        apiKey
                    )

                    val weatherData = WeatherData(
                        city = response.cityName,
                        temperature = response.main.temperature,
                        description = response.weather.firstOrNull()?.description ?: "N/A",
                        humidity = response.main.humidity,
                        pressure = response.main.pressure,
                        windSpeed = response.wind.speed
                    )
                    onResult(weatherData)
                } catch (e: Exception) {
                    Log.e("HomeScreen", "Error al obtener clima: ${e.message}")
                    Toast.makeText(context, "Error al obtener el clima 🌧️", Toast.LENGTH_SHORT).show()
                } finally {
                    onLoading(false)
                }
            }
        } else {
            // No hay ubicación conocida -> pedir una nueva
            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                2000L // cada 2 segundos
            ).setMaxUpdates(1) // solo una actualización
                .build()

            val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                    val newLocation = result.lastLocation
                    if (newLocation != null) {
                        scope.launch {
                            try {
                                onLoading(true)
                                val apiKey = "10f59b8a28f44ff6a0902440a033e964"
                                val response = RetrofitInstance.api.getWeatherByCoordinates(
                                    newLocation.latitude,
                                    newLocation.longitude,
                                    apiKey
                                )
                                val weatherData = WeatherData(
                                    city = response.cityName,
                                    temperature = response.main.temperature,
                                    description = response.weather.firstOrNull()?.description
                                        ?: "N/A",
                                    humidity = response.main.humidity,
                                    pressure = response.main.pressure,
                                    windSpeed = response.wind.speed
                                )
                                onResult(weatherData)
                            } catch (e: Exception) {
                                Log.e("HomeScreen", "Error al obtener clima: ${e.message}")
                                Toast.makeText(
                                    context,
                                    "Error al obtener el clima 🌦️",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } finally {
                                onLoading(false)
                            }
                        }
                        fusedLocationClient.removeLocationUpdates(this)
                    } else {
                        Toast.makeText(context, "No se pudo obtener la ubicación 😕", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }.addOnFailureListener { e ->
        Log.e("HomeScreen", "Fallo al obtener ubicación: ${e.message}")
        Toast.makeText(context, "Error al acceder a la ubicación ❌", Toast.LENGTH_SHORT).show()
    }
}


@Composable
fun DynamicWeatherBackground(description: String?, content: @Composable () -> Unit) {
    val animationFile = when {
        description == null -> "sunny.json"
        description.contains("lluvia", ignoreCase = true) -> "rain.json"
        description.contains("nublado", ignoreCase = true)
                || description.contains("nubes", ignoreCase = true)
                || description.contains("muy nubloso", ignoreCase = true)
                || description.contains("muy nuboso", ignoreCase = true) -> "cloudy.json"
        description.contains("claro", ignoreCase = true)
                || description.contains("soleado", ignoreCase = true) -> "sunny.json"
        else -> "sunny.json"
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(animationFile))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        content()
        Spacer(modifier = Modifier.height(16.dp))
    }
}









