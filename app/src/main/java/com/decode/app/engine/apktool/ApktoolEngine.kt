package com.decode.app.engine.apktool

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileInputStream

class ApktoolEngine {

    data class ApktoolResult(
        val success: Boolean,
        val outputDir: File? = null,
        val error: String? = null
    )

    fun decodeApk(apkFile: File, outputDir: File): ApktoolResult {
        return try {
            outputDir.mkdirs()
            decodeResources(apkFile, outputDir)
            decodeManifest(apkFile, outputDir)
            decodeDex(apkFile, outputDir)
            ApktoolResult(true, outputDir)
        } catch (e: Exception) {
            ApktoolResult(false, error = e.message)
        }
    }

    private fun decodeResources(apkFile: File, outputDir: File) {
        val resDir = File(outputDir, "res")
        resDir.mkdirs()
        try {
            java.util.zip.ZipFile(apkFile).use { zip ->
                zip.entries().asSequence().filter { it.name.startsWith("res/") }.forEach { entry ->
                    val outFile = File(outputDir, entry.name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        zip.getInputStream(entry).use { it.copyTo(outFile.outputStream()) }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun decodeManifest(apkFile: File, outputDir: File) {
        try {
            java.util.zip.ZipFile(apkFile).use { zip ->
                val entry = zip.getEntry("AndroidManifest.xml")
                if (entry != null) {
                    val manifestFile = File(outputDir, "AndroidManifest.xml")
                    zip.getInputStream(entry).use { it.copyTo(manifestFile.outputStream()) }
                }
            }
        } catch (_: Exception) {}
    }

    private fun decodeDex(apkFile: File, outputDir: File) {
        val smaliDir = File(outputDir, "smali")
        smaliDir.mkdirs()
        try {
            java.util.zip.ZipFile(apkFile).use { zip ->
                zip.entries().asSequence().filter {
                    it.name.endsWith(".dex")
                }.forEach { entry ->
                    val dexFile = File(outputDir, entry.name)
                    zip.getInputStream(entry).use { it.copyTo(dexFile.outputStream()) }
                }
            }
        } catch (_: Exception) {}
    }

    fun buildApk(inputDir: File, outputFile: File): ApktoolResult {
        return try {
            java.util.zip.ZipOutputStream(outputFile.outputStream()).use { zos ->
                inputDir.walkTopDown().forEach { file ->
                    if (file.isFile) {
                        val entryName = file.relativeTo(inputDir).path
                        zos.putNextEntry(java.util.zip.ZipEntry(entryName))
                        file.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
            ApktoolResult(true, outputFile)
        } catch (e: Exception) {
            ApktoolResult(false, error = e.message)
        }
    }
}
