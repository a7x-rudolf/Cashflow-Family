package com.app.cashflowfamily.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.cashflowfamily.ui.theme.BalanceCardGradientEnd
import com.app.cashflowfamily.ui.theme.BalanceCardGradientMid
import com.app.cashflowfamily.ui.theme.BalanceCardGradientStart
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

    // Ukuran font saldo mengecil otomatis kalau angka kepanjangan untuk
    // layar sempit, supaya tidak overflow/kepotong (nominal besar +
    // device kecil = rawan clipping kalau font-nya fixed).
    var balanceFontSize by remember(balance) { mutableFloatStateOf(32f) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        0.0f to BalanceCardGradientStart,
                        0.5f to BalanceCardGradientMid,
                        1.0f to BalanceCardGradientEnd,
                    )
                )
        ) {
            // Refined abstract shapes
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    radius = w * 0.4f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.9f, h * 0.1f)
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = w * 0.3f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.9f)
                )
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isCurrentMonth) "Saldo Bulan Ini" else "Saldo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Text(
                            text = DateFormatter.formatMonthYear(monthTimestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = CurrencyFormatter.formatRupiah(balance),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = balanceFontSize.sp,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    onTextLayout = { result ->
                        if (result.hasVisualOverflow && balanceFontSize > 22f) {
                            balanceFontSize -= 2f
                        }
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IncomeExpenseItem(
                        icon = Icons.Filled.ArrowDownward,
                        label = "Income",
                        amount = income,
                        iconColor = Color(0xFF69F0AE),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    IncomeExpenseItem(
                        icon = Icons.Filled.ArrowUpward,
                        label = "Expense",
                        amount = expense,
                        iconColor = Color(0xFFFF8A80),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
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
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )

            Text(
                text = CurrencyFormatter.formatRupiah(amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
