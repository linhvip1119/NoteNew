package com.example.colorphone.setup

import com.example.colorphone.BuildConfig
import dev.keego.haki.preference.RemotePreferences

object AppPreferences : RemotePreferences(
    name = "app_preferences",
    BuildConfig.VERSION_NAME,
    BuildConfig.DEBUG,
) {
    var haki_timebase by intPref(
        "haki_timebase",
        defaultValue = 20000,
    )
}