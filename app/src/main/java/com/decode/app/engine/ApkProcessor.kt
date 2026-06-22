package com.decode.app.engine

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

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
                packageName = manifestInfo.first,
                versionName = manifestInfo.second,
                versionCode = manifestInfo.third,
                minSdk = manifestInfo.fourth,
                targetSdk = manifestInfo.fifth,
                fileSize = apkFile.length(),
                entryCount = outputDir.listFiles()?.size ?: 0
            )

            Result.success(apkInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractManifestInfo(apkFile: File): FiveTuple {
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
                    val pkgRegex = Regex("""package="([^"]+)""")
                    val verNameRegex = Regex("""versionName="([^"]+)""")
                    val verCodeRegex = Regex("""versionCode="([^"]+)""")
                    val minSdkRegex = Regex("""minSdkVersion="([^"]+)""")
                    val targetSdkRegex = Regex("""targetSdkVersion="([^"]+)""")

                    pkgRegex.find(manifestStr)?.let { pkg = it.groupValues[1] }
                    verNameRegex.find(manifestStr)?.let { verName = it.groupValues[1] }
                    verCodeRegex.find(manifestStr)?.let { verCode = it.groupValues[1].toIntOrNull() ?: 0 }
                    minSdkRegex.find(manifestStr)?.let { minSdk = it.groupValues[1].toIntOrNull() ?: 0 }
                    targetSdkRegex.find(manifestStr)?.let { targetSdk = it.groupValues[1].toIntOrNull() ?: 0 }
                }
            }
        } catch (_: Exception) {}

        return FiveTuple(pkg, verName, verCode, minSdk, targetSdk)
    }

    private data class FiveTuple(val first: String, val second: String, val third: Int, val fourth: Int, val fifth: Int)

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

    suspend fun rebuildApk(
        inputDir: File,
        outputFile: File,
        signingConfig: SigningConfig? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val zipBuilder = java.util.zip.ZipOutputStream(FileOutputStream(outputFile))
            inputDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val entryName = file.relativeTo(inputDir).path
                    zipBuilder.putNextEntry(java.util.zip.ZipEntry(entryName))
                    file.inputStream().use { it.copyTo(zipBuilder) }
                    zipBuilder.closeEntry()
                }
            }
            zipBuilder.close()

            if (signingConfig != null) {
                signApk(outputFile, signingConfig)
            }

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun signApk(apkFile: File, config: SigningConfig) {
        try {
            val apksigner = com.android.apksig.ApkSigner.Builder(
                listOf(
                    com.android.apksig.ApkSigner.Signer.Builder()
                        .setPrivateKey(config.privateKey)
                        .setCertificates(listOf(config.certificate))
                        .build()
                )
            )
                .setInputApk(apkFile)
                .setOutputApk(File(apkFile.absolutePath + ".signed"))
                .build()

            apksigner.sign()

            File(apkFile.absolutePath + ".signed").renameTo(apkFile)
        } catch (_: Exception) {}
    }
}

data class SigningConfig(
    val privateKey: java.security.PrivateKey,
    val certificate: java.security.cert.X509Certificate
)
