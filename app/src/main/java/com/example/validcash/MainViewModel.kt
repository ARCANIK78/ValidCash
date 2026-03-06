package com.example.validcash

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.validcash.model.BanknoteData
import com.example.validcash.parser.BanknoteParser

class MainViewModel : ViewModel() {
    var banknoteData by mutableStateOf(BanknoteData())
        private set

    fun onTextDetected(text: String, context: Context) {
        val detectedData = BanknoteParser.parse(text)
        if (detectedData.isValid) {
            banknoteData = detectedData
            // Clear cache after successful analysis
            clearCache(context)
        }
    }

    private fun clearCache(context: Context) {
        try {
            val cacheDir = context.cacheDir
            deleteDir(cacheDir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: java.io.File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list() ?: return false
            for (i in children.indices) {
                val success = deleteDir(java.io.File(dir, children[i]))
                if (!success) return false
            }
            return dir.delete()
        } else if (dir != null && dir.isFile) {
            return dir.delete()
        } else {
            return false
        }
    }
}

