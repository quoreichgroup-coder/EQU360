package com.example

import com.example.data.scanner.QrCodeParser
import com.example.data.scanner.QrParseResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QrCodeParserTest {

    @Test
    fun parse_structuredPlantCareFormat_extractsEquipmentId() {
        val result = QrCodeParser.parse("PLANTCARE:EQUIPMENT:121SC008")
        assertTrue(result is QrParseResult.Success)
        val success = result as QrParseResult.Success
        assertEquals("121SC008", success.equipmentId)
    }

    @Test
    fun parse_rawEquipmentId_extractsEquipmentId() {
        val result = QrCodeParser.parse("121SC008")
        assertTrue(result is QrParseResult.Success)
        val success = result as QrParseResult.Success
        assertEquals("121SC008", success.equipmentId)
    }

    @Test
    fun parse_sapEamFormat_extractsEquipmentId() {
        val result = QrCodeParser.parse("SAP:EAM:CRU01")
        assertTrue(result is QrParseResult.Success)
        val success = result as QrParseResult.Success
        assertEquals("CRU01", success.equipmentId)
    }

    @Test
    fun parse_webAssetUrl_extractsEquipmentId() {
        val result = QrCodeParser.parse("https://plantcare.ai/eq/CV04")
        assertTrue(result is QrParseResult.Success)
        val success = result as QrParseResult.Success
        assertEquals("CV04", success.equipmentId)
    }

    @Test
    fun parse_jsonFormat_extractsEquipmentId() {
        val result = QrCodeParser.parse("{\"equipmentId\": \"AF01\"}")
        assertTrue(result is QrParseResult.Success)
        val success = result as QrParseResult.Success
        assertEquals("AF01", success.equipmentId)
    }

    @Test
    fun parse_keyValueFormat_extractsEquipmentId() {
        val result = QrCodeParser.parse("EQUIPMENT_ID=CV06A")
        assertTrue(result is QrParseResult.Success)
        val success = result as QrParseResult.Success
        assertEquals("CV06A", success.equipmentId)
    }

    @Test
    fun parse_emptyOrInvalid_returnsInvalid() {
        val emptyResult = QrCodeParser.parse("")
        assertTrue(emptyResult is QrParseResult.Invalid)

        val nullResult = QrCodeParser.parse(null)
        assertTrue(nullResult is QrParseResult.Invalid)

        val badResult = QrCodeParser.parse("?")
        assertTrue(badResult is QrParseResult.Invalid)
    }
}
