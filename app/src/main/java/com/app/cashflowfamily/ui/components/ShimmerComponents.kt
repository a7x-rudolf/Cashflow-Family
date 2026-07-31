package com.app.cashflowfamily.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer

@Composable
fun HomeShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .shimmer()
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Balance Card Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Quick Action Row Shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(4) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Wave Card Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Transaction List Header
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Transaction Items Shimmer
        repeat(5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    )
                }
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                )
            }
        }
    }
}

@Composable
fun AnalyticsShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .shimmer(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )
        
        // Summary Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )
        
        // Two Summary Cards
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )
        }
        
        // Chart Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )
        
        // Insights
        repeat(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )
        }
    }
}
