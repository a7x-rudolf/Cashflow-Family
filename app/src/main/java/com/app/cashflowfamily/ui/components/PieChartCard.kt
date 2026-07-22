package com.app.cashflowfamily.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.PieChart
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
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.DonutPieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.app.cashflowfamily.utils.CurrencyFormatter
import com.app.cashflowfamily.viewmodel.CategoryData

// Palette warna untuk chart
val ChartColors = listOf(
    Color(0xFF2E7D32), // Hijau
    Color(0xFF1565C0), // Biru
    Color(0xFFE65100), // Orange
    Color(0xFF6A1B9A), // Ungu
    Color(0xFFC62828), // Merah
    Color(0xFF00838F), // Cyan
    Color(0xFFFF6F00), // Amber
    Color(0xFF4E342E), // Coklat
    Color(0xFF37474F), // Blue Grey
    Color(0xFFAD1457)  // Pink
)

@Composable
fun PieChartCard(
    title: String,
    categoryData: List<CategoryData>,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (categoryData.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                // Chart
                PieChartContent(
                    categoryData = categoryData,
                    totalAmount = totalAmount
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Legend
                CategoryLegend(
                    categoryData = categoryData,
                    totalAmount = totalAmount
                )
            }
        }
    }
}

@Composable
private fun PieChartContent(
    categoryData: List<CategoryData>,
    totalAmount: Double
) {
    val pieSlices = categoryData.mapIndexed { index, data ->
        PieChartData.Slice(
            label = data.category,
            value = data.amount.toFloat(),
            color = ChartColors[index % ChartColors.size]
        )
    }

    val pieChartData = PieChartData(
        slices = pieSlices,
        plotType = PlotType.Donut
    )

    val pieChartConfig = PieChartConfig(
        strokeWidth = 40f,
        activeSliceAlpha = 0.9f,
        isAnimationEnable = true,
        animationDuration = 800,
        backgroundColor = MaterialTheme.colorScheme.surface,
        chartPadding = 30,
        labelVisible = false,
        showSliceLabels = false
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        DonutPieChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            pieChartData = pieChartData,
            pieChartConfig = pieChartConfig
        )

        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = CurrencyFormatter.formatRupiah(totalAmount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CategoryLegend(
    categoryData: List<CategoryData>,
    totalAmount: Double
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categoryData.forEachIndexed { index, data ->
            LegendItem(
                color = ChartColors[index % ChartColors.size],
                category = data.category,
                amount = data.amount,
                percentage = data.percentage
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    category: String,
    amount: Double,
    percentage: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = CurrencyFormatter.formatRupiah(amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.width(36.dp)
        )
    }
}