package com.example.astrodefender

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AstroDefenderGame()
        }
    }
}

data class Asteroid(
    var x: Float,
    var y: Float,
    var dx: Float,
    var dy: Float,
    var radius: Float,
    var big: Boolean
)

data class Bullet(
    var x: Float,
    var y: Float,
    var dx: Float,
    var dy: Float
)

@Composable
fun AstroDefenderGame() {
    var started by remember { mutableStateOf(false) }

    if (!started) {
        MainMenu {
            started = true
        }
    } else {
        GameScreen {
            started = false
        }
    }
}

@Composable
fun MainMenu(onPlay: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ASTRO DEFENDER", color = Color.White)
            Spacer(modifier = Modifier.height(30.dp))
            Button(onClick = onPlay) {
                Text("PLAY")
            }
        }
    }
}

@Composable
fun GameScreen(onGameOverBack: () -> Unit) {
    var shipX by remember { mutableStateOf(500f) }
    var shipY by remember { mutableStateOf(900f) }
    var angle by remember { mutableStateOf(-90f) }
    var gameOver by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }

    val bullets = remember { mutableStateListOf<Bullet>() }
    val asteroids = remember {
        mutableStateListOf(
            Asteroid(100f, 100f, 3f, 2f, 55f, true),
            Asteroid(800f, 200f, -2f, 3f, 55f, true),
            Asteroid(500f, 400f, 2f, -2f, 40f, false)
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16)

            if (!gameOver) {
                bullets.forEach {
                    it.x += it.dx
                    it.y += it.dy
                }

                asteroids.forEach {
                    it.x += it.dx
                    it.y += it.dy

                    if (it.x < 0) it.x = 1000f
                    if (it.x > 1000f) it.x = 0f
                    if (it.y < 0) it.y = 1800f
                    if (it.y > 1800f) it.y = 0f
                }

                val bulletsToRemove = mutableListOf<Bullet>()
                val asteroidsToRemove = mutableListOf<Asteroid>()
                val asteroidsToAdd = mutableListOf<Asteroid>()

                for (bullet in bullets) {
                    for (asteroid in asteroids) {
                        val distance = hypot(
                            bullet.x - asteroid.x,
                            bullet.y - asteroid.y
                        )

                        if (distance < asteroid.radius) {
                            bulletsToRemove.add(bullet)
                            asteroidsToRemove.add(asteroid)
                            score += 10

                            if (asteroid.big) {
                                asteroidsToAdd.add(
                                    Asteroid(
                                        asteroid.x,
                                        asteroid.y,
                                        4f,
                                        3f,
                                        30f,
                                        false
                                    )
                                )
                                asteroidsToAdd.add(
                                    Asteroid(
                                        asteroid.x,
                                        asteroid.y,
                                        -4f,
                                        -3f,
                                        30f,
                                        false
                                    )
                                )
                            }
                        }
                    }
                }

                bullets.removeAll(bulletsToRemove)
                asteroids.removeAll(asteroidsToRemove)
                asteroids.addAll(asteroidsToAdd)

                for (asteroid in asteroids) {
                    val distance = hypot(shipX - asteroid.x, shipY - asteroid.y)
                    if (distance < asteroid.radius + 25f) {
                        gameOver = true
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            shipX = width / 2
            shipY = height / 2

            asteroids.forEach {
                drawCircle(
                    color = Color.Gray,
                    radius = it.radius,
                    center = Offset(it.x, it.y)
                )
            }

            bullets.forEach {
                drawCircle(
                    color = Color.Cyan,
                    radius = 8f,
                    center = Offset(it.x, it.y)
                )
            }

            val rad = Math.toRadians(angle.toDouble())
            val tip = Offset(
                shipX + cos(rad).toFloat() * 40f,
                shipY + sin(rad).toFloat() * 40f
            )
            val left = Offset(
                shipX + cos(rad + 2.5).toFloat() * 35f,
                shipY + sin(rad + 2.5).toFloat() * 35f
            )
            val right = Offset(
                shipX + cos(rad - 2.5).toFloat() * 35f,
                shipY + sin(rad - 2.5).toFloat() * 35f
            )

            val path = Path().apply {
                moveTo(tip.x, tip.y)
                lineTo(left.x, left.y)
                lineTo(right.x, right.y)
                close()
            }

            drawPath(path, Color.White)
        }

        Text(
            text = "Score: $score",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = { angle -= 15f }) {
                Text("LEFT")
            }

            Button(onClick = {
                val rad = Math.toRadians(angle.toDouble())
                bullets.add(
                    Bullet(
                        shipX,
                        shipY,
                        cos(rad).toFloat() * 15f,
                        sin(rad).toFloat() * 15f
                    )
                )
            }) {
                Text("FIRE")
            }

            Button(onClick = { angle += 15f }) {
                Text("RIGHT")
            }
        }

        if (gameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("GAME OVER", color = Color.Red)
                    Text("Score: $score", color = Color.White)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = onGameOverBack) {
                        Text("Back to Menu")
                    }
                }
            }
        }
    }
}