package com.example.validcash.model

data class BanknoteData(
    val serie: String = "",
    val valor: String = "",
    val numeroSerie: String = ""
) {
    val isValid: Boolean get() = serie.isNotBlank() && valor.isNotBlank() && numeroSerie.isNotBlank()
    
    // Valores que tienen validación: 10, 20, 50
    val isValidatable: Boolean get() = valor in listOf("10", "20", "50")
    
    // Valores que no tienen validación disponible: 100, 200
    val hasValidationAvailable: Boolean get() = valor in listOf("10", "20", "50")
}
