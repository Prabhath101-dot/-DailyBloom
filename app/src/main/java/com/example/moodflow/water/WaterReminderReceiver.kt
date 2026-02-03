package com.example.moodflow.water

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.example.moodflow.R
import com.example.moodflow.MainActivity

class WaterReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "water_reminders"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Water Reminders", NotificationManager.IMPORTANCE_HIGH)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 400, 200, 400)
            // Custom sound from res/raw/water_reminder.* if present
            val soundResId = context.resources.getIdentifier("water_reminder", "raw", context.packageName)
            if (soundResId != 0) {
                val soundUri = android.net.Uri.parse("android.resource://" + context.packageName + "/" + soundResId)
                channel.setSound(soundUri, android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
            }
            // Recreate/replace existing channel if needed so sound updates apply
            nm.deleteNotificationChannel(channelId)
            nm.createNotificationChannel(channel)
        }
        // PendingIntent to open Log Water page
        val resultIntent = Intent(context, MainActivity::class.java).apply { putExtra("open_log_water", true) }
        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(2002, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("Hydration reminder")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Take a sip now. Your body will thank you. Log it in MoodFlow!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // If custom sound exists, set it here too for pre-O devices
        val soundResId = context.resources.getIdentifier("water_reminder", "raw", context.packageName)
        if (soundResId != 0) {
            val uri = android.net.Uri.parse("android.resource://" + context.packageName + "/" + soundResId)
            notification.sound = uri
        }
        nm.notify(System.currentTimeMillis().toInt(), notification)

        // Extra vibration for older channels or if app in foreground
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 400, 200, 400), -1)
            }
        } catch (_: Exception) { }
    }
}

