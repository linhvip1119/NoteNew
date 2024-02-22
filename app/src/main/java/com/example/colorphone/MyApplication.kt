package com.example.colorphone

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.example.colorphone.util.Const.CHANNEL_ID_ONE_TIME_WORK
import com.example.colorphone.util.Const.CHANNEL_ID_PERIOD_WORK
import com.example.colorphone.util.PrefUtil
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication :Application() {

    @Inject
    lateinit var prefUtil: PrefUtil
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        if (prefUtil.isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        prefUtil.isDarkThemeLD.observeForever { isDarkMode ->
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channelPeriodic =
                NotificationChannel(CHANNEL_ID_PERIOD_WORK, "Period Work Request", importance)
            channelPeriodic.description = "Periodic Note"
            val channelInstant =
                NotificationChannel(CHANNEL_ID_ONE_TIME_WORK, "One Time Work Request", importance)
            channelInstant.description = "One Time Note"
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = applicationContext.getSystemService(
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(channelPeriodic)
            notificationManager.createNotificationChannel(channelInstant)
        }
    }
}