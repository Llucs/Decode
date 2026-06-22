package com.decode.app.engine.svg

import android.graphics.Picture
import com.caverock.androidsvg.SVG
import java.io.File
import java.io.InputStream

class SvgRenderer {

    fun renderToPicture(inputStream: InputStream): Picture? {
        return try {
            val svg = SVG.getFromInputStream(inputStream)
            svg.renderToPicture()
        } catch (e: Exception) {
            null
        }
    }

    fun renderToPicture(file: File): Picture? {
        return try {
            val svg = SVG.getFromInputStream(file.inputStream())
            svg.renderToPicture()
        } catch (e: Exception) {
            null
        }
    }

    fun renderToPicture(svgContent: String): Picture? {
        return try {
            val svg = SVG.getFromString(svgContent)
            svg.renderToPicture()
        } catch (e: Exception) {
            null
        }
    }
}
