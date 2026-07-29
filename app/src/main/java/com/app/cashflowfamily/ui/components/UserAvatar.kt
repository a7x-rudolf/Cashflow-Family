package com.app.cashflowfamily.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.app.cashflowfamily.utils.ImageUtils

/**
 * Avatar profil yang dipakai di Beranda, Keluarga, dan Setelan.
 *
 * Prioritas tampilan:
 * 1. [photoUrl] berupa data URI base64 (hasil upload manual) -> decode & tampilkan Bitmap
 * 2. [photoUrl] berupa URL https (foto akun Google) -> load lewat Coil
 * 3. Tidak ada foto -> avatar inisial nama (perilaku default/lama)
 */
@Composable
fun UserAvatar(
    modifier: Modifier = Modifier,
    name: String,
    photoUrl: String? = null,
    size: Dp = 40.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.onPrimary,
    textColor: Color = MaterialTheme.colorScheme.primary,
) {
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val fontSize = (size.value * 0.4f).sp

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        when {
            !photoUrl.isNullOrBlank() && ImageUtils.isDataUri(photoUrl) -> {
                val bitmap: Bitmap? = remember(photoUrl) {
                    ImageUtils.decodeDataUriToBitmap(photoUrl)
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Foto profil $name",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(size)
                            .clip(CircleShape)
                    )
                } else {
                    InitialText(initial, fontSize, textColor)
                }
            }

            !photoUrl.isNullOrBlank() && ImageUtils.isHttpUrl(photoUrl) -> {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto profil $name",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape)
                )
            }

            else -> {
                InitialText(initial, fontSize, textColor)
            }
        }
    }
}

@Composable
private fun InitialText(initial: String, fontSize: TextUnit, textColor: Color) {
    Text(
        text = initial,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = textColor
    )
}
