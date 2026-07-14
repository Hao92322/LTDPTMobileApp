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
        val zoneId = ZoneId.systemDefault()
        val timeInMillis = triggerTime.atZone(zoneId).toInstant().toEpochMilli()
        val currentTime = System.currentTimeMillis()
        
        android.util.Log.d("ReminderScheduler", "--- LẬP LỊCH BÁO THỨC ---")
        android.util.Log.d("ReminderScheduler", "ID: $id, Title: $title")
        android.util.Log.d("ReminderScheduler", "Thời gian báo thức: $triggerTime (millis: $timeInMillis)")
        android.util.Log.d("ReminderScheduler", "Thời gian hiện tại: ${LocalDateTime.now(zoneId)} (millis: $currentTime)")
        android.util.Log.d("ReminderScheduler", "Thời gian chênh lệch: ${(timeInMillis - currentTime) / 1000} giây")

        if (timeInMillis <= currentTime) {
            val diffMs = currentTime - timeInMillis
            if (diffMs < 3 * 60 * 1000) { // Trễ dưới 3 phút
                android.util.Log.d("ReminderScheduler", "KÍCH HOẠT NGAY: Báo thức trễ trong vòng 3 phút (trễ ${diffMs / 1000} giây).")
                val triggerIntent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("TODO_ID", id)
                    putExtra("TODO_TITLE", title)
                    putExtra("TODO_SUBTITLE", subtitle)
                }
                context.sendBroadcast(triggerIntent)
            } else {
                android.util.Log.w("ReminderScheduler", "HỦY LẬP LỊCH: Đã quá hạn hơn 3 phút (trễ ${diffMs / 1000} giây).")
            }
            return
        }

        // Kiểm tra quyền lập lịch báo thức chính xác (chỉ Android 12 trở lên)
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        android.util.Log.d("ReminderScheduler", "Quyền Exact Alarm (canScheduleExactAlarms): $canScheduleExact")

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

        try {
            if (canScheduleExact) {
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
                android.util.Log.d("ReminderScheduler", "Đã đặt báo thức CHÍNH XÁC (setExactAndAllowWhileIdle) thành công!")
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
                android.util.Log.w("ReminderScheduler", "Đã đặt báo thức KHÔNG CHÍNH XÁC (setAndAllowWhileIdle) do thiếu quyền!")
            }
        } catch (e: SecurityException) {
            android.util.Log.e("ReminderScheduler", "Lỗi bảo mật khi đặt báo thức chính xác, tự động fallback", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
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
