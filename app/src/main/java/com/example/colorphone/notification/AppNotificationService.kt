package com.example.colorphone.notification

import android.annotation.SuppressLint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.keego.brokenscreen.notification.NotificationHelper

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class AppNotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null) {
            NotificationHelper.showNotification(
                applicationContext,
                remoteMessage.notification!!.title,
                remoteMessage.notification!!.body
            )
        }
    }
}