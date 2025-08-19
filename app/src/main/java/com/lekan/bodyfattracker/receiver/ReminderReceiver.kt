package com.lekan.bodyfattracker.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lekan.bodyfattracker.MainActivity // Assuming MainActivity is your app's entry point
import com.lekan.bodyfattracker.R
import com.lekan.bodyfattracker.data.local.ReminderRepository // For ACTION_REMINDER constant

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "body_fat_tracker_reminder_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ReminderRepository.ACTION_REMINDER) {
            showNotification(context)
        }
    }

    private fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Create Notification Channel (for API 26+ - Android O)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.reminder_notification_channel_name)
            val descriptionText = context.getString(R.string.reminder_notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                // Optionally enable lights, vibration, etc.
                // enableLights(true)
                // lightColor = Color.RED
                // enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel) // Register the channel with the system
        }

        // 2. Create an Intent to launch MainActivity when notification is tapped
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // You can add extras here if you want to navigate to a specific screen
            // putExtra("navigateTo", "reminderScreen")
        }
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            0, // Request code for this PendingIntent
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_reminder) // **IMPORTANT: Create this icon!**
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_notification_large))
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(context.getString(R.string.reminder_notification_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingContentIntent) // Set the intent that will fire when the user taps the notification
            .setAutoCancel(true) // Automatically removes the notification when the user taps it
            // Optionally add actions, big text style, etc.
            // .addAction(R.drawable.ic_snooze, "Snooze", snoozePendingIntent)

        // 4. Show the notification
        // Use a consistent ID for this notification type if you want to update/cancel it later
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}
