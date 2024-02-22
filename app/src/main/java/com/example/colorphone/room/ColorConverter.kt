package com.example.colorphone.room

import androidx.room.TypeConverter
import com.example.colorphone.model.ColorItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class ColorConverter {

    @TypeConverter
    fun fromColorList(countryLang: List<ColorItem?>?): String? {
        if (countryLang == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<ColorItem?>?>() {}.type
        return gson.toJson(countryLang, type)
    }

    @TypeConverter
    fun toColorList(countryLangString: String?): ArrayList<ColorItem>? {
        if (countryLangString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<ArrayList<ColorItem?>?>() {}.type
        return gson.fromJson<ArrayList<ColorItem>>(countryLangString, type)
    }
}