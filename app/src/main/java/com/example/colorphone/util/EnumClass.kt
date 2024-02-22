package com.example.colorphone.util

enum class TypeColorNote {
    F_PRIMARY, DEFAULT,
    D_RED, B_GREEN, A_ORANGE, GRAY, C_BLUE, BLINK
}

enum class TypeItem { TEXT, CHECK_LIST }

enum class ThemeMode { LIGHT, DARK, SYSTEM }

enum class TypeClick {
    CLICK_SELECTED,
    CLICK_CHANGE_COLOR_ITEM
}

enum class SortType {
    MODIFIED_TIME,
    CREATE_TIME,
    REMINDER_TIME,
    COLOR
}

enum class RepeatType(val mName: String, val value: Long) {
    DOES_NOT_REPEAT("None", 0),
    DAILY("Daily", 1440),
    WEEKLY("Weekly", 10080),
    MONTHLY("Monthly", 43200),
    CUSTOM("Custom", 0),
    MORE_DAYS("day(s)", 1440),
    MORE_WEEKS("week(s)", 10080),
    MORE_MONTH("month(s)", 43200)
}

enum class TypeView(val value: String, val type: Int) {
    List("List", 1), Grid("Grid", 2), Details("Details", 3)
}