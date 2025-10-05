package com.example.lifeline

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PrefManager(context: Context) {
    private val PREF_NAME = "onboarding_pref"
    private val KEY_FIRST_TIME = "isFirstTime"

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isFirstTimeLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_TIME, true)
    }

    fun setFirstTimeLaunch(isFirstTime: Boolean) {
        prefs.edit { putBoolean(KEY_FIRST_TIME, isFirstTime) }
    }

    fun clearAll() {
        prefs.edit { clear() }
    }
}
