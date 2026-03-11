package com.example.validcash.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.validcash.utils.PreferencesManager
import com.example.validcash.utils.SoundManager

class OnboardingViewModel : ViewModel() {
    lateinit var preferencesManager: PreferencesManager
        private set
    
    lateinit var soundManager: SoundManager
        private set
    
    fun init(context: Context) {
        preferencesManager = PreferencesManager(context)
        soundManager = SoundManager(context)
        soundManager.initPreferences(preferencesManager)
    }
    
    fun isOnboardingCompleted(): Boolean {
        return preferencesManager.isOnboardingCompleted
    }
    
    fun completeOnboarding() {
        preferencesManager.isOnboardingCompleted = true
    }
    
    fun toggleSound(): Boolean {
        return preferencesManager.toggleSound()
    }
    
    fun isSoundEnabled(): Boolean {
        return preferencesManager.isSoundEnabled
    }
    
    override fun onCleared() {
        super.onCleared()
        if (::soundManager.isInitialized) {
            soundManager.release()
        }
    }
}

