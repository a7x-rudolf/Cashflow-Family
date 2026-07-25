package com.app.cashflowfamily.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.cashflowfamily.utils.CurrencyFormatter
import com.app.cashflowfamily.viewmodel.InsightType

/**
 * Card ringkas untuk halaman Beranda: menampilkan tren pemasukan vs
 * pengeluaran harian pada bulan yang sedang aktif di card saldo (2 garis,
 * gaya wave/heartbeat) + satu insight utama.
 * Sengaja dibuat compact (bukan versi lengkap seperti di halaman Analytics).
 * Otomatis mengikuti bulan yang dipilih di BalanceCardPager -- caller cukup
 * mengoper data harian bulan yang sedang aktif.
 */
@Composable
fun HomeInsightWaveCard(
    dailyIncome: List<Double>,
    dailyExpense: List<Double>,
    insightTitle: String,
    insightDescription: String,
    insightType: InsightType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val incomeColor = Color(0xFF81C784)   // konsisten dengan ikon "Pemasukan" di BalanceCard
    val expenseColor = Color(0xFFE57373)  // konsisten dengan ikon "Pengeluaran" di BalanceCard
    val (icon, accentColor) = when (insightType) {
        InsightType.POSITIVE -> Icons.Filled.CheckCircle to Color(0xFF2ECC71)
        InsightType.NEGATIVE -> Icons.AutoMirrored.Filled.TrendingDown to Color(0xFFE53935)
        InsightType.WARNING -> Icons.Filled.Warning to Color(0xFFE65100)
        InsightType.INFO -> Icons.Filled.Info to MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Insights,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Insight Keuangan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Lihat detail",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Wave chart mini dual-line (pemasukan vs pengeluaran, harian)
            val hasEnoughData = dailyIncome.size >= 2 && dailyExpense.size == dailyIncome.size
            if (hasEnoughData) {
                DualMiniWaveChart(
                    valuesA = dailyIncome,
                    valuesB = dailyExpense,
                    colorA = incomeColor,
                    colorB = expenseColor,
                    height = 64.dp,
                    endLabel = CurrencyFormatter.formatNumber(dailyIncome.last())
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LegendDot(color = incomeColor, label = "Pemasukan")
                    Spacer(modifier = Modifier.width(14.dp))
                    LegendDot(color = expenseColor, label = "Pengeluaran")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Insight utama
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = insightTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = insightDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
        )
    }
}
