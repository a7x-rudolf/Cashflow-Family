package com.app.cashflowfamily.utils.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.app.cashflowfamily.data.model.Transaction
import com.app.cashflowfamily.utils.CurrencyFormatter
import com.app.cashflowfamily.utils.DateFormatter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt

object ExportHelper {

    // ===== EXPORT PDF =====
    fun exportToPdf(
        context: Context,
        familyName: String,
        monthTimestamp: Long,
        transactions: List<Transaction>,
        totalIncome: Double,
        totalExpense: Double
    ): File? {
        return try {
            val document = PdfDocument()

            // A4 size in points (595 x 842)
            val pageWidth = 595
            val pageHeight = 842
            val margin = 40f

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            var yPosition = margin + 20f

            // === HEADER ===
            val titlePaint = Paint().apply {
                color = "#5EC5F5".toColorInt()
                textSize = 24f
                isFakeBoldText = true
            }
            canvas.drawText("Cashflow Family", margin, yPosition, titlePaint)

            yPosition += 20f
            val subtitlePaint = Paint().apply {
                color = Color.GRAY
                textSize = 14f
            }
            canvas.drawText("Laporan Keuangan Keluarga", margin, yPosition, subtitlePaint)

            yPosition += 30f

            // Divider line
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }
            canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint)

            yPosition += 25f

            // === INFO SECTION ===
            val infoPaint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
            }
            val infoBoldPaint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
                isFakeBoldText = true
            }

            canvas.drawText("Keluarga:", margin, yPosition, infoPaint)
            canvas.drawText(familyName, margin + 100f, yPosition, infoBoldPaint)

            yPosition += 18f
            canvas.drawText("Periode:", margin, yPosition, infoPaint)
            canvas.drawText(DateFormatter.formatMonthYear(monthTimestamp), margin + 100f, yPosition, infoBoldPaint)

            yPosition += 18f
            canvas.drawText("Dibuat pada:", margin, yPosition, infoPaint)
            val currentDate = SimpleDateFormat("d MMM yyyy, HH:mm", Locale("in", "ID")).format(Date())
            canvas.drawText(currentDate, margin + 100f, yPosition, infoBoldPaint)

            yPosition += 30f

            // === RINGKASAN ===
            val sectionPaint = Paint().apply {
                color = "#5EC5F5".toColorInt()
                textSize = 14f
                isFakeBoldText = true
            }
            canvas.drawText("RINGKASAN", margin, yPosition, sectionPaint)

            yPosition += 20f

            val balance = totalIncome - totalExpense

            // Box ringkasan
            val boxPaint = Paint().apply {
                color = "#F5F5F5".toColorInt()
                style = Paint.Style.FILL
            }
            canvas.drawRect(margin, yPosition, pageWidth - margin, yPosition + 90f, boxPaint)

            yPosition += 20f

            canvas.drawText("Total Pemasukan:", margin + 15f, yPosition, infoPaint)
            val incomeColor = Paint().apply {
                color = "#2E7D32".toColorInt()
                textSize = 11f
                isFakeBoldText = true
            }
            canvas.drawText(
                CurrencyFormatter.formatRupiah(totalIncome),
                pageWidth - margin - 15f - incomeColor.measureText(CurrencyFormatter.formatRupiah(totalIncome)),
                yPosition,
                incomeColor
            )

            yPosition += 20f
            canvas.drawText("Total Pengeluaran:", margin + 15f, yPosition, infoPaint)
            val expenseColor = Paint().apply {
                color = "#C62828".toColorInt()
                textSize = 11f
                isFakeBoldText = true
            }
            canvas.drawText(
                CurrencyFormatter.formatRupiah(totalExpense),
                pageWidth - margin - 15f - expenseColor.measureText(CurrencyFormatter.formatRupiah(totalExpense)),
                yPosition,
                expenseColor
            )

            yPosition += 20f
            canvas.drawText("Saldo Bulan Ini:", margin + 15f, yPosition, infoBoldPaint)
            val balanceColor = Paint().apply {
                color = if (balance >= 0) "#2E7D32".toColorInt() else "#C62828".toColorInt()
                textSize = 12f
                isFakeBoldText = true
            }
            canvas.drawText(
                CurrencyFormatter.formatRupiah(balance),
                pageWidth - margin - 15f - balanceColor.measureText(CurrencyFormatter.formatRupiah(balance)),
                yPosition,
                balanceColor
            )

            yPosition += 35f

            // === DETAIL TRANSAKSI ===
            canvas.drawText("DETAIL TRANSAKSI (${transactions.size} transaksi)", margin, yPosition, sectionPaint)

            yPosition += 20f

            // Table Header
            val tableHeaderBg = Paint().apply {
                color = "#5EC5F5".toColorInt()
                style = Paint.Style.FILL
            }
            canvas.drawRect(margin, yPosition - 12f, pageWidth - margin, yPosition + 8f, tableHeaderBg)

            val tableHeaderPaint = Paint().apply {
                color = Color.WHITE
                textSize = 10f
                isFakeBoldText = true
            }

            // Column positions
            val colTanggal = margin + 5f
            val colKategori = margin + 90f
            val colDeskripsi = margin + 200f
            val colOleh = margin + 330f
            val colJumlah = pageWidth - margin - 5f

            canvas.drawText("Tanggal", colTanggal, yPosition, tableHeaderPaint)
            canvas.drawText("Kategori", colKategori, yPosition, tableHeaderPaint)
            canvas.drawText("Deskripsi", colDeskripsi, yPosition, tableHeaderPaint)
            canvas.drawText("Oleh", colOleh, yPosition, tableHeaderPaint)
            val jumlahHeaderWidth = tableHeaderPaint.measureText("Jumlah")
            canvas.drawText("Jumlah", colJumlah - jumlahHeaderWidth, yPosition, tableHeaderPaint)

            yPosition += 18f

            // Table Rows
            val rowPaint = Paint().apply {
                color = Color.BLACK
                textSize = 9f
            }

            transactions.forEachIndexed { index, transaction ->
                // Check if you need new page
                if (yPosition > pageHeight - margin - 40f) {
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = margin + 20f
                }

                // Alternate row background
                if (index % 2 == 0) {
                    val altBg = Paint().apply {
                        color = "#FAFAFA".toColorInt()
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(margin, yPosition - 10f, pageWidth - margin, yPosition + 5f, altBg)
                }

                // Data
                canvas.drawText(
                    DateFormatter.formatShortDate(transaction.date),
                    colTanggal,
                    yPosition,
                    rowPaint
                )

                val categoryText = if (transaction.category.length > 15)
                    transaction.category.take(13) + ".."
                else transaction.category
                canvas.drawText(categoryText, colKategori, yPosition, rowPaint)

                val descText = if (transaction.description.isBlank()) "-"
                else if (transaction.description.length > 20) transaction.description.take(18) + ".."
                else transaction.description
                canvas.drawText(descText, colDeskripsi, yPosition, rowPaint)

                val userText = if (transaction.userName.length > 8) transaction.userName.take(7) + ".."
                else transaction.userName
                canvas.drawText(userText, colOleh, yPosition, rowPaint)

                // Amount dengan warna
                val amountText = (if (transaction.type == "income") "+" else "-") +
                        CurrencyFormatter.formatRupiah(transaction.amount)
                val amountPaint = Paint().apply {
                    color = if (transaction.type == "income")
                        "#2E7D32".toColorInt()
                    else "#C62828".toColorInt()
                    textSize = 9f
                    isFakeBoldText = true
                }
                val amountWidth = amountPaint.measureText(amountText)
                canvas.drawText(amountText, colJumlah - amountWidth, yPosition, amountPaint)

                yPosition += 16f
            }

            // === FOOTER ===
            yPosition = pageHeight - margin
            val footerPaint = Paint().apply {
                color = Color.GRAY
                textSize = 9f
            }
            canvas.drawText(
                "Dibuat oleh Cashflow Family - Aplikasi Keuangan Keluarga",
                margin,
                yPosition,
                footerPaint
            )

            document.finishPage(page)

            // Save to file
            val exportDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val fileName = "CashflowFamily_${DateFormatter.formatMonthYear(monthTimestamp).replace(" ", "_")}.pdf"
            val file = File(exportDir, fileName)

            FileOutputStream(file).use { fos ->
                document.writeTo(fos)
            }

            document.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ===== EXPORT CSV =====
    fun exportToCsv(
        context: Context,
        familyName: String,
        monthTimestamp: Long,
        transactions: List<Transaction>
    ): File? {
        return try {
            val exportDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val fileName = "CashflowFamily_${DateFormatter.formatMonthYear(monthTimestamp).replace(" ", "_")}.csv"
            val file = File(exportDir, fileName)

            val sb = StringBuilder()

            // Header info (as comment rows)
            sb.append("Cashflow Family - Laporan Keuangan\n")
            sb.append("Keluarga,$familyName\n")
            sb.append("Periode,${DateFormatter.formatMonthYear(monthTimestamp)}\n")
            sb.append("Dibuat,${SimpleDateFormat("d MMM yyyy HH:mm", Locale("in", "ID")).format(Date())}\n")
            sb.append("Total Transaksi,${transactions.size}\n")
            sb.append("\n")

            // Column headers
            sb.append("Tanggal,Jenis,Kategori,Deskripsi,Dicatat Oleh,Jumlah (Rp)\n")

            // Data rows
            transactions.forEach { transaction ->
                val date = DateFormatter.formatShortDate(transaction.date)
                val type = if (transaction.type == "income") "Pemasukan" else "Pengeluaran"
                val category = escapeCsv(transaction.category)
                val description = escapeCsv(transaction.description.ifBlank { "-" })
                val user = escapeCsv(transaction.userName)
                val amount = (if (transaction.type == "income") "+" else "-") + transaction.amount.toLong().toString()

                sb.append("$date,$type,$category,$description,$user,$amount\n")
            }

            // Summary
            sb.append("\n")
            val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
            val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
            sb.append("RINGKASAN\n")
            sb.append("Total Pemasukan,${income.toLong()}\n")
            sb.append("Total Pengeluaran,${expense.toLong()}\n")
            sb.append("Saldo,${(income - expense).toLong()}\n")

            file.writeText(sb.toString())

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Escape CSV special characters
    private fun escapeCsv(text: String): String {
        return if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            "\"${text.replace("\"", "\"\"")}\""
        } else {
            text
        }
    }

    // ===== SHARE FILE =====
    fun shareFile(context: Context, file: File, mimeType: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Laporan Cashflow Family")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Laporan keuangan keluarga dari Cashflow Family."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Bagikan Laporan via")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ===== OPEN FILE =====
    fun openFile(context: Context, file: File, mimeType: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(openIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}