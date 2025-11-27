// app/src/main/java/com/cns/plantex/ui/components/CircularTimer.kt
package com.cns.plantex.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularTimer(
    remainingSeconds: Long,
    totalSeconds: Long,
    isConnected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    // Infinite rotation for the glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowRotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60

    Box(
        modifier = modifier
            .size((260 * pulseScale).dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, radius = 130.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background glow
        Canvas(
            modifier = Modifier.size(280.dp)
        ) {
            val strokeWidth = 4.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2

            rotate(glowRotation) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.3f),
                            tertiaryColor.copy(alpha = 0.1f),
                            primaryColor.copy(alpha = 0.3f)
                        )
                    ),
                    radius = radius + 20.dp.toPx(),
                    style = Stroke(width = 40.dp.toPx())
                )
            }
        }

        // Main timer circle
        Canvas(
            modifier = Modifier.size(240.dp)
        ) {
            val strokeWidth = 16.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

            // Background track
            drawArc(
                color = outlineColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc
            if (isConnected) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(primaryColor, tertiaryColor, primaryColor)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // End cap glow
                val angle = Math.toRadians((-90 + 360 * animatedProgress).toDouble())
                val endX = center.x + radius * cos(angle).toFloat()
                val endY = center.y + radius * sin(angle).toFloat()

                drawCircle(
                    color = primaryColor,
                    radius = strokeWidth / 2 + 4.dp.toPx(),
                    center = Offset(endX, endY),
                    alpha = 0.5f
                )
            }
        }

        // Inner circle with content
        Box(
            modifier = Modifier
                .size(200.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = primaryColor.copy(alpha = 0.3f)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            surfaceColor,
                            primaryContainerColor.copy(alpha = 0.3f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!isConnected) {
                    Text(
                        text = "Not Connected",
                        style = MaterialTheme.typography.titleMedium,
                        color = onSurfaceColor.copy(alpha = 0.5f)
                    )
                } else {
                    Text(
                        text = "Next Watering",
                        style = MaterialTheme.typography.labelLarge,
                        color = onSurfaceColor.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (hours > 0) {
                            Text(
                                text = String.format("%d", hours),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = primaryColor
                            )
                            Text(
                                text = "h ",
                                style = MaterialTheme.typography.titleLarge,
                                color = onSurfaceColor.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = String.format("%02d", minutes),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = primaryColor
                        )
                        Text(
                            text = "m ",
                            style = MaterialTheme.typography.titleLarge,
                            color = onSurfaceColor.copy(alpha = 0.7f)
                        )
                        Text(
                            text = String.format("%02d", seconds),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = onSurfaceColor.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "s",
                            style = MaterialTheme.typography.titleMedium,
                            color = onSurfaceColor.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap to adjust",
                        style = MaterialTheme.typography.labelSmall,
                        color = tertiaryColor
                    )
                }
            }
        }
    }
}