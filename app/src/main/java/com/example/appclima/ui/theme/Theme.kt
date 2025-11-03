package com.example.appclima.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color.White,     // Fondo general blanco
    surface = Color.White,        // Fondo de superficies blanco
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,   // Texto negro
    onSurface = Color.Black       // Texto negro
)

@Composable
fun AppClimaTheme(
    content: @Composable () -> Unit
) {
    // Fuerza siempre el esquema claro
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        // Fondo blanco global que envuelve toda la app
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
        ) {
            content()
        }
    }
}
