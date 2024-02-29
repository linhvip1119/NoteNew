package com.example.colorphone.util.ext

import android.util.Log
import com.example.colorphone.util.Const.FORMAT_DATE_NOTE
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun getCurrentDate(): String =
    SimpleDateFormat(FORMAT_DATE_NOTE, Locale.getDefault()).format(Calendar.getInstance().time)

fun getCurrentTimeToLong(): Long {
    return System.currentTimeMillis()
}

fun convertLongToDateYYMMDD(dateLong: Long): String {
    val date = Date(dateLong)
    val format = SimpleDateFormat(FORMAT_DATE_NOTE, Locale.getDefault())
    return format.format(date)
}

fun convertLongToDate(dateLong: Long, format: String): String? {
    val date = Date(dateLong)
    val mFormat = SimpleDateFormat(format, Locale.getDefault())
    return mFormat.format(date)
}

fun convertDateStringToLong(time: String, pattern: String): Long? {
    return try {
        val date = SimpleDateFormat(pattern).parse(time)
        date?.time
    } catch (e: Exception) {
        logException(e)
        0
    }
}

fun convertYYYYMMTODateMMMYYYY(date: String, patternOld: String, patternNew: String): String {
    val format = SimpleDateFormat(patternOld, Locale.getDefault()).parse(date)
    return SimpleDateFormat(patternNew, Locale.getDefault()).format(format as Date).toString()
}

fun logException(e: Any) {
    Log.i("error", "formatDateStr: $e")
}