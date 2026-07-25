package com.app.cashflowfamily.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Chart garis mini bergaya "wave" (area chart dengan kurva halus),
 * dipakai untuk menampilkan tren singkat (mis. saldo beberapa bulan terakhir)
 * dalam ruang yang kecil/compact.
 */
@Composable
fun MiniWaveChart(
    values: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (values.size < 2) return@Canvas

        val minVal = values.min()
        val maxVal = values.max()
        val range = (maxVal - minVal).takeIf { it != 0.0 } ?: 1.0

        val verticalPadding = 8.dp.toPx()
        val chartHeight = size.height - (verticalPadding * 2)
        val stepX = size.width / (values.size - 1)

        val points = values.mapIndexed { index, value ->
            val normalized = ((value - minVal) / range).toFloat()
            val x = index * stepX
            val y = verticalPadding + chartHeight - (normalized * chartHeight)
            Offset(x, y)
        }

        // Kurva halus melalui titik tengah antar poin (smooth bezier)
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val midX = (p0.x + p1.x) / 2f
                cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
            }
        }

        // Area di bawah garis dengan gradient memudar (efek wave)
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
                    lineColor.copy(alpha = 0.32f),
                    lineColor.copy(alpha = 0.02f)
                ),
                startY = 0f,
                endY = size.height
            )
        )

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
        )

        // Tonjolkan titik terakhir (bulan berjalan)
        drawCircle(
            color = lineColor.copy(alpha = 0.20f),
            radius = 8.dp.toPx(),
            center = points.last()
        )
        drawCircle(
            color = lineColor,
            radius = 4.dp.toPx(),
            center = points.last()
        )
    }
}
