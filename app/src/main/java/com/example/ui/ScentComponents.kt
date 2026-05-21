package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

// Section 13 custom dial meter: visualizes Deal Score
@Composable
fun DealScoreGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "score"
    )

    val (color, label) = when (score) {
        in 90..100 -> ScentExceptional to "WYJĄTKOWA OKAZJA"
        in 75..89 -> ScentVeryGood to "BARDZO DOBRA OKAZJA"
        in 60..74 -> ScentGood to "DOBRA OFERTA"
        else -> ScentRegular to "ZWYKŁA PROMOCJA"
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val strokeWidth = 14.dp.toPx()
            val sizeMin = size.minDimension
            val radius = (sizeMin - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Track background
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Active Arc
            val sweepAngle = (animatedScore / 100f) * 360f
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Neon Glow underlay
            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth + 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "${score}/100",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontFamily = FontFamily.Serif
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "DEAL SCORE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = NeutralMuted,
                    letterSpacing = 1.2.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

// Custom glow curves - draws history of prices (Section 11 / 18.2)
@Composable
fun PriceHistoryGraph(
    prices: List<Float>,
    modifier: Modifier = Modifier
) {
    if (prices.isEmpty()) {
        Box(
            modifier = modifier.background(LightVelvet, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Brak danych historycznych", color = NeutralMuted)
        }
        return
    }

    val minVal = (prices.minOrNull() ?: 0f) * 0.9f
    val maxVal = (prices.maxOrNull() ?: 100f) * 1.1f
    val diff = if (maxVal - minVal == 0f) 1f else maxVal - minVal

    Box(
        modifier = modifier
            .background(DarkVelvet, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "HISTORIA CENY (30 DNI)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = RoseGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "Aktualnie: ${prices.last().toInt()} PLN",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = ScentVeryGood,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val graphHeight = size.height
                val graphWidth = size.width
                val pointsCount = prices.size

                val stepX = if (pointsCount > 1) graphWidth / (pointsCount - 1) else graphWidth

                val coordinates = prices.mapIndexed { idx, p ->
                    val x = idx * stepX
                    val y = graphHeight - (((p - minVal) / diff) * graphHeight)
                    Offset(x, y)
                }

                // Smooth Path build
                val path = Path().apply {
                    if (coordinates.isNotEmpty()) {
                        moveTo(coordinates[0].x, coordinates[0].y)
                        for (i in 1 until coordinates.size) {
                            val from = coordinates[i - 1]
                            val to = coordinates[i]
                            val conX = (from.x + to.x) / 2f
                            cubicTo(conX, from.y, conX, to.y, to.x, to.y)
                        }
                    }
                }

                // Gradient fill under the graph
                val fillPath = Path().apply {
                    addPath(path)
                    if (coordinates.isNotEmpty()) {
                        lineTo(coordinates.last().x, graphHeight)
                        lineTo(coordinates[0].x, graphHeight)
                        close()
                    }
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            RoseGold.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )

                // Neon Golden outline
                drawPath(
                    path = path,
                    color = RoseGold,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw bullet nodes
                coordinates.forEachIndexed { index, offset ->
                    val color = if (index == coordinates.lastIndex) ScentVeryGood else RoseGold
                    drawCircle(
                        color = Color.Black,
                        radius = 5.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = color,
                        radius = 3.5.dp.toPx(),
                        center = offset
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Graph X Axis markers representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("30 dni temu", style = MaterialTheme.typography.bodySmall, color = NeutralMuted)
                Text("15 dni temu", style = MaterialTheme.typography.bodySmall, color = NeutralMuted)
                Text("Dzisiaj", style = MaterialTheme.typography.bodySmall, color = NeutralMuted)
            }
        }
    }
}

// Custom Glass Perfume Bottle Vector Canvas representation to bypass empty Unsplash images
@Composable
fun PerfumeBottleIllustration(
    brand: String,
    modifier: Modifier = Modifier
) {
    val bottleColor = when (brand.lowercase()) {
        "creed" -> Brush.verticalGradient(listOf(Color(0xFFE5E5E5), Color(0xFF1C1A17)))
        "dior" -> Brush.verticalGradient(listOf(Color(0xFF2C3E50), Color(0xFF0F171E)))
        "tom ford" -> Brush.verticalGradient(listOf(Color(0xFF800020), Color(0xFF2D142C))) // Deep cherry
        "chanel" -> Brush.verticalGradient(listOf(Color(0xFFFFDF7A), Color(0xFFC5A059))) // gold
        "armani" -> Brush.verticalGradient(listOf(Color(0xFFE8F1F5), Color(0xFFAFBEC6)))
        else -> Brush.verticalGradient(listOf(RoseGold, MatteCharcoal))
    }

    Box(
        modifier = modifier
            .background(LightVelvet, RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(100.dp, 120.dp)) {
            val width = size.width
            val height = size.height

            // 1. Cap of the bottle
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(Color(0xFFD4AF37), Color(0xFFF3E5AB), Color(0xFFC5A059))),
                size = Size(width * 0.4f, height * 0.15f),
                topLeft = Offset(width * 0.3f, 4f),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // 2. Neck
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = Size(width * 0.28f, height * 0.05f),
                topLeft = Offset(width * 0.36f, height * 0.15f)
            )

            // 3. Main Glass Body
            val bodyHeight = height * 0.7f
            drawRoundRect(
                brush = bottleColor,
                size = Size(width, bodyHeight),
                topLeft = Offset(0f, height * 0.22f),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )

            // High Glass reflection outline (luxury glow vibe)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.15f),
                size = Size(width * 0.88f, bodyHeight * 0.9f),
                topLeft = Offset(width * 0.06f, height * 0.26f),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )

            // 4. Elegant Minimalist Label in center
            drawRoundRect(
                color = Color.White.copy(alpha = 0.92f),
                size = Size(width * 0.7f, bodyHeight * 0.4f),
                topLeft = Offset(width * 0.15f, height * 0.4f),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Dynamic symbol inside label
            val symbolColor = when (brand.lowercase()) {
                "creed" -> Color.Black
                "dior" -> Color(0xFF1E293B)
                "tom ford" -> Color(0xFF58111A)
                "chanel" -> Color(0xFFC5A059)
                else -> Color.Black
            }

            drawCircle(
                color = symbolColor,
                radius = 6.dp.toPx(),
                center = Offset(width * 0.5f, height * 0.6f)
            )
        }

        // Mini Brand textual badge overlaid
        Text(
            text = brand.uppercase(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        )
    }
}
