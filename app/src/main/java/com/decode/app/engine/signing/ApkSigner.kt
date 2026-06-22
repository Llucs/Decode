package com.decode.app.engine.signing

import com.android.apksig.ApkSigner
import com.android.apksig.apk.ApkFormatException
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Security
import java.security.cert.X509Certificate
import java.util.Date

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
        return signWithDefaultKey(inputApk, File(inputApk.parent, "signed_${inputApk.name}"))
    }

    fun signWithDefaultKey(inputApk: File, outputApk: File): SignResult {
        return try {
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
        Security.addProvider(BouncyCastleProvider())

        val dn = X500Name("CN=Decode, OU=Development, O=Decode, C=UN")
        val validity = 365 * 24 * 60 * 60 * 1000L
        val notBefore = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L)
        val notAfter = Date(System.currentTimeMillis() + validity)

        val pubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.public.encoded)
        val certBuilder = X509v3CertificateBuilder(
            dn,
            BigInteger.valueOf(System.currentTimeMillis()),
            notBefore,
            notAfter,
            dn,
            pubKeyInfo
        )

        val signer = JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.private as PrivateKey)
        val certHolder = certBuilder.build(signer)
        return JcaX509CertificateConverter().getCertificate(certHolder)
    }
}
