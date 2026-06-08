package com.example.beautyparlor

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp

@Composable
fun QRCodeDisplay(url: String, modifier: Modifier = Modifier) {
    // Generate the QR code as a bitmap
    val qrBitmap = remember(url) {
        val size = 512 // QR code image size
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bitmap.asImageBitmap()
    }

    Image(
        bitmap = qrBitmap,
        contentDescription = "QR Code for App Download",
        modifier = modifier.size(250.dp) // Set the size for display
    )
}