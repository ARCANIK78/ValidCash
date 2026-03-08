package com.example.validcash.validator

import com.example.validcash.model.BanknoteData

object BanknoteValidator {

    // Rangos de números de serie B no válidos para Bs10
    private val invalidRanges10 = listOf(
        67250001L..67700000L,
        69050001L..71300000L, // Combinado (69050001 a 71300000)
        76310012L..85139995L,
        86400001L..86850000L,
        90900001L..91350000L,
        91800001L..92250000L
    )

    // Rangos de números de serie B no válidos para Bs20
    private val invalidRanges20 = listOf(
        87280145L..91646549L,
        96650001L..97100000L,
        99800001L..100700000L, // Combinado (99800001 a 100700000)
        109250001L..109700000L,
        110600001L..111500000L, // Combinado (110600001 a 111500000)
        111950001L..113300000L, // Combinado (111950001 a 113300000)
        114200001L..115550000L, // Combinado (114200001 a 115550000)
        118700001L..119600000L, // Combinado (118700001 a 119600000)
        120500001L..120950000L
    )

    // Rangos de números de serie B no válidos para Bs50
    private val invalidRanges50 = listOf(
        67250001L..67700000L,
        69050001L..71300000L, // Combinado (69050001 a 71300000)
        76310012L..85139995L,
        86400001L..86850000L,
        90900001L..91350000L,
        91800001L..92250000L
    )

    fun isGenuine(banknote: BanknoteData): Boolean {
        if (!banknote.isValid) return true 
        if (banknote.serie != "B") return true 

        val numero = banknote.numeroSerie.toLongOrNull() ?: return true
        val valor = banknote.valor.toIntOrNull() ?: return true

        return when (valor) {
            10 -> invalidRanges10.none { numero in it }
            20 -> invalidRanges20.none { numero in it }
            50 -> invalidRanges50.none { numero in it }
            else -> true
        }
    }
}
