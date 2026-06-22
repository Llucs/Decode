package com.decode.app.engine.dex

import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class Dex2JarConverter {

    data class ConversionResult(
        val success: Boolean,
        val outputFile: File? = null,
        val classCount: Int = 0,
        val error: String? = null
    )

    fun convert(dexFile: File, outputJar: File): ConversionResult {
        return try {
            outputJar.parentFile?.mkdirs()
            val classes = mutableListOf<String>()

            val dex = org.jf.dexlib2.DexFileFactory.loadDexFile(
                dexFile,
                org.jf.dexlib2.Opcodes.getDefault()
            )

            JarOutputStream(outputJar.outputStream()).use { jos ->
                dex.classes.forEach { classDef ->
                    val className = classDef.type.replace('/', '.')
                        .removeSuffix(";")
                        .removePrefix("L") + ".class"
                    classes.add(className)
                    jos.putNextEntry(JarEntry(className))
                    jos.closeEntry()
                }
            }

            ConversionResult(true, outputJar, classes.size)
        } catch (e: Exception) {
            ConversionResult(false, error = e.message)
        }
    }
}
