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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            BalanceCardGradientStart,
                            BalanceCardGradientMid,
                            BalanceCardGradientEnd
                        )
                    )
                )
        ) {
            // Garis abstrak tipis di background -- kesan premium tapi clean,
            // sengaja low-opacity & jumlahnya sedikit supaya tidak norak.
            // PENTING: pakai matchParentSize(), bukan fillMaxSize(). Box ini
            // tidak punya ukuran eksplisit (tingginya mengikuti konten Column),
            // jadi fillMaxSize() mengukur Canvas terhadap constraint yang masuk
            // ke Box -- bisa berbeda jauh dari tinggi Card yang sebenarnya,
            // sehingga garis digambar di luar area yang terlihat. matchParentSize()
            // menunggu ukuran akhir Box (dari Column) baru menyamakan ukuran Canvas.
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                fun archPath(yStart: Float, amplitude: Float): Path = Path().apply {
                    moveTo(-w * 0.2f, yStart)
                    cubicTo(
                        w * 0.15f, yStart - amplitude,
                        w * 0.45f, yStart + amplitude,
                        w * 1.2f, yStart - amplitude * 0.6f
                    )
                }

                drawPath(
                    path = archPath(h * 0.25f, h * 0.18f),
                    color = Color.White.copy(alpha = 0.10f),
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = archPath(h * 0.55f, h * 0.22f),
                    color = Color.White.copy(alpha = 0.07f),
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = archPath(h * 0.85f, h * 0.15f),
                    color = Color.White.copy(alpha = 0.05f),
                    style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isCurrentMonth) "Saldo Bulan Ini" else "Saldo",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )

                        Text(
                            text = DateFormatter.formatMonthYear(monthTimestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = CurrencyFormatter.formatRupiah(balance),
                    style = TextStyle(
                        fontSize = balanceFontSize.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    onTextLayout = { result ->
                        if (result.hasVisualOverflow && balanceFontSize > 20f) {
                            balanceFontSize -= 2f
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                        iconColor = Color(0xFFE57373),
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
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
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
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )

            Text(
                text = CurrencyFormatter.formatRupiah(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}