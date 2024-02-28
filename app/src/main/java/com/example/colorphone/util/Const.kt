package com.example.colorphone.util

object Const {
    const val CHANNEL_ID_PERIOD_WORK = "PERIODIC_APP_UPDATES"
    const val CHANNEL_ID_ONE_TIME_WORK = "INSTANT_APP_UPDATES"

    //type
    const val TYPE_NOTE = "TYPE_NOTE"
    const val TYPE_CHECKLIST = "TYPE_CHECKLIST"

    const val EDIT_NOTE_SCREEN = "BASE_EDIT_NOTE_SCREEN"
    const val TEXT_SCREEN = "TEXT_SCREEN"
    const val CHECK_LIST_SCREEN = "CHECK_LIST_SCREEN"
    const val SELECTED_SCREEN = "SELECTED_SCREEN"
    const val SETTING_SCREEN = "SETTING_SCREEN"

    //config
    const val APPLICATION_ID = "iNote.note.notepad.stickynote"

    const val FORMAT_DATE_NOTE = "dd-MM-yyyy | HH:mm"
    const val FORMAT_DATE_REMINDER = "yyyy-MM-dd"
    const val FORMAT_DATE_REMINDER_TIME = "HH:mm"
    const val FORMAT_DATE_UPDATE = "yyyy-MM-dd HH:mm"

    var currentType: String = TypeColorNote.DEFAULT.name

    var notificationOn = true

    //key bundle
    const val KEY_RELOAD_DATA_TEXT = "KEY_RELOAD_DATA_TEXT"
    const val KEY_ID_DATA_NOTE = "KEY_ID_DATA_NOTE"
    const val TYPE_ITEM_EDIT = "TYPE_ITEM_EDIT"
    const val CURRENT_TYPE_ITEM = "CURRENT_TYPE_ITEM"
    const val NOTE_FROM_LONG_CLICK = "NOTE_FROM_LONG_CLICK"
    const val POSITION_SELECTED = "POSITION_SELECTED"
    const val KEY_SCREEN_RECYCLER_BIN = "KEY_SCREEN_RECYCLER_BIN"

    //key fragment listener
    const val KEY_FILTER_COLOR_NOTE = "KEY_FILTER_COLOR_NOTE"

    //reminder
    const val ID = "ID"
    const val TITTLE = "TITTLE_ALARM"
    const val MESSAGE = "MESSAGE_ALARM"
    const val TYPE_WORKER = "TYPE_WORKER"
    const val TYPE_ITEM = "TYPE_ITEM_WORKER"

    //email
    const val EMAIL_FEEDBACK = "contact@keego.dev"
}