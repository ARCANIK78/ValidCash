package com.example.validcash.model

data class BanknoteData(
    val serie: String = "",
    val valor: String = "",
    val numeroSerie: String = ""
) {
    val isValid: Boolean get() = serie.isNotBlank() && valor.isNotBlank() && numeroSerie.isNotBlank()
}
