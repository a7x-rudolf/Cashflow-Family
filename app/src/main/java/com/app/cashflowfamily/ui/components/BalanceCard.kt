package com.app.cashflowfamily.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cashflowfamily.ui.theme.BlueSecondary
import com.app.cashflowfamily.ui.theme.BlueSecondaryDark
import com.app.cashflowfamily.ui.theme.BlueSecondaryLight
import com.app.cashflowfamily.ui.theme.GreenPrimary
import com.app.cashflowfamily.ui.theme.GreenPrimaryDark
import com.app.cashflowfamily.utils.CurrencyFormatter
import com.app.cashflowfamily.utils.DateFormatter

@Composable
fun BalanceCard(
    monthTimestamp: Long,
    balance: Double,
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier
) {
    val isCurrentMonth = DateFormatter.isCurrentMonth(monthTimestamp)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GreenPrimaryDark,
                            GreenPrimary,
                            BlueSecondary
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1200f, 1200f)
                    )
                )
        ) {
            // Dekorasi garis abstrak + glow di belakang konten
            AbstractWaveDecoration(modifier = Modifier.matchParentSize())

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isCurrentMonth) "Saldo Bulan Ini" else "Saldo",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Text(
                            text = DateFormatter.formatMonthYear(monthTimestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = CurrencyFormatter.formatRupiah(balance),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IncomeExpenseItem(
                        icon = Icons.Filled.ArrowDownward,
                        label = "Pemasukan",
                        amount = income,
                        iconColor = Color(0xFF81C784),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    IncomeExpenseItem(
                        icon = Icons.Filled.ArrowUpward,
                        label = "Pengeluaran",
                        amount = expense,
                        iconColor = Color(0xFFEF9A9A),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Dekorasi visual berupa garis-garis wave abstrak yang tipis dan transparan,
 * plus dua "glow" blob halus di pojok, supaya card terasa lebih premium
 * tanpa mengganggu keterbacaan konten di atasnya.
 */
@Composable
private fun AbstractWaveDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Glow blob pojok kanan atas
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    BlueSecondaryLight.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                center = Offset(w * 0.92f, h * 0.05f),
                radius = w * 0.55f
            ),
            radius = w * 0.55f,
            center = Offset(w * 0.92f, h * 0.05f)
        )

        // Glow blob pojok kiri bawah
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    BlueSecondaryDark.copy(alpha = 0.30f),
                    Color.Transparent
                ),
                center = Offset(w * 0.02f, h * 1.05f),
                radius = w * 0.6f
            ),
            radius = w * 0.6f,
            center = Offset(w * 0.02f, h * 1.05f)
        )

        // Garis-garis wave abstrak, tipis & semi-transparan
        val waveConfigs = listOf(
            Triple(h * 0.30f, h * 0.10f, 0.16f),
            Triple(h * 0.55f, h * 0.14f, 0.12f),
            Triple(h * 0.80f, h * 0.09f, 0.09f)
        )

        waveConfigs.forEach { (baseY, amplitude, alpha) ->
            val path = Path().apply {
                moveTo(-w * 0.1f, baseY)
                cubicTo(
                    w * 0.2f, baseY - amplitude,
                    w * 0.35f, baseY + amplitude,
                    w * 0.6f, baseY
                )
                cubicTo(
                    w * 0.8f, baseY - amplitude * 0.8f,
                    w * 0.95f, baseY + amplitude * 0.6f,
                    w * 1.15f, baseY - amplitude * 0.3f
                )
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = alpha),
                style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun IncomeExpenseItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    amount: Double,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )

            Text(
                text = CurrencyFormatter.formatRupiah(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}
