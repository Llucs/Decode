package com.decode.app.engine.axml

import java.io.File
import java.io.InputStream

class AxmlParser {

    data class AXmlInfo(
        val packageName: String?,
        val versionName: String?,
        val versionCode: Int?,
        val minSdk: Int?,
        val targetSdk: Int?,
        val activities: List<String>,
        val services: List<String>,
        val receivers: List<String>,
        val permissions: List<String>,
        val rawXml: String
    )

    fun parse(inputStream: InputStream): AXmlInfo {
        val xmlBytes = inputStream.readBytes()
        val xmlString = String(xmlBytes)

        val pkg = extractAttribute(xmlString, "package")
        val verName = extractAttribute(xmlString, "versionName")
        val verCode = extractAttribute(xmlString, "versionCode")?.toIntOrNull()
        val minSdk = extractAttribute(xmlString, "minSdkVersion")?.toIntOrNull()
        val targetSdk = extractAttribute(xmlString, "targetSdkVersion")?.toIntOrNull()

        val activities = extractTags(xmlString, "activity")
        val services = extractTags(xmlString, "service")
        val receivers = extractTags(xmlString, "receiver")
        val permissions = extractTags(xmlString, "uses-permission")

        return AXmlInfo(
            packageName = pkg,
            versionName = verName,
            versionCode = verCode,
            minSdk = minSdk,
            targetSdk = targetSdk,
            activities = activities,
            services = services,
            receivers = receivers,
            permissions = permissions,
            rawXml = xmlString
        )
    }

    private fun extractAttribute(xml: String, attr: String): String? {
        val regex = Regex("""$attr="([^"]+)"""")
        return regex.find(xml)?.groupValues?.getOrNull(1)
    }

    private fun extractTags(xml: String, tag: String): List<String> {
        val regex = Regex("""<$tag[^>]*name="([^"]+)"""")
        return regex.findAll(xml).map { it.groupValues[1] }.toList()
    }

    fun parse(file: File): AXmlInfo = parse(file.inputStream())
    fun parse(bytes: ByteArray): AXmlInfo = parse(java.io.ByteArrayInputStream(bytes))
}
