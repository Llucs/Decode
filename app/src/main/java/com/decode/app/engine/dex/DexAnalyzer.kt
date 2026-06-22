package com.decode.app.engine.dex

import jadx.api.JadxArgs
import jadx.api.JadxDecompiler
import java.io.File

class DexAnalyzer {

    data class DexInfo(
        val dexCount: Int,
        val classCount: Int,
        val methodCount: Int,
        val fieldCount: Int,
        val stringCount: Int
    )

    data class DecompileResult(
        val success: Boolean,
        val javaCode: String = "",
        val error: String? = null
    )

    fun analyzeDex(dexFile: File): DexInfo {
        return try {
            val dex = org.jf.dexlib2.DexFileFactory.loadDexFile(dexFile, org.jf.dexlib2.Opcodes.getDefault())
            var classes = 0
            var methods = 0
            var fields = 0
            var strings = 0

            dex.classes.forEach { cls ->
                classes++
                methods += cls.methods.size
                fields += cls.fields.size
            }

            if (dex is org.jf.dexlib2.dexbacked.DexBackedDexFile) {
                strings = dex.stringsCount
            }

            DexInfo(1, classes, methods, fields, strings)
        } catch (e: Exception) {
            DexInfo(0, 0, 0, 0, 0)
        }
    }

    fun decompileToJava(dexFile: File, outputDir: File): DecompileResult {
        return try {
            val args = JadxArgs()
            args.setOutDir(outputDir)
            args.setFsCaseSensitive(false)
            args.setShowInconsistentCode(true)
            args.setCfgOutput(false)
            args.setRawCfgOutput(false)
            args.setFallbackMode(false)

            val decompiler = JadxDecompiler(args)
            decompiler.loadFile(dexFile)
            decompiler.save()

            val javaFiles = outputDir.walkTopDown()
                .filter { it.extension == "java" }
                .map { it.relativeTo(outputDir).path }
                .toList()

            DecompileResult(true, javaCode = javaFiles.joinToString("\n"))
        } catch (e: Exception) {
            DecompileResult(false, error = e.message)
        }
    }
}
