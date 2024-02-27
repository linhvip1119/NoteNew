package com.example.colorphone.ui.reminder.workHelper

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.colorphone.R
import com.example.colorphone.model.NoteModel
import com.example.colorphone.util.Const
import com.example.colorphone.util.Const.ID
import com.example.colorphone.util.Const.MESSAGE
import com.example.colorphone.util.Const.TITTLE
import com.example.colorphone.util.Const.TYPE_ITEM
import com.example.colorphone.util.Const.TYPE_WORKER

class ReminderWorkerNotification(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        return try {
            if (Const.notificationOn) {
                showNotification()
                return Result.success()
            } else Result.failure()
        } catch (exception: Exception) {
            Log.e(LOG_REMINDER, "Error push notification failed", exception)
            Result.failure()
        }
    }

    private fun setMyAction(context: Context?, noteModel: NoteModel): PendingIntent? {
        val myPendingIntent = context?.let {
            val desFragment = R.id.editFragment
            NavDeepLinkBuilder(it)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(
                        desFragment,
                        bundleOf(
                            "ARG_CREATE_NOTE" to false,
                            "ARG_FROM_WIDGET" to true,
                            "KEY_IDS_NOTE_FROM_WIDGET" to noteModel.ids
                        )
                    )
                    .createPendingIntent()
        }
        return myPendingIntent
    }

    private fun showNotification() {
        val ids = inputData.getInt(ID, 0)
        val tittle = inputData.getString(TITTLE) ?: ""
        val content = inputData.getString(MESSAGE) ?: ""
        val typeItem = inputData.getString(TYPE_ITEM) ?: ""
        val note =
            NoteModel(
                ids = ids,
                title = tittle,
                content = content,
                typeItem = typeItem
            )

        val icon = BitmapFactory.decodeResource(context.resources, R.drawable.image_text)
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TITLE_COLUMN_INDEX)
        val vibrate = longArrayOf(0, 100, 200, 300)
        val notification =
            NotificationCompat.Builder(context, inputData.getString(TYPE_WORKER) ?: "").apply {
                setContentIntent(setMyAction(context, note))
                setSmallIcon(R.drawable.ic_reminder_25dp)
                setLargeIcon(icon)
                setContentTitle(note.title)
                setContentText(note.content)
                priority = NotificationCompat.PRIORITY_HIGH
                setCategory(NotificationCompat.CATEGORY_ALARM)
                setSound(sound)
                setVibrate(vibrate)
                setAutoCancel(true)
            }


        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            note.ids?.let { notify(it, notification.build()) }
        }
    }

    companion object {
        private const val LOG_REMINDER = "ReminderWorker"
    }

}