package com.example.validcash.parser

import com.example.validcash.model.BanknoteData

object BanknoteParser {
    // Billetes bolivianos: 10, 20, 50, 100, 200 Bs
    
    // Número de serie (8-10 dígitos)
    private val numeroSerieRegex = Regex("\\b(\\d{8,10})\\b")
    
    // Serie (letra única A-Z, buscada después del número de serie)
    private val serieRegex = Regex("\\b([A-Z])\\b")
    
    // Valor del billete
    private val valorRegexBs = Regex("\\b[Bb][Ss]\\.?\\s*(\\d+)\\b")
    private val valorRegexNum = Regex("\\b(10|20|50|100|200)\\b")

    fun parse(text: String): BanknoteData {
        var serie = ""
        var valor = ""
        var numeroSerie = ""
        
        val normalizedText = text.replace(Regex("\\s+"), " ").trim()
        
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

