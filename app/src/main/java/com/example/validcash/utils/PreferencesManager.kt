package com.example.validcash.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "validcash_prefs"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    // Sound preference
    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true) // Default: sound ON
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    // Onboarding preference
    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    fun toggleSound(): Boolean {
        isSoundEnabled = !isSoundEnabled
        return isSoundEnabled
    }
}

