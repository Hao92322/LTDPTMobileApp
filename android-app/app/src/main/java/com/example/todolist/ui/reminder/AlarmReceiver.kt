package com.example.todolist.ui.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getIntExtra("TODO_ID", -1)
        val todoTitle = intent.getStringExtra("TODO_TITLE") ?: "Nhắc nhở công việc!"
        val todoSubtitle = intent.getStringExtra("TODO_SUBTITLE") ?: ""

        // Khởi chạy AlarmActivity để hiển thị báo thức và reo chuông
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("TODO_ID", todoId)
            putExtra("TODO_TITLE", todoTitle)
            putExtra("TODO_SUBTITLE", todoSubtitle)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        context.startActivity(alarmIntent)
    }
}
