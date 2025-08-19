package com.lekan.bodyfattracker.data.local

import android.R.attr.action
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lekan.bodyfattracker.receiver.ReminderReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_reminders")

data class ReminderPreferences(
    val isEnabled: Boolean,
    val hour: Int?,
    val minute: Int?
)

class ReminderRepository(private val context: Context) {

    private object PreferencesKeys {
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    val reminderPreferencesFlow: Flow<ReminderPreferences> = context.dataStore.data
        .map { preferences ->
            val isEnabled = preferences[PreferencesKeys.REMINDER_ENABLED] ?: false
            val hour = preferences[PreferencesKeys.REMINDER_HOUR]
            val minute = preferences[PreferencesKeys.REMINDER_MINUTE]
            ReminderPreferences(isEnabled, hour, minute)
        }

    suspend fun updateReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_ENABLED] = enabled
            if (!enabled) { // Optionally clear time if disabling
                preferences.remove(PreferencesKeys.REMINDER_HOUR)
                preferences.remove(PreferencesKeys.REMINDER_MINUTE)
            }
        }
    }

    suspend fun updateReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_HOUR] = hour
            preferences[PreferencesKeys.REMINDER_MINUTE] = minute
            preferences[PreferencesKeys.REMINDER_ENABLED] = true // Ensure it's enabled when time is set
        }
    }

    // --- Alarm Scheduling/Cancelling logic will go here ---
     fun scheduleReminder(hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            // If the time is in the past, schedule for the next day
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        // For exact alarms on modern Android, check permissions:
        // if (alarmManager.canScheduleExactAlarms()) { // API 31+
        //     alarmManager.setExactAndAllowWhileIdle(
        //         AlarmManager.RTC_WAKEUP,
        //         calendar.timeInMillis,
        //         pendingIntent
        //     )
        // } else { /* Handle cases where exact alarms are denied or use inexact */ }
        // For broader compatibility, you might start with setInexactRepeating or setRepeating for daily.
        // Or setExact for a one-time and re-schedule in the receiver.
        // For a daily repeating exact alarm:
        alarmManager.setRepeating( // Or setInexactRepeating for battery saving
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
     }

     fun cancelReminder() {
         val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
         val intent = Intent(context, ReminderReceiver::class.java).apply {
             action = ACTION_REMINDER
         }
         val pendingIntent = PendingIntent.getBroadcast(
             context,
             REMINDER_REQUEST_CODE,
             intent,
             PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // NoCreate to check if it exists
         )
         if (pendingIntent != null) {
             alarmManager.cancel(pendingIntent)
             pendingIntent.cancel()
         }
     }

    companion object { // For PendingIntent request code
        const val REMINDER_REQUEST_CODE = 123
        const val ACTION_REMINDER = "com.lekan.bodyfattracker.ACTION_REMINDER"
    }
}
