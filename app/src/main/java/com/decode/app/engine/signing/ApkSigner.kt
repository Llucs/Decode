package com.decode.app.engine.signing

import com.android.apksig.ApkSigner
import com.android.apksig.apk.ApkFormatException
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate

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

            val signer = ApkSigner.Builder(
                listOf(
                    ApkSigner.Signer.Builder()
                        .setPrivateKey(privateKey)
                        .setCertificates(listOf(certificate))
                        .build()
                )
            )
                .setInputApk(inputApk)
                .setOutputApk(outputApk)
                .setV1SigningEnabled(true)
                .setV2SigningEnabled(true)
                .setV3SigningEnabled(true)
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

            val signer = ApkSigner.Builder(
                listOf(
                    ApkSigner.Signer.Builder()
                        .setPrivateKey(generateTestKeyPair().private as PrivateKey)
                        .setCertificates(listOf(generateTestCertificate()))
                        .build()
                )
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

    private fun generateTestKeyPair(): java.security.KeyPair {
        val generator = java.security.KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        return generator.generateKeyPair()
    }

    private fun generateTestCertificate(): java.security.cert.X509Certificate {
        val keyPair = generateTestKeyPair()
        val dn = "CN=Decode, OU=Development, O=Decode, L=Unknown, ST=Unknown, C=UN"
        val cert = org.bouncycastle.asn1.x500.X500Name(dn)
        // Using a simple self-signed cert approach
        return try {
            val factory = java.security.cert.CertificateFactory.getInstance("X.509")
            val certBytes = generateSelfSignedCertBytes(keyPair)
            factory.generateCertificate(java.io.ByteArrayInputStream(certBytes)) as java.security.cert.X509Certificate
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate test certificate", e)
        }
    }

    private fun generateSelfSignedCertBytes(keyPair: java.security.KeyPair): ByteArray {
        // Simplified self-signed certificate generation
        return try {
            val dn = "CN=Decode Test Key"
            val validity = 365 * 24 * 60 * 60

            val keyStore = java.security.KeyStore.getInstance("PKCS12")
            keyStore.load(null, null)
            keyStore.setKeyEntry("decode", keyPair.private, "password".toCharArray(), null)

            java.io.ByteArrayOutputStream().use { baos ->
                keyStore.store(baos, "password".toCharArray())
                baos.toByteArray()
            }
        } catch (e: Exception) {
            ByteArray(0)
        }
    }
}
