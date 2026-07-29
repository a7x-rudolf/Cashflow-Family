package com.app.cashflowfamily.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

/**
 * Util untuk foto profil yang di-upload manual oleh user.
 *
 * Project ini masih di Firebase plan Spark (belum Blaze), jadi Firebase Storage
 * tidak dipakai. Sebagai gantinya, foto dikompres kecil lalu disimpan langsung
 * sebagai data URI base64 di field `photoUrl` pada dokumen user di Firestore
 * (batas 1 dokumen Firestore adalah 1 MB, jadi ukuran hasil kompres di sini
 * dijaga jauh di bawah itu, biasanya puluhan KB saja).
 */
object ImageUtils {

    private const val TAG = "ImageUtils"
    private const val MAX_DIMENSION = 512
    private const val JPEG_QUALITY = 80
    const val DATA_URI_PREFIX = "data:image/jpeg;base64,"

    /**
     * Baca gambar dari [uri] hasil Photo Picker, resize ke maksimal
     * [MAX_DIMENSION]x[MAX_DIMENSION] (mempertahankan aspect ratio, crop persegi
     * di tengah), kompres ke JPEG, lalu encode ke data URI base64.
     *
     * Return null kalau gagal dibaca/decode.
     */
    fun compressImageToDataUri(context: Context, uri: Uri): String? {
        return try {
            val original = context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            } ?: return null

            val squared = cropToSquare(original)
            val resized = if (squared.width > MAX_DIMENSION) {
                Bitmap.createScaledBitmap(squared, MAX_DIMENSION, MAX_DIMENSION, true)
            } else {
                squared
            }

            val outputStream = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            val bytes = outputStream.toByteArray()

            if (resized !== original) original.recycle()
            if (resized !== squared) squared.recycle()

            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            DATA_URI_PREFIX + base64
        } catch (e: Exception) {
            Log.e(TAG, "Gagal kompres foto profil", e)
            null
        }
    }

    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        return if (bitmap.width == bitmap.height) {
            bitmap
        } else {
            Bitmap.createBitmap(bitmap, x, y, size, size)
        }
    }

    /** Decode data URI base64 (hasil [compressImageToDataUri]) jadi Bitmap untuk ditampilkan. */
    fun decodeDataUriToBitmap(dataUri: String): Bitmap? {
        return try {
            val base64 = dataUri.substringAfter(",", missingDelimiterValue = "")
            if (base64.isBlank()) return null
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal decode foto profil", e)
            null
        }
    }

    fun isDataUri(value: String): Boolean = value.startsWith("data:image")

    fun isHttpUrl(value: String): Boolean =
        value.startsWith("http://") || value.startsWith("https://")
}
