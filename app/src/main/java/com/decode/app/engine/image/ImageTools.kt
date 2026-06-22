package com.decode.app.engine.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Picture
import com.caverock.androidsvg.SVG
import java.io.File
import java.io.FileOutputStream

class ImageTools {

    fun convertSvgToPng(svgFile: File, outputPng: File, width: Int = 512, height: Int = 512): Boolean {
        return try {
            val svg = SVG.getFromInputStream(svgFile.inputStream())
            val picture = svg.renderToPicture()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawPicture(picture)
            FileOutputStream(outputPng).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getBitmapInfo(file: File): BitmapFactory.Options {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, options)
        return options
    }

    fun scaleBitmap(file: File, maxWidth: Int, maxHeight: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, options)

        val scaleFactor = maxOf(
            options.outWidth / maxWidth,
            options.outHeight / maxHeight,
            1
        )

        options.inJustDecodeBounds = false
        options.inSampleSize = scaleFactor

        return BitmapFactory.decodeFile(file.absolutePath, options)
    }
}
