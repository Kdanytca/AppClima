package com.example.appclima.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.appclima.R
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(onTimeout: () -> Unit) {
    // --- Efecto de aparición del logo ---
    var alpha by remember { mutableStateOf(0f) }
    val alphaAnim = animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing)
    )

    // --- Efecto de movimiento tipo lluvia ---
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val offsetY = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    LaunchedEffect(Unit) {
        alpha = 1f // Empieza la animación
        delay(3000) // ⏳ Espera 3 segundos
        onTimeout() // 👉 Pasa a la siguiente pantalla
    }

    // --- Fondo con degradado azul oscuro ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF001833), Color(0xFF0A2540))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo2_0),
                contentDescription = "Logo AppClima",
                modifier = Modifier
                    .size(180.dp)
                    .offset(y = offsetY.value.dp)
                    .graphicsLayer(alpha = alphaAnim.value)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "AppClima",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer(alpha = alphaAnim.value)
            )
        }
    }
}
