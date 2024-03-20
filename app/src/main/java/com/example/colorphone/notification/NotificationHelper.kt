package com.keego.brokenscreen.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.colorphone.R
import com.example.colorphone.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService


object NotificationHelper {

    fun showNotification(
        context: Context, title: String?, message: String?
    ) {
        try {

            val contentIntent = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java).apply {

                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }, PendingIntent.FLAG_IMMUTABLE
            )

            val notificationLayout = RemoteViews(
                context.packageName,
                R.layout.layout_notification
            ).apply {
                this.setTextViewText(R.id.title, title)
                this.setTextViewText(R.id.content, message)
            }
            val notificationLayoutSmall = RemoteViews(
                context.packageName,
                R.layout.layout_notification
            ).apply {
                this.setTextViewText(R.id.title, title)
                this.setTextViewText(R.id.content, message)
            }
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(
                context, context.getString(R.string.notification_channel_id)
            )
                .setCustomBigContentView(notificationLayout)
                //.setCustomContentView(notificationLayoutSmall)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(message)
                .setAutoCancel(true).setOnlyAlertOnce(true).setContentIntent(contentIntent)


            val notificationManager =
                context.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE) as NotificationManager

            // Check if the Android Version is greater than Oreo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(
                    NotificationChannel(
                        context.getString(R.string.notification_channel_id),
                        "ai voice changer",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                )
            }
            with(NotificationManagerCompat.from(context)) {
                // notificationId is a unique int for each notification that you must define
                if (ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d("Notification", "Notification denied")
                    return
                }
                Log.d("Notification", "Notification sent")
                notify(0, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}