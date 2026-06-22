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
import java.security.cert.Certificate

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
                listOf(certificate as Certificate)
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
                listOf(certificate as Certificate)
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
        val dn = "CN=Decode, OU=Development, O=Decode, L=Unknown, ST=Unknown, C=UN"
        val validity = 365 * 24 * 60 * 60
        val algorithm = "SHA256WithRSA"

        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(null, null)

        val certBytes = createSelfSignedCertBytes(keyPair, dn, algorithm, validity)
        val factory = CertificateFactory.getInstance("X.509")
        return factory.generateCertificate(ByteArrayInputStream(certBytes)) as X509Certificate
    }

    private fun createSelfSignedCertBytes(
        keyPair: java.security.KeyPair,
        dn: String,
        algorithm: String,
        validity: Int
    ): ByteArray {
        try {
            val classDef = Class.forName("javax.security.auth.x500.X500Principal")
            val principal = classDef.getConstructor(String::class.java).newInstance(dn)
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val startDate = calendar.time
            calendar.add(java.util.Calendar.DAY_OF_YEAR, validity)
            val endDate = calendar.time

            val sigClass = Class.forName("java.security.cert.CertificateFactory")
            val cf = CertificateFactory.getInstance("X.509")

            val byteStream = java.io.ByteArrayOutputStream()
            byteStream.write("-----BEGIN CERTIFICATE-----\n".toByteArray())
            val encoded = java.util.Base64.getMimeEncoder().encode(
                keyPair.public.encoded
            )
            byteStream.write(encoded)
            byteStream.write("\n-----END CERTIFICATE-----\n".toByteArray())

            return try {
                val cert = cf.generateCertificate(
                    ByteArrayInputStream(byteStream.toByteArray())
                ) as X509Certificate
                cert.encoded
            } catch (e: Exception) {
                byteArrayOf()
            }
        } catch (e: Exception) {
            byteArrayOf()
        }
    }
}
