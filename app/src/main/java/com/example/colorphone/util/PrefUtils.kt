package com.example.colorphone.util

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

object PrefUtils {
    private const val APP_PREFERENCES_KEY = "CLEAN_PREFERENCES"
    private var sharedPreferences: SharedPreferences? = null
    private fun getSharedPreferences(context: Context): SharedPreferences? {
        if (sharedPreferences == null) {
            sharedPreferences = context.applicationContext.getSharedPreferences(
                APP_PREFERENCES_KEY,
                Context.MODE_PRIVATE
            )
        }
        return sharedPreferences
    }

    fun languageApp(context: Context): String? {
        val appSharedPrefs = getSharedPreferences(context)
        return appSharedPrefs?.getString("language_current", Locale.getDefault().language)
    }

    fun languageApp(context: Context, value: String) {
        val appSharedPrefs = getSharedPreferences(context)
        val editor = appSharedPrefs?.edit()
        editor?.putString("language_current", value)
        editor?.apply()
    }

}