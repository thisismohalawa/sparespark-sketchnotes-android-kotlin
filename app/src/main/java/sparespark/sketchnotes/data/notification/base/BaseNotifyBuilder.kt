package sparespark.sketchnotes.data.notification.base

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat

interface BaseNotifyBuilder {
    fun getBasicNotificationBuilder(
        context: Context, channelId: String, playSound: Boolean
    ): NotificationCompat.Builder
    fun <T> getPendingIntentWithStack(
        context: Context, javaClass: Class<T>
    ): PendingIntent
    fun NotificationManager.createNotificationChannel(
        channelID: String, channelName: String, playSound: Boolean
    )
}
