package com.example.validcash.parser

import com.example.validcash.model.BanknoteData

object BanknoteParser {
    // Billetes bolivianos: 10, 20, 50, 100, 200 Bs
    
    // Número de serie (8-10 dígitos) - mejorado para capturar ceros a la izquierda
    // Usa lookahead/lookbehind en lugar de \b para mejor compatibilidad con ceros iniciales
    private val numeroSerieRegex = Regex("(?<![\\d])(0*\\d{8,10})(?![\\d])")
    
    // Serie (letra única A-Z, buscada después del número de serie)
    private val serieRegex = Regex("\\b([A-Z])\\b")
    
    // Valor del billete
    private val valorRegexBs = Regex("\\b[Bb][Ss]\\.?\\s*(\\d+)\\b")
    private val valorRegexNum = Regex("\\b(10|20|50|100|200)\\b")

    /**
     * Corrige errores comunes de OCR en números de serie
     * - "O" (letra O mayúscula) seguida de dígitos → "0"
     * - Dígitos seguidos de "O" → "0"
     */
    private fun correctOcrErrors(text: String): String {
        var corrected = text
        
        // Reemplazar letra O mayúscula por cero en contextos numéricos
        // "O12345678" → "012345678"
        corrected = corrected.replace(Regex("O(?=\\d)"), "0")
        // "12345678O" → "123456780"
        corrected = corrected.replace(Regex("(?<=\\d)O"), "0")
        
        return corrected
    }

    fun parse(text: String): BanknoteData {
        var serie = ""
        var valor = ""
        var numeroSerie = ""
        
        // Primero corregir errores comunes de OCR
        val correctedText = correctOcrErrors(text)
        val normalizedText = correctedText.replace(Regex("\\s+"), " ").trim()
        
        // Buscar número de serie primero
        val numeroMatch = numeroSerieRegex.find(normalizedText)
        if (numeroMatch != null) {
            numeroSerie = numeroMatch.groupValues[1]
            
            // Buscar letra de serie DESPUÉS del número de serie en el texto
            val afterNumero = normalizedText.substring(numeroMatch.range.last + 1)
            val serieMatch = serieRegex.find(afterNumero)
            if (serieMatch != null) {
                serie = serieMatch.groupValues[1].uppercase()
            }
        }

        // Buscar valor
        val valorBsMatch = valorRegexBs.find(normalizedText)
        if (valorBsMatch != null) {
            val detectedValue = valorBsMatch.groupValues[1]
            if (detectedValue in listOf("10", "20", "50", "100", "200")) {
                valor = detectedValue
            }
        }
        
        if (valor.isEmpty()) {
            val valorMatch = valorRegexNum.find(normalizedText)
            if (valorMatch != null) {
                valor = valorMatch.value
            }
        }
        
        // Corregir errores comunes de OCR: I/T/1
        if (serie == "T" || serie == "1") {
            serie = "I"
        }

        return BanknoteData(serie, valor, numeroSerie)
    }
}

