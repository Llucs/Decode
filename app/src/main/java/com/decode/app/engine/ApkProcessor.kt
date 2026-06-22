package com.decode.app.engine

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ApkProcessor(private val context: Context) {

    data class ApkInfo(
        val packageName: String,
        val versionName: String,
        val versionCode: Int,
        val minSdk: Int,
        val targetSdk: Int,
        val fileSize: Long,
        val entryCount: Int
    )

    suspend fun processApk(uri: Uri, outputDir: File): Result<ApkInfo> = withContext(Dispatchers.IO) {
        try {
            outputDir.mkdirs()
            val apkFile = File(outputDir, "source.apk")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(apkFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Cannot open APK file"))

            val manifestInfo = extractManifestInfo(apkFile)
            extractApk(apkFile, outputDir)

            val apkInfo = ApkInfo(
                packageName = manifestInfo.pkg,
                versionName = manifestInfo.verName,
                versionCode = manifestInfo.verCode,
                minSdk = manifestInfo.minSdk,
                targetSdk = manifestInfo.targetSdk,
                fileSize = apkFile.length(),
                entryCount = outputDir.listFiles()?.size ?: 0
            )

            Result.success(apkInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class ManifestInfo(
        val pkg: String, val verName: String, val verCode: Int,
        val minSdk: Int, val targetSdk: Int
    )

    private fun extractManifestInfo(apkFile: File): ManifestInfo {
        var pkg = "unknown"
        var verName = "unknown"
        var verCode = 0
        var minSdk = 0
        var targetSdk = 0

        try {
            java.util.jar.JarFile(apkFile).use { jar ->
                val entry = jar.getJarEntry("AndroidManifest.xml")
                if (entry != null) {
                    val manifestBytes = jar.getInputStream(entry).readBytes()
                    val manifestStr = String(manifestBytes)
                    Regex("""package="([^"]+)""").find(manifestStr)?.let { pkg = it.groupValues[1] }
                    Regex("""versionName="([^"]+)""").find(manifestStr)?.let { verName = it.groupValues[1] }
                    Regex("""versionCode="([^"]+)""").find(manifestStr)?.let { verCode = it.groupValues[1].toIntOrNull() ?: 0 }
                    Regex("""minSdkVersion="([^"]+)""").find(manifestStr)?.let { minSdk = it.groupValues[1].toIntOrNull() ?: 0 }
                    Regex("""targetSdkVersion="([^"]+)""").find(manifestStr)?.let { targetSdk = it.groupValues[1].toIntOrNull() ?: 0 }
                }
            }
        } catch (_: Exception) {}

        return ManifestInfo(pkg, verName, verCode, minSdk, targetSdk)
    }

    private fun extractApk(apkFile: File, outputDir: File) {
        java.util.zip.ZipFile(apkFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val entryFile = File(outputDir, entry.name)
                if (entry.isDirectory) {
                    entryFile.mkdirs()
                } else {
                    entryFile.parentFile?.mkdirs()
                    zip.getInputStream(entry).use { input ->
                        FileOutputStream(entryFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
    }

    suspend fun processApkFromFile(apkFile: File, outputDir: File): Result<ApkInfo> = withContext(Dispatchers.IO) {
        try {
            outputDir.mkdirs()
            val manifestInfo = extractManifestInfo(apkFile)
            extractApk(apkFile, outputDir)

            val apkInfo = ApkInfo(
                packageName = manifestInfo.pkg,
                versionName = manifestInfo.verName,
                versionCode = manifestInfo.verCode,
                minSdk = manifestInfo.minSdk,
                targetSdk = manifestInfo.targetSdk,
                fileSize = apkFile.length(),
                entryCount = outputDir.listFiles()?.size ?: 0
            )

            Result.success(apkInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rebuildApk(
        inputDir: File,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            java.util.zip.ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
                inputDir.walkTopDown().forEach { file ->
                    if (file.isFile) {
                        val entryName = file.relativeTo(inputDir).path
                        zos.putNextEntry(java.util.zip.ZipEntry(entryName))
                        file.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
