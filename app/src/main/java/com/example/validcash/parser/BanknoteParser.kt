package com.example.validcash.parser

import com.example.validcash.model.BanknoteData

object BanknoteParser {
    // Prioridad: Número seguido de Letra (ej: 00000000 B) o viceversa (ej: B 00000000)
    // Se usa {8,} para capturar números de serie de 8 o más dígitos
    private val combinedRegex1 = Regex("(\\d{8,})\\s+([A-Z])\\b")
    private val combinedRegex2 = Regex("\\b([A-Z])\\s+(\\d{8,})")
    
    private val valorRegex = Regex("\\b(10|20|50|100|200)\\b")
    private val serieRegex = Regex("\\b[A-Z]\\b")
    private val numeroSerieRegex = Regex("\\d{8,}")

    fun parse(text: String): BanknoteData {
        var serie = ""
        var valor = ""
        var numeroSerie = ""

        // 1. Intentar encontrar patrones combinados por proximidad (Serie + Número)
        val match1 = combinedRegex1.find(text)
        val match2 = combinedRegex2.find(text)

        when {
            match1 != null -> {
                numeroSerie = match1.groupValues[1]
                serie = match1.groupValues[2]
            }
            match2 != null -> {
                serie = match2.groupValues[1]
                numeroSerie = match2.groupValues[2]
            }
            else -> {
                // Fallback: Buscar por separado si no se detectan juntos en la misma línea
                val numeroMatch = numeroSerieRegex.find(text)
                if (numeroMatch != null) numeroSerie = numeroMatch.value

                val serieMatch = serieRegex.find(text)
                if (serieMatch != null) serie = serieMatch.value
            }
        }

        // 2. Buscar el valor (Denominación) de forma independiente
        val valorMatch = valorRegex.find(text)
        if (valorMatch != null) {
            valor = valorMatch.value
        }

        return BanknoteData(serie, valor, numeroSerie)
    }
}
