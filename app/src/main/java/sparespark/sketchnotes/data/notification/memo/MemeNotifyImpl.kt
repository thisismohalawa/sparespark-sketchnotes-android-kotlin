package sparespark.sketchnotes.data.notification.memo

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import sparespark.sketchnotes.R
import sparespark.sketchnotes.core.result.DataResult
import sparespark.sketchnotes.data.notification.base.BaseNotifyBuilder
import sparespark.sketchnotes.notes.NoteActivity

private const val CHANNEL_ID_SHARED = "SharedMemo"
private const val CHANNEL_NAME_SHARED = "SharedMemo"
private const val SHARED_ID = 1

class MemeNotifyImpl(
    val context: Context
) : BaseNotifyBuilder, MemoNotify {

    override fun showSharedMemeNotification(title: String?, content: String?) =
        DataResult.build {
            val nBuilder = getBasicNotificationBuilder(
                context,
                CHANNEL_ID_SHARED, false
            )
            nBuilder.setContentTitle(title ?: "Paper Planner")
                .setContentText(content ?: "Someone shared a new memo with you.")
                .setContentIntent(
                    getPendingIntentWithStack(
                        context, NoteActivity::
                        class.java
                    )
                )
            val nManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(
                CHANNEL_ID_SHARED, CHANNEL_NAME_SHARED, true
            )
            nManager.notify(SHARED_ID, nBuilder.build())
        }

    override fun getBasicNotificationBuilder(
        context: Context,
        channelId: String,
        playSound: Boolean
    ): NotificationCompat.Builder {
        val notificationSound: Uri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val nBuilder =
            NotificationCompat.Builder(context, channelId).setSmallIcon(R.drawable.ic_pending)
                .setAutoCancel(true).setDefaults(0)
        if (playSound) nBuilder.setSound(notificationSound)
        return nBuilder
    }

    override fun <T> getPendingIntentWithStack(
        context: Context,
        javaClass: Class<T>
    ): PendingIntent {
        val resultIntent = Intent(context, javaClass)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(javaClass)
        stackBuilder.addNextIntent(resultIntent)

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @TargetApi(26)
    override fun NotificationManager.createNotificationChannel(
        channelID: String,
        channelName: String,
        playSound: Boolean
    ) {
        val channelImportance = if (playSound) NotificationManager.IMPORTANCE_DEFAULT
        else NotificationManager.IMPORTANCE_LOW
        val nChannel = NotificationChannel(channelID, channelName, channelImportance)
        nChannel.enableLights(true)
        nChannel.lightColor = Color.BLUE
        this.createNotificationChannel(nChannel)
    }

}
