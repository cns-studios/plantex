// app/src/main/java/com/cns/plantex/util/NotificationHelper.kt
package com.cns.plantex.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cns.plantex.R

private const val NOTIFICATION_CHANNEL_ID = "watering_notification_channel"
private const val NOTIFICATION_ID = 1

class NotificationHelper(private val context: Context) {

    fun createNotification() {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to Water Your Plant!")
            .setContentText("Your plant is thirsty. Open the app to water it.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Watering Notifications"
            val descriptionText = "Notifications for watering your plant"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
