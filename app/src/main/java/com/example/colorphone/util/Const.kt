package com.example.colorphone.util

object Const {
    const val CHANNEL_ID_PERIOD_WORK = "PERIODIC_APP_UPDATES"
    const val CHANNEL_ID_ONE_TIME_WORK = "INSTANT_APP_UPDATES"

    //type
    const val TYPE_NOTE = "TYPE_NOTE"
    const val TYPE_CHECKLIST = "TYPE_CHECKLIST"

    //config
    const val APPLICATION_ID = "iNote.note.notepad.stickynote"

    const val FORMAT_DATE_NOTE = "dd-MM-yyyy | HH:mm"
    const val FORMAT_DATE_REMINDER = "yyyy-MM-dd"
    const val FORMAT_DATE_REMINDER_TIME = "HH:mm"
    const val FORMAT_DATE_UPDATE = "yyyy-MM-dd HH:mm"

    var currentType: String? = TypeColorNote.DEFAULT.name

    //key bundle
    const val KEY_RELOAD_DATA_TEXT = "KEY_RELOAD_DATA_TEXT"
    const val KEY_ID_DATA_NOTE = "KEY_ID_DATA_NOTE"
    const val TYPE_ITEM_EDIT = "TYPE_ITEM_EDIT"
}