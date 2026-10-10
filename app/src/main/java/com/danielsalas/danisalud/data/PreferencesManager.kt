package com.danielsalas.danisalud.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("danisalud_prefs", Context.MODE_PRIVATE)

    var userName: String
        get() = prefs.getString("user_name", "DaniSalut") ?: "DaniSalut"
        set(value) = prefs.edit().putString("user_name", value).apply()

    var language: String
        get() = prefs.getString("language", "ca") ?: "ca"
        set(value) = prefs.edit().putString("language", value).apply()

    var darkMode: String
        get() = prefs.getString("dark_mode", "system") ?: "system"
        set(value) = prefs.edit().putString("dark_mode", value).apply()
}
