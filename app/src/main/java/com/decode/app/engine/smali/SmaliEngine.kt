package com.decode.app.engine.smali

import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.dexbacked.DexBackedDexFile
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.writer.io.FileDataStore
import org.jf.dexlib2.writer.pool.DexPool
import java.io.File

class SmaliEngine {

    data class SmaliResult(
        val success: Boolean,
        val classes: List<String> = emptyList(),
        val error: String? = null
    )

    fun disassembleDex(dexFile: File): SmaliResult {
        return try {
            val dex = DexFileFactory.loadDexFile(dexFile, Opcodes.getDefault())
            val classNames = mutableListOf<String>()
            dex.classes.forEach { classDef ->
                classNames.add(classDef.type)
            }
            SmaliResult(true, classes = classNames)
        } catch (e: Exception) {
            SmaliResult(false, error = e.message)
        }
    }

    fun assembleDex(outputDex: File): SmaliResult {
        return try {
            val dexPool = DexPool(Opcodes.getDefault())
            outputDex.parentFile?.mkdirs()
            dexPool.writeTo(FileDataStore(outputDex))
            SmaliResult(true)
        } catch (e: Exception) {
            SmaliResult(false, error = e.message)
        }
    }

    fun getClassInfo(dexFile: File, className: String): SmaliResult {
        return try {
            val dex = DexFileFactory.loadDexFile(dexFile, Opcodes.getDefault())
            val classes = dex.classes.filter { it.type.contains(className) }.map { it.type }
            SmaliResult(true, classes = classes)
        } catch (e: Exception) {
            SmaliResult(false, error = e.message)
        }
    }
}
