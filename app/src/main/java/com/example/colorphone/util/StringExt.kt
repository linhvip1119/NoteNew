package com.example.colorphone.util

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.colorphone.R
import com.example.colorphone.model.EmailUser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun String.isNotNullOfEmpty() = this != "null" && this.isNotEmpty()

fun parseListTypeNoteToJson(list: List<String>): String {
    return Gson().toJson(list)
}

fun parseUserEmailToJson(user: EmailUser): String {
    return Gson().toJson(user)
}

fun parseJsonToUserEmail(string: String): EmailUser? {
    return Gson().fromJson(string, EmailUser::class.java)
}

fun ImageView.loadImageUser(url: String) {
    Glide.with(this)
        .load(url) // image url
        .placeholder(R.drawable.icons_google_svg) // any placeholder to load at start
//        .error(R.drawable.imagenotfound)  // any image in case of error
//        .override(200, 200) // resizing
        .centerCrop()
        .into(this)
}

fun String.getListTypeNote(): List<String> {
    val typeToken = object : TypeToken<List<String>>() {}.type
    return Gson().fromJson(this, typeToken)
}

fun Int.formatTime(): String = if (this.toString().length < 2) "0$this" else this.toString()

fun TextView.makeHighLightText(subStringToColorize: String, @ColorRes colorResId: Int) {
    val builder = SpannableStringBuilder()

    val startIndex = text.indexOf(subStringToColorize, startIndex = 0, ignoreCase = false)
    val endIndex = startIndex + subStringToColorize.length

    val spannableString = SpannableString(text)
    val boldSpan = StyleSpan(Typeface.BOLD)
    val colorDrawable = ContextCompat.getColor(context, colorResId)

    if (startIndex != -1) {
        spannableString.setSpan(
            boldSpan,
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            ForegroundColorSpan(colorDrawable),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        ) //To change color of text

        builder.append(spannableString)
        setText(builder, TextView.BufferType.SPANNABLE)
    }
}

fun Context.getTextLanguage(name: String): String {
    return when (name) {
        getString(R.string.english) -> getString(R.string.english)
        getString(R.string.spanishKey) -> getString(R.string.espanol)
        getString(R.string.frenchKey) -> getString(R.string.francais)
        getString(R.string.koreanKey) -> getString(R.string.koreanText)
        getString(R.string.japanKey) -> getString(R.string.japanText)
        getString(R.string.portugueseKey) -> getString(R.string.portugueseText)
        getString(R.string.germanKey) -> getString(R.string.germanText)
        getString(R.string.turkishKey) -> getString(R.string.turkishText)
        getString(R.string.indoKey) -> getString(R.string.indoText)
        getString(R.string.thaiKey) -> getString(R.string.thaiText)
        else -> getString(R.string.english)
    }
}