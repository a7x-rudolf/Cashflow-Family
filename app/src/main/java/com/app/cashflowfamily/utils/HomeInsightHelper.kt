package com.app.cashflowfamily.utils

import com.app.cashflowfamily.data.model.Transaction
import com.app.cashflowfamily.viewmodel.Insight
import com.app.cashflowfamily.viewmodel.InsightType
import com.app.cashflowfamily.viewmodel.MonthData
import java.util.Calendar

/**
 * Helper murni (tanpa side-effect/fetch) untuk menyiapkan data yang dipakai
 * HomeInsightWaveCard di halaman Beranda. Sengaja dipisah dari HomeViewModel
 * supaya tidak perlu mengubah state/fetch logic yang sudah ada -- semua
 * dihitung dari MonthData yang sudah tersedia di HomeUiState.
 */
object HomeInsightHelper {

    /**
     * Total pemasukan & pengeluaran per hari dalam 1 bulan (index 0 = tanggal 1).
     * Dipakai untuk wave chart 2-garis yang otomatis mengikuti bulan yang
     * sedang aktif di BalanceCardPager.
     */
    fun dailyIncomeExpenseSeries(monthData: MonthData): Pair<List<Double>, List<Double>> {
        val daysInMonth = Calendar.getInstance().apply {
            timeInMillis = monthData.monthTimestamp
        }.getActualMaximum(Calendar.DAY_OF_MONTH)

        val income = DoubleArray(daysInMonth)
        val expense = DoubleArray(daysInMonth)

        for (transaction in monthData.transactions) {
            val day = Calendar.getInstance().apply {
                timeInMillis = transaction.date
            }.get(Calendar.DAY_OF_MONTH) - 1

            if (day !in 0 until daysInMonth) continue

            if (transaction.type == "income") {
                income[day] += transaction.amount
            } else if (transaction.type == "expense") {
                expense[day] += transaction.amount
            }
        }

        return income.toList() to expense.toList()
    }

    /**
     * Insight utama (satu) untuk bulan yang sedang aktif -- versi ringkas
     * dari logic status saldo di AnalyticsViewModel.generateInsights,
     * supaya pesannya konsisten temanya antara Beranda dan halaman Analytics.
     */
    fun primaryInsight(monthData: MonthData): Insight {
        if (monthData.transactions.isEmpty()) {
            return Insight(
                title = "Belum Ada Data",
                description = "Belum ada transaksi di bulan ini. Mulai catat untuk mendapatkan insight.",
                type = InsightType.INFO
            )
        }

        val balance = monthData.balance
        val income = monthData.income

        return when {
            balance > 0 -> {
                val ratio = if (income > 0) (balance / income * 100).toInt() else 0
                Insight(
                    title = "Keuangan Sehat",
                    description = "Anda berhasil menyisihkan $ratio% dari pemasukan bulan ini.",
                    type = InsightType.POSITIVE
                )
            }
            balance < 0 -> Insight(
                title = "Pengeluaran Melebihi Pemasukan",
                description = "Pengeluaran Anda melebihi pemasukan bulan ini. Perlu evaluasi budget.",
                type = InsightType.WARNING
            )
            else -> Insight(
                title = "Keseimbangan",
                description = "Pemasukan dan pengeluaran seimbang bulan ini.",
                type = InsightType.INFO
            )
        }
    }
}
