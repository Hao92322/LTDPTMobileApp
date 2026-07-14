package com.example.todolist.ui.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDateTime
import java.time.ZoneId

object ReminderScheduler {

    fun scheduleAlarm(context: Context, id: Int, title: String, subtitle: String, triggerTime: LocalDateTime) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        // Kiểm tra quyền lập lịch báo thức chính xác (chỉ Android 12 trở lên)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Nếu không thể lập lịch chính xác, dùng báo thức thông thường
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("TODO_ID", id)
                    putExtra("TODO_TITLE", title)
                    putExtra("TODO_SUBTITLE", subtitle)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val zoneId = ZoneId.systemDefault()
                val timeInMillis = triggerTime.atZone(zoneId).toInstant().toEpochMilli()
                if (timeInMillis > System.currentTimeMillis()) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                }
                return
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TODO_ID", id)
            putExtra("TODO_TITLE", title)
            putExtra("TODO_SUBTITLE", subtitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val zoneId = ZoneId.systemDefault()
        val timeInMillis = triggerTime.atZone(zoneId).toInstant().toEpochMilli()

        if (timeInMillis > System.currentTimeMillis()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                // Phòng hờ lỗi SecurityException trên Android 14+ khi gọi setExact
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            }
        }
    }

    fun scheduleSnooze(context: Context, id: Int, title: String, subtitle: String) {
        val snoozeTime = LocalDateTime.now().plusMinutes(5)
        scheduleAlarm(context, id, "$title (Báo lại)", subtitle, snoozeTime)
    }

    fun cancelAlarm(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
