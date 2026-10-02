package com.example.data.scanner

import java.util.Locale
import java.util.regex.Pattern

/**
 * Result of QR/Barcode parsing.
 */
sealed class QrParseResult {
    data class Success(
        val equipmentId: String,
        val rawContent: String,
        val formatDetected: String
    ) : QrParseResult()

    data class Invalid(
        val rawContent: String,
        val reason: String
    ) : QrParseResult()
}

/**
 * Parsing logic for industrial equipment QR codes and barcodes.
 * Supports structured plant formats, URLs, SAP EAM tags, JSON, key-value, and raw IDs.
 * Extensible for future SAP PM/EAM scanning schemes.
 */
object QrCodeParser {

    private val JSON_EQUIPMENT_ID_REGEX = Pattern.compile(
        "\"equipmentId\"\\s*:\\s*\"([A-Za-z0-9_-]+)\"",
        Pattern.CASE_INSENSITIVE
    )

    private val KEY_VALUE_REGEX = Pattern.compile(
        "(?:EQUIPMENT_ID|EQUIPMENT|EQ_ID|EQ)\\s*[:=]\\s*([A-Za-z0-9_-]+)",
        Pattern.CASE_INSENSITIVE
    )

    fun parse(rawCode: String?): QrParseResult {
        if (rawCode.isNullOrBlank()) {
            return QrParseResult.Invalid("", "Empty QR or barcode content")
        }

        val trimmed = rawCode.trim()

        // 1. Structured PlantCare Format: PLANTCARE:EQUIPMENT:121SC008
        if (trimmed.startsWith("PLANTCARE:EQUIPMENT:", ignoreCase = true)) {
            val id = trimmed.substring("PLANTCARE:EQUIPMENT:".length).trim()
            if (id.isNotBlank()) {
                return QrParseResult.Success(id.uppercase(Locale.ROOT), trimmed, "PlantCare Standard Tag")
            }
        }

        // 2. SAP EAM Format: SAP:EAM:121SC008 or SAP:PM:121SC008
        if (trimmed.startsWith("SAP:EAM:", ignoreCase = true) || trimmed.startsWith("SAP:PM:", ignoreCase = true)) {
            val prefixLen = if (trimmed.startsWith("SAP:EAM:", ignoreCase = true)) 8 else 7
            val id = trimmed.substring(prefixLen).trim()
            if (id.isNotBlank()) {
                return QrParseResult.Success(id.uppercase(Locale.ROOT), trimmed, "SAP EAM Tag")
            }
        }

        // 3. Web URL Format: https://plantcare.ai/eq/121SC008 or .../equipment/121SC008
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            val lastSlash = trimmed.lastIndexOf('/')
            if (lastSlash != -1 && lastSlash < trimmed.length - 1) {
                val candidate = trimmed.substring(lastSlash + 1).split('?', '#')[0].trim()
                if (isValidEquipmentId(candidate)) {
                    return QrParseResult.Success(candidate.uppercase(Locale.ROOT), trimmed, "Web Asset URL")
                }
            }
        }

        // 4. JSON Format: {"equipmentId": "121SC008"}
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            val matcher = JSON_EQUIPMENT_ID_REGEX.matcher(trimmed)
            if (matcher.find()) {
                val id = matcher.group(1)?.trim()
                if (!id.isNullOrBlank()) {
                    return QrParseResult.Success(id.uppercase(Locale.ROOT), trimmed, "JSON Payload")
                }
            }
        }

        // 5. Key-Value Tag: EQUIPMENT_ID=121SC008 or EQUIPMENT:121SC008
        val kvMatcher = KEY_VALUE_REGEX.matcher(trimmed)
        if (kvMatcher.find()) {
            val id = kvMatcher.group(1)?.trim()
            if (!id.isNullOrBlank()) {
                return QrParseResult.Success(id.uppercase(Locale.ROOT), trimmed, "Key-Value Tag")
            }
        }

        // 6. Direct Raw Equipment ID (e.g. 121SC008, CV04, AF01, CRU01)
        if (isValidEquipmentId(trimmed)) {
            return QrParseResult.Success(trimmed.uppercase(Locale.ROOT), trimmed, "Direct Barcode / Serial ID")
        }

        return QrParseResult.Invalid(trimmed, "Unrecognized equipment code format")
    }

    /**
     * Checks if string matches standard industrial equipment ID conventions (e.g., alphanumeric with hyphens/underscores).
     */
    private fun isValidEquipmentId(candidate: String): Boolean {
        if (candidate.length < 2 || candidate.length > 32) return false
        // Allow alphanumeric, dashes, underscores, and slashes
        return candidate.matches(Regex("^[A-Za-z0-9_\\-/]+$"))
    }
}
