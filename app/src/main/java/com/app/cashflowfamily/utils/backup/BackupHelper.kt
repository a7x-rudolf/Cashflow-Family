package com.app.cashflowfamily.utils.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.app.cashflowfamily.viewmodel.BackupData
import com.google.gson.GsonBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupHelper {

    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /**
     * Export backup data ke file JSON
     */
    fun exportBackup(context: Context, backupData: BackupData): File? {
        return try {
            val backupDir = File(context.getExternalFilesDir(null), "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "CashflowFamily_Backup_$timestamp.json"
            val file = File(backupDir, fileName)

            val jsonString = gson.toJson(backupData)
            file.writeText(jsonString)

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Import backup data dari file JSON
     */
    fun importBackup(context: Context, fileUri: Uri): BackupData? {
        return try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: return null

            val jsonString = inputStream.bufferedReader().use { it.readText() }
            gson.fromJson(jsonString, BackupData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Share backup file
     */
    fun shareBackup(context: Context, file: File) {
        try {
            android.util.Log.d("BackupHelper", "Sharing file: ${file.absolutePath}")
            android.util.Log.d("BackupHelper", "File exists: ${file.exists()}")
            android.util.Log.d("BackupHelper", "File size: ${file.length()} bytes")

            if (!file.exists()) {
                android.util.Log.e("BackupHelper", "File does not exist!")
                android.widget.Toast.makeText(
                    context,
                    "File backup tidak ditemukan",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return
            }

            val authority = "${context.packageName}.fileprovider"
            android.util.Log.d("BackupHelper", "FileProvider authority: $authority")

            val uri: Uri = FileProvider.getUriForFile(
                context,
                authority,
                file
            )

            android.util.Log.d("BackupHelper", "URI: $uri")

            // Support 2 mime types (JSON + wildcard) untuk kompatibilitas
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Backup Cashflow Family")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "File backup keuangan keluarga dari Cashflow Family."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Simpan/Bagikan Backup ke")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(chooserIntent)

            android.util.Log.d("BackupHelper", "Share intent launched")
        } catch (e: Exception) {
            android.util.Log.e("BackupHelper", "Error sharing file", e)
            android.widget.Toast.makeText(
                context,
                "Gagal share: ${e.message}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Validasi backup data
     */
    fun validateBackup(backupData: BackupData): ValidationResult {
        return when {
            backupData.version.isBlank() ->
                ValidationResult(false, "File backup tidak valid: version kosong")

            backupData.familyName.isBlank() && backupData.transactions.isEmpty() ->
                ValidationResult(false, "File backup kosong atau rusak")

            else -> ValidationResult(true, "Backup valid")
        }
    }

    data class ValidationResult(
        val isValid: Boolean,
        val message: String
    )
}