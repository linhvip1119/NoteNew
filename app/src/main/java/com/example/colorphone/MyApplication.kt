package com.example.colorphone

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.example.colorphone.adsConfig.AdsConstants
import com.example.colorphone.adsConfig.AppOpenManager
import com.example.colorphone.model.RemoteConfigAds
import com.example.colorphone.util.Const
import com.example.colorphone.util.Const.CHANNEL_ID_ONE_TIME_WORK
import com.example.colorphone.util.Const.CHANNEL_ID_PERIOD_WORK
import com.example.colorphone.util.PrefUtil
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication :Application() {

    @Inject
    lateinit var prefUtil: PrefUtil
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        getRemoteConfigAds()
        Const.mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
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

    private fun getRemoteConfigAds() {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    try {
                        getListRemoteConfigAds(remoteConfig)
                        getTimeBase(remoteConfig)
                    } catch (_: Exception) {
                    }
                }
            }
    }

    private fun getTimeBase(remoteConfig: FirebaseRemoteConfig) {
        val timebase = remoteConfig.getLong("ads_timebase")
        if (timebase > 0) {
            AdsConstants.TIME_BASE_DEFAULT = timebase
        }
    }

    private fun getListRemoteConfigAds(remoteConfig: FirebaseRemoteConfig) {
        val result = remoteConfig.getString("ads_state")
        val gson = Gson()
        val jsonRemoteConfigs = gson.fromJson<List<RemoteConfigAds>>(result, object : TypeToken<List<RemoteConfigAds>>() {}.type)
        jsonRemoteConfigs?.let {
            it.forEach { remoteConfigAds ->
                AdsConstants.mapRemoteConfigAds[remoteConfigAds.spaceName] = remoteConfigAds
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