package com.decode.app.engine.dex

import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcodes
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
            val dex = DexFileFactory.loadDexFile(dexFile, Opcodes.getDefault())
            var classes = 0
            var methods = 0
            var fields = 0

            dex.classes.forEach { cls ->
                classes++
                methods += cls.methods.count()
                fields += cls.fields.count()
            }

            DexInfo(1, classes, methods, fields, 0)
        } catch (e: Exception) {
            DexInfo(0, 0, 0, 0, 0)
        }
    }

    fun decompileToJava(dexFile: File, outputDir: File): DecompileResult {
        return try {
            val args = jadx.api.JadxArgs()
            args.setOutDir(outputDir)
            args.setCfgOutput(false)
            args.setFallbackMode(false)

            val decompiler = jadx.api.JadxDecompiler(args)
            decompiler.loadFiles(listOf(dexFile))
            decompiler.save()

            DecompileResult(true, javaCode = "Decompilation complete to: ${outputDir.absolutePath}")
        } catch (e: Exception) {
            DecompileResult(false, error = e.message)
        }
    }
}
