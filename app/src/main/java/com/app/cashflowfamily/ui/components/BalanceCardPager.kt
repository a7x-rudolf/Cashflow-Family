package com.app.cashflowfamily.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.app.cashflowfamily.viewmodel.MonthData
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun BalanceCardPager(
    monthDataList: List<MonthData>,
    onPageChanged: (Int) -> Unit,  // Callback saat halaman berubah
    modifier: Modifier = Modifier
) {
    if (monthDataList.isEmpty()) return

    // Start di bulan ini (index terakhir)
    val pagerState = rememberPagerState(
        initialPage = monthDataList.size - 1,
        pageCount = { monthDataList.size }
    )

    // Listen perubahan halaman dan panggil callback
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                onPageChanged(page)
            }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) { page ->
            val data = monthDataList[page]
            BalanceCard(
                monthTimestamp = data.monthTimestamp,
                balance = data.balance,
                income = data.income,
                expense = data.expense
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        PageIndicator(
            pageCount = monthDataList.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    val visibleRange = 5
    val startIndex = maxOf(0, currentPage - visibleRange / 2)
    val endIndex = minOf(pageCount - 1, startIndex + visibleRange - 1)
    val adjustedStart = maxOf(0, endIndex - visibleRange + 1)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in adjustedStart..endIndex) {
            val isSelected = i == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (isSelected) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        }
                    )
            )
        }
    }
}