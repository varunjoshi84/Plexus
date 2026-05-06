package com.example.plexus.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.safeDrawingPadding
// ─── Colors ───────────────────────────────────────────
object PlexusColors {
    val BgDark        = Color(0xFF080818)
    val CardBg        = Color(0xFF0E0E28)
    val PurplePrimary = Color(0xFF7B5CF0)
    val CyanPrimary   = Color(0xFF00C8F0)
    val CyanLight     = Color(0xFF00E8FF)
    val CyanGreen     = Color(0xFF00E8A8)
    val TextWhite     = Color(0xFFFFFFFF)
    val TextMuted     = Color(0xFF8899BB)
}

// ─── Gradients ────────────────────────────────────────
object PlexusGradients {
    val logoGradient get() = Brush.linearGradient(
        colors = listOf(Color(0xFF7B5CF0), Color(0xFF00C8F0))
    )
    val cyanText get() = Brush.linearGradient(
        colors = listOf(Color(0xFF7B5CF0), Color(0xFF00C8F0))
    )
    val cyanButton get() = Brush.linearGradient(
        colors = listOf(Color(0xFF7B5CF0), Color(0xFF00C8F0))
    )
    val cyanButtonDisabled get() = Brush.linearGradient(
        colors = listOf(
            Color(0xFF7B5CF0).copy(alpha = 0.3f),
            Color(0xFF00C8F0).copy(alpha = 0.3f)
        )
    )
    val cardBorder get() = Brush.linearGradient(
        colors = listOf(
            Color(0xFF7B5CF0).copy(alpha = 0.5f),
            Color(0xFF00C8F0).copy(alpha = 0.1f),
            Color(0xFF00C8F0).copy(alpha = 0.3f)
        )
    )
    val ambient get() = Brush.radialGradient(
        colors = listOf(Color(0xFF7B5CF0).copy(alpha = 0.2f), Color.Transparent)
    )
}

// ─── Background ───────────────────────────────────────
@Composable
fun PlexusBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PlexusColors.BgDark)
            .safeDrawingPadding() // ← ADD THIS — fixes all screens at once
    ) {
        PlexusGrid()
        PlexusAmbientGlow()
        content()
    }
}

// ─── Grid ─────────────────────────────────────────────
@Composable
fun PlexusGrid(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val gridSize = 40.dp.toPx()
        val color = Color(0xFF7B5CF0).copy(alpha = 0.04f)
        var x = 0f
        while (x < size.width) {
            drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            x += gridSize
        }
        var y = 0f
        while (y < size.height) {
            drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += gridSize
        }
    }
}

// ─── Ambient Glow ─────────────────────────────────────
@Composable
fun PlexusAmbientGlow(
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center
) {
    val infinite = rememberInfiniteTransition(label = "ambient")
    val scale by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOut), RepeatMode.Reverse),
        label = "scale"
    )
    val alpha by infinite.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOut), RepeatMode.Reverse),
        label = "alpha"
    )
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(alignment)
                .size(320.dp)
                .scale(scale)
                .alpha(alpha)
                .background(PlexusGradients.ambient, CircleShape)
        )
    }
}

// ─── Ripple Rings ─────────────────────────────────────
@Composable
fun PlexusRippleRings(
    modifier: Modifier = Modifier,
    ringCount: Int = 3,
    ringSize: Int = 200
) {
    Box(modifier = modifier.size(ringSize.dp)) {
        repeat(ringCount) { i ->
            val t = rememberInfiniteTransition(label = "ripple_$i")
            val scale by t.animateFloat(
                initialValue = 0.4f, targetValue = 1.4f,
                animationSpec = infiniteRepeatable(
                    tween(3000, delayMillis = i * 700, easing = LinearEasing),
                    RepeatMode.Restart
                ), label = "rs_$i"
            )
            val alpha by t.animateFloat(
                initialValue = 0.7f, targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    tween(3000, delayMillis = i * 700, easing = LinearEasing),
                    RepeatMode.Restart
                ), label = "ra_$i"
            )
            Box(
                modifier = Modifier
                    .size(ringSize.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .drawBehind {
                        drawCircle(
                            color = Color(0xFF7B5CF0).copy(alpha = 0.3f),
                            radius = size.minDimension / 2,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
            )
        }
    }
}

// ─── Status Badge ─────────────────────────────────────
@Composable
fun PlexusStatusBadge(
    label: String,
    dotColor: Color
) {
    val infinite = rememberInfiniteTransition(label = "badge_$label")
    val dotAlpha by infinite.animateFloat(
        initialValue = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "dot_alpha"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(PlexusColors.PurplePrimary.copy(alpha = 0.06f))
            .border(1.dp, PlexusColors.PurplePrimary.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .alpha(dotAlpha)
                .background(dotColor, CircleShape)
        )
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            color = PlexusColors.TextWhite.copy(alpha = 0.75f)
        )
    }
}

// ─── Logo ─────────────────────────────────────────────
@Composable
fun PlexusLogo(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    val infinite = rememberInfiniteTransition(label = "logo_glow")
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOut), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Canvas(
            modifier = Modifier
                .size(size * 1.4f)
                .alpha(glowAlpha * 0.4f)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF7B5CF0).copy(alpha = 0.3f),
                        Color(0xFF00C8F0).copy(alpha = 0.1f),
                        Color.Transparent
                    )
                ),
                radius = this.size.minDimension / 2
            )
        }

        // X mark
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = w * 0.13f

            val gradient = Brush.linearGradient(
                colors = listOf(Color(0xFF7B5CF0), Color(0xFF00C8F0)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
            val gradientReverse = Brush.linearGradient(
                colors = listOf(Color(0xFF00C8F0), Color(0xFF7B5CF0)),
                start = Offset(w, 0f),
                end = Offset(0f, h)
            )

            val leftPath = Path().apply {
                moveTo(w * 0.08f, h * 0.08f)
                cubicTo(w * 0.35f, h * 0.08f, w * 0.35f, h * 0.5f, w * 0.08f, h * 0.92f)
            }
            drawPath(leftPath, gradient, style = Stroke(strokeW, cap = StrokeCap.Round))

            val rightPath = Path().apply {
                moveTo(w * 0.92f, h * 0.08f)
                cubicTo(w * 0.65f, h * 0.08f, w * 0.65f, h * 0.5f, w * 0.92f, h * 0.92f)
            }
            drawPath(rightPath, gradientReverse, style = Stroke(strokeW, cap = StrokeCap.Round))

            val innerLeftPath = Path().apply {
                moveTo(w * 0.28f, h * 0.15f)
                cubicTo(w * 0.52f, h * 0.15f, w * 0.52f, h * 0.5f, w * 0.28f, h * 0.85f)
            }
            drawPath(innerLeftPath, gradient, style = Stroke(strokeW, cap = StrokeCap.Round))

            val innerRightPath = Path().apply {
                moveTo(w * 0.72f, h * 0.15f)
                cubicTo(w * 0.48f, h * 0.15f, w * 0.48f, h * 0.5f, w * 0.72f, h * 0.85f)
            }
            drawPath(innerRightPath, gradientReverse, style = Stroke(strokeW, cap = StrokeCap.Round))
        }
    }
}