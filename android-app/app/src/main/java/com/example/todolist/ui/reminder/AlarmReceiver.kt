package com.example.todolist.ui.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getIntExtra("TODO_ID", -1)
        val todoTitle = intent.getStringExtra("TODO_TITLE") ?: "Nhắc nhở công việc!"
        val todoSubtitle = intent.getStringExtra("TODO_SUBTITLE") ?: ""

        android.util.Log.d("AlarmReceiver", "=== BÁO THỨC ĐƯỢC KÍCH HOẠT ===")
        android.util.Log.d("AlarmReceiver", "Nhận Broadcast cho Todo ID: $todoId, Title: $todoTitle")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "todo_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nhắc nhở công việc",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh thông báo nhắc nhở việc cần làm"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Khởi chạy AlarmActivity để hiển thị báo thức và reo chuông
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("TODO_ID", todoId)
            putExtra("TODO_TITLE", todoTitle)
            putExtra("TODO_SUBTITLE", todoSubtitle)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        // Tạo PendingIntent cho fullScreenIntent
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            todoId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Icon mặc định của hệ thống
            .setContentTitle(todoTitle)
            .setContentText(todoSubtitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)

        notificationManager.notify(todoId, notificationBuilder.build())

        // Cố gắng mở Activity trực tiếp nếu thiết bị cho phép hoặc app đang ở foreground
        try {
            context.startActivity(alarmIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
