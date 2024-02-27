package com.example.colorphone.util

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefUtil
@Inject
constructor(
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
) {

    val isDarkThemeLD: MutableLiveData<Boolean> = MutableLiveData()

    fun putValueMode(value: Boolean) {
        isDarkThemeLD.postValue(value)
    }

    var isDarkMode: Boolean
        get() = sharedPreferences.getBoolean("isDarkMode", false)
        set(value) {
            editor.putBoolean("isDarkMode", value).commit()
        }

    var themeColor: String
        get() = sharedPreferences.getString("themeColor", TypeColorNote.BLUE.name) ?: TypeColorNote.BLUE.name
        set(value) {
            editor.putString("themeColor", value).commit()
        }

    var isLoggedIn: Boolean
        get() = sharedPreferences.getBoolean("isLoggedIn", false)
        set(value) {
            editor.putBoolean("isLoggedIn", value).commit()
        }

    var typeView: String
        get() = sharedPreferences.getString("typeView", TypeView.Details.value) ?: TypeView.Details.value
        set(value) {
            editor.putString("typeView", value).commit()
        }

    var sortType: String?
        get() = sharedPreferences.getString("sortType", SortType.MODIFIED_TIME.name)
        set(value) {
            editor.putString("sortType", value).commit()
        }

    var pin: String?
        get() = sharedPreferences.getString("pinzz", "")
        set(value) {
            editor.putString("pinzz", value).commit()
        }

    var email: String?
        get() = sharedPreferences.getString("email", "")
        set(value) {
            editor.putString("email", value).commit()
        }

}