package com.example.validcash.validator

import com.example.validcash.model.BanknoteData

object BanknoteValidator {

    // Rangos de números de serie B no válidos para Bs10
    private val invalidRanges10 = listOf(
        67250001L..67700000L,
        69050001L..71300000L, // Rangos contiguos combinados
        76310012L..85139995L,
        86400001L..86850000L,
        90900001L..91350000L,
        91800001L..92250000L
    )

    // Rangos de números de serie B no válidos para Bs20
    private val invalidRanges20 = listOf(
        87280145L..91646549L,
        96650001L..97100000L,
        99800001L..100700000L, // Rangos contiguos combinados
        109250001L..109700000L,
        110600001L..111500000L, // Rangos contiguos combinados
        111950001L..113300000L, // Rangos contiguos combinados
        114200001L..115500000L, // Rangos contiguos combinados
        118700001L..119600000L, // Rangos contiguos combinados
        120500001L..120950000L
    )

    // Rangos de números de serie B no válidos para Bs50 (Corregido: Bs30 no existe)
    private val invalidRanges50 = listOf(
        77100001L..77550000L,
        78000001L..78450000L,
        78900001L..97250000L, // Rangos contiguos combinados
        98150001L..98600000L,
        104900001L..105800000L, // Rangos contiguos combinados
        106700001L..107150000L,
        107600001L..108500000L, // Rangos contiguos combinados
        109400001L..109850000L
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
            else -> true // 100 y 200 no tienen restricciones conocidas en serie B
        }
    }
}
