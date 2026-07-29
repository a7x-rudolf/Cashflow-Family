package com.app.cashflowfamily.ui.components

import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Chart garis mini bergaya "trading/stock ticker" -- kurva mulus + area
 * gradient + garis grid tipis + label nilai terakhir mengambang di ujung
 * kanan (seperti "harga saat ini" pada chart saham/trading), lengkap
 * dengan denyut halo animasi di titik terakhir supaya terasa "hidup".
 *
 * Kurva dibangun dengan Catmull-Rom -> Bezier (bukan midpoint-bezier
 * sederhana) supaya lekukan mengikuti arah titik sebelum & sesudahnya
 * secara halus, tanpa kink/patahan di tiap titik data.
 */
@Composable
fun MiniWaveChart(
    values: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    endLabel: String? = null
) {
    val pulse by rememberInfiniteTransition(label = "wave_pulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_pulse_value"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (values.size < 2) return@Canvas

        val minVal = values.min()
        val maxVal = values.max()
        val range = (maxVal - minVal).takeIf { it != 0.0 } ?: 1.0

        val verticalPadding = 10.dp.toPx()
        val chartHeight = size.height - (verticalPadding * 2)
        val stepX = size.width / (values.size - 1)

        val points = values.mapIndexed { index, value ->
            val normalized = ((value - minVal) / range).toFloat()
            val x = index * stepX
            val y = verticalPadding + chartHeight - (normalized * chartHeight)
            Offset(x, y)
        }

        drawGridLines(lineColor)

        val linePath = smoothPath(points)

        val areaPath = Path().apply {
            addPath(linePath)
            lineTo(points.last().x, size.height)
            lineTo(points.first().x, size.height)
            close()
        }

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.30f),
                    lineColor.copy(alpha = 0.0f)
                ),
                startY = 0f,
                endY = size.height
            )
        )

        val strokeBrush = Brush.horizontalGradient(
            colors = listOf(
                lineColor.copy(alpha = 0.55f),
                lineColor
            )
        )

        // Glow tipis di belakang garis utama -- simulasi soft-glow tanpa blur/render effect.
        drawPath(
            path = linePath,
            color = lineColor.copy(alpha = 0.18f),
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = linePath,
            brush = strokeBrush,
            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        drawPulsingEndpoint(points.last(), lineColor, pulse)

        if (endLabel != null) {
            drawValueBadge(points.last(), endLabel, lineColor)
        }
    }
}

/**
 * Wave chart mini dengan 2 garis sekaligus (mis. pemasukan vs pengeluaran)
 * dalam satu skala sumbu-Y yang sama -- dipakai di card insight Beranda
 * supaya tren dua nilai bisa dibandingkan langsung. Garis utama (A) diberi
 * gaya "trading chart": area gradient, grid tipis, glow, titik akhir
 * berdenyut, dan label nilai mengambang; garis pembanding (B) dibuat lebih
 * tipis/redup di lapisan bawah supaya A tetap jadi fokus visual.
 */
@Composable
fun DualMiniWaveChart(
    valuesA: List<Double>,
    valuesB: List<Double>,
    colorA: Color,
    colorB: Color,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    endLabel: String? = null
) {
    val pulse by rememberInfiniteTransition(label = "dual_wave_pulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dual_wave_pulse_value"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (valuesA.size < 2 || valuesB.size != valuesA.size) return@Canvas

        // Satu skala bersama untuk kedua garis, supaya perbandingan besar-kecilnya valid.
        val combined = valuesA + valuesB
        val minVal = combined.min()
        val maxVal = combined.max()
        val range = (maxVal - minVal).takeIf { it != 0.0 } ?: 1.0

        val verticalPadding = 8.dp.toPx()
        val chartHeight = size.height - (verticalPadding * 2)
        val stepX = size.width / (valuesA.size - 1)

        fun toPoints(values: List<Double>): List<Offset> =
            values.mapIndexed { index, value ->
                val normalized = ((value - minVal) / range).toFloat()
                val x = index * stepX
                val y = verticalPadding + chartHeight - (normalized * chartHeight)
                Offset(x, y)
            }

        val pointsA = toPoints(valuesA)
        val pointsB = toPoints(valuesB)
        val pathA = smoothPath(pointsA)
        val pathB = smoothPath(pointsB)

        drawGridLines(colorA)

        // Garis B (pembanding, mis. pengeluaran): tipis & redup, di lapisan bawah.
        drawPath(
            path = pathB,
            color = colorB.copy(alpha = 0.55f),
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Area gradient di bawah garis utama (A, mis. pemasukan) -- lebih tegas
        // di dekat garis, memudar cepat ke bawah, khas tampilan chart trading.
        val areaPathA = Path().apply {
            addPath(pathA)
            lineTo(pointsA.last().x, size.height)
            lineTo(pointsA.first().x, size.height)
            close()
        }
        drawPath(
            path = areaPathA,
            brush = Brush.verticalGradient(
                colors = listOf(
                    colorA.copy(alpha = 0.28f),
                    colorA.copy(alpha = 0.04f),
                    colorA.copy(alpha = 0.0f)
                ),
                startY = 0f,
                endY = size.height
            )
        )

        val strokeBrushA = Brush.horizontalGradient(
            colors = listOf(
                colorA.copy(alpha = 0.55f),
                colorA
            )
        )

        // Glow lembut + garis utama A di lapisan paling atas -- jadi fokus mata.
        drawPath(
            path = pathA,
            color = colorA.copy(alpha = 0.16f),
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = pathA,
            brush = strokeBrushA,
            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        drawPulsingEndpoint(pointsA.last(), colorA, pulse)

        if (endLabel != null) {
            drawValueBadge(pointsA.last(), endLabel, colorA)
        }
    }
}

/** Grid horizontal tipis (3 garis putus-putus) -- referensi visual khas chart trading. */
private fun DrawScope.drawGridLines(tintColor: Color) {
    val rows = 3
    for (i in 1 until rows) {
        val y = size.height * i / rows
        drawLine(
            color = tintColor.copy(alpha = 0.05f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 5.dp.toPx()))
        )
    }
}

/** Titik akhir dengan halo yang berdenyut pelan -- kesan data "live". */
private fun DrawScope.drawPulsingEndpoint(center: Offset, color: Color, pulse: Float) {
    val pulseRadius = (7.dp.toPx()) + (pulse * 7.dp.toPx())
    val pulseAlpha = (1f - pulse) * 0.25f

    drawCircle(color = color.copy(alpha = pulseAlpha), radius = pulseRadius, center = center)
    drawCircle(color = color.copy(alpha = 0.20f), radius = 6.dp.toPx(), center = center)
    drawCircle(color = color, radius = 3.2.dp.toPx(), center = center)
}

/**
 * Label nilai mengambang di dekat titik terakhir, gaya "harga saat ini"
 * pada chart trading -- kapsul kecil dengan background solid + teks putih.
 */
private fun DrawScope.drawValueBadge(anchor: Offset, text: String, accentColor: Color) {
    val textSizePx = 10.sp.toPx()
    val paddingH = 6.dp.toPx()
    val paddingV = 3.dp.toPx()

    val paint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        textSize = textSizePx
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    val textWidth = paint.measureText(text)
    val textHeight = paint.fontMetrics.let { it.descent - it.ascent }

    val badgeWidth = textWidth + paddingH * 2
    val badgeHeight = textHeight + paddingV * 2

    // Anchor di kanan-atas titik terakhir, tapi dijaga tidak keluar dari batas kanvas.
    var left = anchor.x - badgeWidth / 2
    left = left.coerceIn(0f, size.width - badgeWidth)
    val top = (anchor.y - badgeHeight - 10.dp.toPx()).coerceAtLeast(0f)

    val rect = RectF(left, top, left + badgeWidth, top + badgeHeight)
    val cornerRadius = badgeHeight / 2

    drawContext.canvas.nativeCanvas.apply {
        val bgPaint = Paint().apply {
            isAntiAlias = true
            color = accentColor.copy(alpha = 0.95f).toArgb()
        }
        drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

        val baseline = rect.centerY() - (paint.ascent() + paint.descent()) / 2
        drawText(text, rect.centerX(), baseline, paint.apply { textAlign = Paint.Align.CENTER })
    }
}

/**
 * Bangun path kurva halus dari sekumpulan titik memakai Catmull-Rom
 * spline yang dikonversi ke kubik Bezier. Berbeda dari midpoint-bezier
 * sederhana, tangent tiap segmen memperhitungkan titik sebelum & sesudahnya
 * sehingga kurva mengalir mulus tanpa kink -- terlihat lebih premium.
 */
private fun smoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points.first().x, points.first().y)
    if (points.size < 3) {
        if (points.size == 2) path.lineTo(points[1].x, points[1].y)
        return path
    }

    val n = points.size
    for (i in 0 until n - 1) {
        val p0 = points[if (i == 0) i else i - 1]
        val p1 = points[i]
        val p2 = points[i + 1]
        val p3 = points[if (i + 2 < n) i + 2 else i + 1]

        // Catmull-Rom -> Bezier control points (tension standar 1/6).
        val c1x = p1.x + (p2.x - p0.x) / 6f
        val c1y = p1.y + (p2.y - p0.y) / 6f
        val c2x = p2.x - (p3.x - p1.x) / 6f
        val c2y = p2.y - (p3.y - p1.y) / 6f

        path.cubicTo(c1x, c1y, c2x, c2y, p2.x, p2.y)
    }
    return path
}
