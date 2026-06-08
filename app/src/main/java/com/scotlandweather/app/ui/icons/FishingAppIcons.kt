package com.scotlandweather.app.ui.icons

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

object FishingAppIcons {
    private val White = SolidColor(Color.White)
    private val Teal = SolidColor(Color(0xFF00EFF6))
    private val Blue = SolidColor(Color(0xFF4FACFE))
    private val BlueAlpha = SolidColor(Color(0xFF4FACFE).copy(alpha = 0.6f))
    private val BlueShadow = SolidColor(Color(0xFF4FACFE).copy(alpha = 0.3f))

    @Composable
    fun Temperature(modifier: Modifier = Modifier) {
        val transition = rememberInfiniteTransition(label = "TempLoop")
        val fluidLevel by transition.animateFloat(
            initialValue = 76f, targetValue = 56f,
            animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "MercuryLevel"
        )
        IconVec(modifier) {
            path(fill = null, stroke = White, strokeLineWidth = 3f) {
                moveTo(64f, 24f); lineTo(64f, 70f)
                arcTo(24f, 24f, 0f, false, true, 64f, 104f)
                arcTo(24f, 24f, 0f, false, true, 52f, 76f)
                lineTo(52f, 24f)
                arcTo(6f, 6f, 0f, false, true, 64f, 24f)
                close()
            }
            path(fill = Teal, stroke = null) {
                moveTo(58f, fluidLevel); lineTo(58f, 80f)
                arcTo(14f, 14f, 0f, false, true, 64f, 98f)
                arcTo(14f, 14f, 0f, false, true, 58f, 80f)
                close()
            }
        }
    }

    @Composable
    fun Rain(modifier: Modifier = Modifier) {
        val transition = rememberInfiniteTransition(label = "RainLoop")
        val dropOffset by transition.animateFloat(
            initialValue = 0f, targetValue = 25f,
            animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart),
            label = "RainDrop"
        )
        IconVec(modifier) {
            path(fill = White, stroke = null) {
                moveTo(40f, 70f)
                arcTo(15f, 15f, 0f, false, true, 45f, 41f)
                arcTo(20f, 20f, 0f, false, true, 80f, 35f)
                arcTo(15f, 15f, 0f, false, true, 90f, 70f)
                close()
            }
            path(fill = null, stroke = Blue, strokeLineWidth = 3f) {
                moveTo(48f, 75f + dropOffset); lineTo(44f, 85f + dropOffset)
                moveTo(66f, 75f + dropOffset); lineTo(62f, 85f + dropOffset)
                moveTo(84f, 75f + dropOffset); lineTo(80f, 85f + dropOffset)
            }
        }
    }

    @Composable
    fun Wind(modifier: Modifier = Modifier) {
        IconVec(modifier) {
            path(fill = null, stroke = Teal, strokeLineWidth = 4f) {
                moveTo(24f, 44f); lineTo(84f, 44f)
                arcTo(10f, 10f, 0f, false, false, 94f, 34f)
                arcTo(10f, 10f, 0f, false, false, 84f, 24f)
                moveTo(14f, 64f); lineTo(94f, 64f)
                moveTo(34f, 84f); lineTo(74f, 84f)
                arcTo(8f, 8f, 0f, false, true, 82f, 92f)
            }
        }
    }

    @Composable
    fun CloudCover(modifier: Modifier = Modifier) {
        IconVec(modifier) {
            path(fill = BlueAlpha, stroke = null) {
                moveTo(30f, 80f)
                arcTo(12f, 12f, 0f, false, true, 35f, 56f)
                arcTo(16f, 16f, 0f, false, true, 65f, 50f)
                arcTo(12f, 12f, 0f, false, true, 75f, 80f)
                close()
            }
            path(fill = White, stroke = null) {
                moveTo(44f, 74f)
                arcTo(15f, 15f, 0f, false, true, 49f, 45f)
                arcTo(20f, 20f, 0f, false, true, 84f, 40f)
                arcTo(15f, 15f, 0f, false, true, 94f, 74f)
                close()
            }
        }
    }

    @Composable
    fun Pressure(modifier: Modifier = Modifier) {
        val transition = rememberInfiniteTransition(label = "BaroLoop")
        val angle by transition.animateFloat(
            initialValue = -45f, targetValue = 45f,
            animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "DialAngle"
        )
        IconVec(modifier) {
            path(fill = null, stroke = White, strokeLineWidth = 3f) {
                moveTo(64f, 24f)
                arcTo(40f, 40f, 0f, false, true, 63.9f, 24f)
            }
            path(fill = null, stroke = Teal, strokeLineWidth = 4f) {
                moveTo(64f, 24f); lineTo(64f, 32f)
                moveTo(24f, 64f); lineTo(32f, 64f)
                moveTo(104f, 64f); lineTo(96f, 64f)
            }
            val rad = Math.toRadians(angle.toDouble())
            val tx = (64 + 30 * sin(rad)).toFloat()
            val ty = (64 - 30 * cos(rad)).toFloat()
            path(fill = null, stroke = Blue, strokeLineWidth = 4f, strokeLineCap = StrokeCap.Round) {
                moveTo(64f, 64f); lineTo(tx, ty)
            }
        }
    }

    @Composable
    fun UvIndex(modifier: Modifier = Modifier) {
        IconVec(modifier) {
            path(fill = Teal, stroke = null) {
                moveTo(44f, 64f)
                arcTo(20f, 20f, 0f, false, true, 83.9f, 64f)
                arcTo(20f, 20f, 0f, false, true, 44f, 64f)
                close()
            }
            path(fill = null, stroke = White, strokeLineWidth = 3f, strokeLineCap = StrokeCap.Round) {
                moveTo(64f, 14f); lineTo(64f, 28f)
                moveTo(64f, 100f); lineTo(64f, 114f)
                moveTo(14f, 64f); lineTo(28f, 64f)
                moveTo(100f, 64f); lineTo(114f, 64f)
            }
        }
    }

    @Composable
    fun MoonPhase(modifier: Modifier = Modifier) {
        val transition = rememberInfiniteTransition(label = "MoonLoop")
        val glowAlpha by transition.animateFloat(
            initialValue = 0.5f, targetValue = 1.0f,
            animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "GlowAlpha"
        )
        val glowColor = SolidColor(Color.White.copy(alpha = glowAlpha))
        IconVec(modifier) {
            path(fill = glowColor, stroke = null) {
                moveTo(64f, 34f)
                arcTo(30f, 30f, 0f, false, false, 94f, 64f)
                arcTo(24f, 24f, 0f, false, true, 64f, 34f)
                close()
            }
        }
    }

    @Composable
    fun FishActivity(modifier: Modifier = Modifier) {
        val transition = rememberInfiniteTransition(label = "FishLoop")
        val tailWag by transition.animateFloat(
            initialValue = -4f, targetValue = 4f,
            animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
            label = "TailWag"
        )
        IconVec(modifier) {
            path(fill = Blue, stroke = null) {
                moveTo(34f, 64f)
                quadTo(64f, 44f, 94f, 64f)
                quadTo(64f, 84f, 34f, 64f)
            }
            path(fill = Teal, stroke = null) {
                moveTo(34f, 64f)
                lineTo(14f, 50f + tailWag)
                lineTo(20f, 64f)
                lineTo(14f, 78f + tailWag)
                close()
            }
        }
    }

    @Composable
    fun FishingScore(modifier: Modifier = Modifier) {
        IconVec(modifier) {
            path(fill = Teal, stroke = White, strokeLineWidth = 3f) {
                moveTo(34f, 34f)
                lineTo(94f, 34f)
                lineTo(84f, 74f)
                quadTo(64f, 94f, 44f, 74f)
                close()
            }
            path(fill = null, stroke = White, strokeLineWidth = 4f, strokeLineCap = StrokeCap.Round) {
                moveTo(44f, 100f); lineTo(84f, 100f)
                moveTo(64f, 84f); lineTo(64f, 100f)
            }
        }
    }

    @Composable
    fun BestTimes(modifier: Modifier = Modifier) {
        val transition = rememberInfiniteTransition(label = "ClockLoop")
        val handRotation by transition.animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
            label = "HandSpin"
        )
        IconVec(modifier) {
            path(fill = null, stroke = White, strokeLineWidth = 3f) {
                moveTo(64f, 24f)
                arcTo(40f, 40f, 0f, false, true, 63.9f, 24f)
            }
            path(fill = null, stroke = White, strokeLineWidth = 4f, strokeLineCap = StrokeCap.Round) {
                moveTo(64f, 64f); lineTo(64f, 44f)
            }
            val rad = Math.toRadians(handRotation.toDouble())
            path(fill = null, stroke = Teal, strokeLineWidth = 3f, strokeLineCap = StrokeCap.Round) {
                moveTo(64f, 64f)
                lineTo((64 + 28 * sin(rad)).toFloat(), (64 - 28 * cos(rad)).toFloat())
            }
        }
    }

    @Composable
    fun Refresh(modifier: Modifier = Modifier) {
        IconVec(modifier) {
            path(fill = null, stroke = Blue, strokeLineWidth = 4f, strokeLineCap = StrokeCap.Round) {
                moveTo(64f, 24f)
                arcTo(40f, 40f, 0f, false, true, 24f, 64f)
            }
            path(fill = Blue, stroke = null) {
                moveTo(54f, 24f); lineTo(68f, 14f); lineTo(64f, 34f); close()
            }
        }
    }

    @Composable
    fun Location(modifier: Modifier = Modifier) {
        val transition = rememberInfiniteTransition(label = "PinLoop")
        val bounce by transition.animateFloat(
            initialValue = 0f, targetValue = -12f,
            animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "PinBounce"
        )
        IconVec(modifier) {
            path(fill = BlueShadow, stroke = null) {
                moveTo(64f, 98f)
                arcTo(12f, 4f, 0f, false, true, 63.9f, 98f)
            }
            path(fill = Teal, stroke = White, strokeLineWidth = 2f) {
                moveTo(64f, 94f + bounce)
                quadTo(34f, 64f + bounce, 34f, 44f + bounce)
                arcTo(30f, 30f, 0f, false, true, 94f, 44f + bounce)
                quadTo(94f, 64f + bounce, 64f, 94f + bounce)
                close()
            }
        }
    }

    @Composable
    private fun IconVec(modifier: Modifier = Modifier, block: ImageVector.Builder.() -> Unit) {
        val vector = ImageVector.Builder(
            name = "icon", defaultWidth = 128.dp, defaultHeight = 128.dp,
            viewportWidth = 128f, viewportHeight = 128f
        ).apply(block).build()
        Box(modifier.size(128.dp)) {
            Icon(imageVector = vector, contentDescription = null, modifier = Modifier.fillMaxSize(), tint = Color.Unspecified)
        }
    }
}
