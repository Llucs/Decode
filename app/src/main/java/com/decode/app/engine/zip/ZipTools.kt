package com.decode.app.engine.zip

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipTools {

    fun extractAll(zipFile: File, outputDir: File): List<File> {
        val extracted = mutableListOf<File>()
        outputDir.mkdirs()

        ZipInputStream(zipFile.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val entryFile = File(outputDir, entry.name)
                if (entry.isDirectory) {
                    entryFile.mkdirs()
                } else {
                    entryFile.parentFile?.mkdirs()
                    FileOutputStream(entryFile).use { fos ->
                        zis.copyTo(fos)
                    }
                    extracted.add(entryFile)
                }
                entry = zis.nextEntry
            }
        }

        return extracted
    }

    fun compressToZip(inputDir: File, outputFile: File): File {
        ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
            inputDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val entryName = file.relativeTo(inputDir).path
                    zos.putNextEntry(ZipEntry(entryName))
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }
        return outputFile
    }

    fun listEntries(zipFile: File): List<String> {
        val entries = mutableListOf<String>()
        java.util.zip.ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                entries.add(entry.name)
            }
        }
        return entries
    }

    fun extractEntry(zipFile: File, entryName: String, outputFile: File): File {
        java.util.zip.ZipFile(zipFile).use { zip ->
            val entry = zip.getEntry(entryName)
            if (entry != null) {
                outputFile.parentFile?.mkdirs()
                zip.getInputStream(entry).use { it.copyTo(outputFile.outputStream()) }
            }
        }
        return outputFile
    }
}
