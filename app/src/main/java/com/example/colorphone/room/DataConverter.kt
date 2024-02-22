package com.example.colorphone.room

import android.util.Log
import androidx.room.TypeConverter
import com.example.colorphone.model.CheckList
import com.example.colorphone.model.NoteModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class DataConverter {
    @TypeConverter
    fun fromCountryLangList(countryLang: List<CheckList?>?): String? {
        if (countryLang == null) {
            return null
        }
        return try {
            val gson = Gson()
            val type = object : TypeToken<List<CheckList?>?>() {}.type
            gson.toJson(countryLang, type)
        } catch (e: Exception) {
            Log.i("error", "fromCountryLangList: $e")
            null
        }
    }

    @TypeConverter
    fun toCountryLangList(countryLangString: String?): ArrayList<CheckList>? {
        if (countryLangString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<CheckList?>?>() {}.type
        return gson.fromJson<ArrayList<CheckList>>(countryLangString, type)
    }

    fun toListNote(countryLangString: String?): ArrayList<NoteModel>? {
        if (countryLangString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<NoteModel?>?>() {}.type
        return gson.fromJson<ArrayList<NoteModel>>(countryLangString, type)
    }

    fun fromListNote(countryLang: ArrayList<NoteModel?>?): String? {
        if (countryLang == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<ArrayList<NoteModel?>?>() {}.type
        return gson.toJson(countryLang, type)
    }
}