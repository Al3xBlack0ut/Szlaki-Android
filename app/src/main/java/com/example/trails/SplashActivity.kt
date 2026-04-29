package com.example.trails

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplashScreen(onTimeout = {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            })
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // Używamy Animatable zamiast InfiniteTransition, aby animacja mogła się zakończyć
    val xOffset = remember { Animatable(-0.2f) }
    val verticalBob = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animacja podskakiwania (bobbing) - działa dopóki jedzie rower
        val bobJob = launch {
            while (true) {
                verticalBob.animateTo(-5f, tween(125, easing = FastOutSlowInEasing))
                verticalBob.animateTo(0f, tween(125, easing = FastOutSlowInEasing))
            }
        }

        // Przejazd roweru - raz od startu do końca
        xOffset.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
        )

        // Po zakończeniu przejazdu zatrzymujemy bobbing i przechodzimy dalej
        bobJob.cancel()
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TRASY",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 12.sp,
                    fontSize = 64.sp
                ),
                color = Color(0xFF001D36)
            )

            Spacer(modifier = Modifier.height(80.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 40.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                val primaryColor = Color(0xFF001D36)

                // Przerywana linia – "ścieżka"
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    drawLine(
                        color = primaryColor.copy(alpha = 0.15f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = maxWidth * xOffset.value)
                        .size(60.dp)
                        .graphicsLayer {
                            translationY = verticalBob.value
                        }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
