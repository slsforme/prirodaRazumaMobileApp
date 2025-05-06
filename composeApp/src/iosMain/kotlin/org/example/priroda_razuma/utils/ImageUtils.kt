package org.example.priroda_razuma.utils

import org.jetbrains.skia.Image
import org.jetbrains.skia.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    val skiaImage = Image.makeFromEncoded(this)
    val bitmap = Bitmap().apply {
        allocPixels(skiaImage.imageInfo)
        skiaImage.readPixels(this, 0, 0)
    }
    return bitmap.asComposeImageBitmap()
}