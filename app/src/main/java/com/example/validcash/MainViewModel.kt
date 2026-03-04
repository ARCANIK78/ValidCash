package com.example.validcash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var textoDetectado by mutableStateOf("")
        private set

    fun onTextDetected(text: String) {
        // Expresión regular para buscar números de 9 o más dígitos
        val regex = Regex("\\d{9,}")
        val match = regex.find(text)
        if (match != null) {
            textoDetectado = "Dígito leído: ${match.value}"
        }
    }
}
