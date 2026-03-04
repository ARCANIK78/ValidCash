package com.example.validcash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.validcash.model.BanknoteData
import com.example.validcash.parser.BanknoteParser

class MainViewModel : ViewModel() {
    var banknoteData by mutableStateOf(BanknoteData())
        private set

    fun onTextDetected(text: String) {
        val detectedData = BanknoteParser.parse(text)
        if (detectedData.isValid) {
            banknoteData = detectedData
        }
    }
}
