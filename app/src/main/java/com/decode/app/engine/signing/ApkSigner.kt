package com.decode.app.engine.signing

import com.android.apksig.ApkSigner
import com.android.apksig.apk.ApkFormatException
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.security.cert.CertificateFactory
import java.io.ByteArrayInputStream
import java.security.KeyPairGenerator

class ApkSignerTool {

    data class SignResult(
        val success: Boolean,
        val signedFile: File? = null,
        val error: String? = null
    )

    fun signWithKeystore(
        inputApk: File,
        keystoreFile: File,
        keystorePassword: String,
        keyAlias: String,
        keyPassword: String
    ): SignResult {
        return try {
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            FileInputStream(keystoreFile).use { fis ->
                keyStore.load(fis, keystorePassword.toCharArray())
            }

            val privateKey = keyStore.getKey(keyAlias, keyPassword.toCharArray()) as PrivateKey
            val certificate = keyStore.getCertificate(keyAlias) as X509Certificate

            val outputApk = File(inputApk.parent, "signed_${inputApk.name}")

            val signerConfig = ApkSigner.SignerConfig.Builder(
                "Decode",
                privateKey,
                listOf(certificate)
            ).build()

            val signer = ApkSigner.Builder(
                listOf(signerConfig)
            )
                .setInputApk(inputApk)
                .setOutputApk(outputApk)
                .setV1SigningEnabled(true)
                .setV2SigningEnabled(true)
                .build()

            signer.sign()

            SignResult(true, outputApk)
        } catch (e: ApkFormatException) {
            SignResult(false, error = "Invalid APK format: ${e.message}")
        } catch (e: Exception) {
            SignResult(false, error = e.message)
        }
    }

    fun signWithDefaultKey(inputApk: File): SignResult {
        return try {
            val outputApk = File(inputApk.parent, "signed_${inputApk.name}")
            val keyPair = generateKeyPair()
            val certificate = generateSelfSignedCertificate(keyPair)

            val signerConfig = ApkSigner.SignerConfig.Builder(
                "Decode",
                keyPair.private as PrivateKey,
                listOf(certificate)
            ).build()

            val signer = ApkSigner.Builder(
                listOf(signerConfig)
            )
                .setInputApk(inputApk)
                .setOutputApk(outputApk)
                .setV1SigningEnabled(true)
                .setV2SigningEnabled(true)
                .build()

            signer.sign()

            SignResult(true, outputApk)
        } catch (e: Exception) {
            SignResult(false, error = e.message)
        }
    }

    private fun generateKeyPair(): java.security.KeyPair {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        return generator.generateKeyPair()
    }

    private fun generateSelfSignedCertificate(keyPair: java.security.KeyPair): X509Certificate {
        val certBytes = createSelfSignedCertBytes(keyPair)
        val factory = CertificateFactory.getInstance("X.509")
        return factory.generateCertificate(ByteArrayInputStream(certBytes)) as X509Certificate
    }

    private fun createSelfSignedCertBytes(
        keyPair: java.security.KeyPair
    ): ByteArray = try {
        val byteStream = java.io.ByteArrayOutputStream()
        byteStream.write("-----BEGIN CERTIFICATE-----\n".toByteArray())
        val encoded = java.util.Base64.getMimeEncoder().encode(
            keyPair.public.encoded
        )
        byteStream.write(encoded)
        byteStream.write("\n-----END CERTIFICATE-----\n".toByteArray())

        val cf = CertificateFactory.getInstance("X.509")
        val cert = cf.generateCertificate(
            ByteArrayInputStream(byteStream.toByteArray())
        ) as X509Certificate
        cert.encoded
    } catch (e: Exception) {
        byteArrayOf()
    }
}
