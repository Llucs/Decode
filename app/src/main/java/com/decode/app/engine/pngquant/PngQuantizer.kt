package com.decode.app.engine.pngquant

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import java.io.File
import java.io.FileOutputStream

class PngQuantizer {

    data class QuantizeResult(
        val success: Boolean,
        val originalSize: Long = 0,
        val compressedSize: Long = 0,
        val savingsPercent: Double = 0.0,
        val error: String? = null
    )

    fun compress(inputFile: File, outputFile: File, maxColors: Int = 256): QuantizeResult {
        return try {
            val originalSize = inputFile.length()
            val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath)
                ?: return QuantizeResult(false, error = "Failed to decode image")

            val compressed = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(compressed)
            canvas.drawBitmap(bitmap, 0f, 0f, null)

            FileOutputStream(outputFile).use { out ->
                compressed.compress(Bitmap.CompressFormat.PNG, 80, out)
            }

            val compressedSize = outputFile.length()
            val savings = if (originalSize > 0) {
                (1.0 - compressedSize.toDouble() / originalSize) * 100.0
            } else 0.0

            QuantizeResult(true, originalSize, compressedSize, savings)
        } catch (e: Exception) {
            QuantizeResult(false, error = e.message)
        }
    }
}
