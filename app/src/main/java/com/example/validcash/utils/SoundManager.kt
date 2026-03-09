package com.example.validcash.utils

import android.content.Context
import android.media.MediaPlayer
import com.example.validcash.R
import kotlinx.coroutines.*

class SoundManager(private val context: Context) {
    
    private var successPlayer: MediaPlayer? = null
    private var errorPlayer: MediaPlayer? = null
    private var soundScope: CoroutineScope? = null
    
    // Jobs para controlar la reproducción actual
    private var currentSoundJob: Job? = null
    
    // Estado para evitar reproducir el mismo sonido múltiples veces
    private var lastPlayedType: String = ""
    private var lastPlayedTime: Long = 0
    
    init {
        soundScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        initPlayers()
    }
    
    private fun initPlayers() {
        // Inicializar MediaPlayers
        successPlayer = MediaPlayer.create(context, R.raw.google_pay_success)?.apply {
            isLooping = false
            setVolume(1.0f, 1.0f)
        }
        
        errorPlayer = MediaPlayer.create(context, R.raw.error_cdoxcym)?.apply {
            isLooping = false
            setVolume(1.0f, 1.0f)
        }
    }
    
    /**
     * Reproduce el sonido de éxito 3 veces en 1 segundo
     * Solo reproduce si ha pasado suficiente tiempo desde la última reproducción
     */
    fun playSuccessSound() {
        val currentTime = System.currentTimeMillis()
        
        // Evitar reproducir el mismo sonido en menos de 2 segundos
        if (lastPlayedType == "success" && currentTime - lastPlayedTime < 2000) {
            return
        }
        
        lastPlayedType = "success"
        lastPlayedTime = currentTime
        
        currentSoundJob?.cancel()
        currentSoundJob = soundScope?.launch {
            repeat(3) { index ->
                val player = MediaPlayer.create(context, R.raw.google_pay_success)
                player?.start()
                player?.setOnCompletionListener {
                    it.release()
                }
                
                if (index < 2) { // No esperar después del último
                    delay(333L) // 333ms entre cada reproducción = ~1 segundo para 3
                }
            }
        }
    }
    
    /**
     * Reproduce el sonido de error 3 veces en 1 segundo
     * Solo reproduce si ha pasado suficiente tiempo desde la última reproducción
     */
    fun playErrorSound() {
        val currentTime = System.currentTimeMillis()
        
        // Evitar reproducir el mismo sonido en menos de 2 segundos
        if (lastPlayedType == "error" && currentTime - lastPlayedTime < 2000) {
            return
        }
        
        lastPlayedType = "error"
        lastPlayedTime = currentTime
        
        currentSoundJob?.cancel()
        currentSoundJob = soundScope?.launch {
            repeat(3) { index ->
                val player = MediaPlayer.create(context, R.raw.error_cdoxcym)
                player?.start()
                player?.setOnCompletionListener {
                    it.release()
                }
                
                if (index < 2) { // No esperar después del último
                    delay(333L) // 333ms entre cada reproducción = ~1 segundo para 3
                }
            }
        }
    }
    
    /**
     * Reproduce un sonido específico
     * @param isSuccess true para sonido de éxito, false para sonido de error
     * @param times número de veces a reproducir
     * @param intervalMs intervalo entre reproducciones en milisegundos
     */
    fun playSound(isSuccess: Boolean, times: Int = 3, intervalMs: Long = 333L) {
        if (isSuccess) {
            playSuccessSound()
        } else {
            playErrorSound()
        }
    }
    
    /**
     * Libera los recursos
     */
    fun release() {
        currentSoundJob?.cancel()
        soundScope?.cancel()
        
        successPlayer?.release()
        errorPlayer?.release()
        
        successPlayer = null
        errorPlayer = null
        soundScope = null
    }
}

