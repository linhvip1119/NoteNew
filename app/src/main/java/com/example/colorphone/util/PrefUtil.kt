package com.example.colorphone.util

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.example.colorphone.model.EmailUser
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefUtil
@Inject
constructor(
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
) {

    var isShowOpenAdsWhenChangeMode: Boolean
        get() = sharedPreferences.getBoolean("isShowOpenAdsWhenChangeMode", true)
        set(value) {
            editor.putBoolean("isShowOpenAdsWhenChangeMode", value).commit()
        }

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
        get() = sharedPreferences.getString("typeView", TypeView.Grid.value) ?: TypeView.Grid.value
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
    var statusNotificationBar: Boolean
        get() = sharedPreferences.getBoolean("statusNotificationBar", true)
        set(value) {
            editor.putBoolean("statusNotificationBar", value).commit()
        }

    var statusEmailUser: EmailUser?
        get() = Gson().fromJson(
            sharedPreferences.getString("statusEmailUser", null),
            EmailUser::class.java
        )
        set(value) {
            val model = Gson().toJson(value)
            editor.putString("statusEmailUser", model).commit()
        }

    var lastSync: Long
        get() = sharedPreferences.getLong("lastSync", 0)
        set(value) {
            editor.putLong("lastSync", value).commit()
        }

    var languageApp: String?
        get() = sharedPreferences.getString("languageApp", "")
        set(value) {
            editor.putString("languageApp", value).commit()
        }

    var newIdNoteWidget: Int
        get() = sharedPreferences.getInt("newIdNoteWidget", -1)
        set(value) {
            editor.putInt("newIdNoteWidget", value).commit()
        }

    fun setIdWidgetNote(idNote: Int, value: Int) {
        editor.putInt("${idNote}_save", value)
        editor.apply()
    }

    fun getIdWidgetNote(idNote: Int): Int {
        return sharedPreferences.getInt("${idNote}_save", -1)
    }

}