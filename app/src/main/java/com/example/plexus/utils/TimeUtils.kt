package com.example.plexus.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    fun formatMessageTime(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val date = Date(timestamp)
        val now = Date()
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(date)
    }

    fun formatChatPreviewTime(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val msgDate = Date(timestamp)
        val now = Date()

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.time

        val yesterdayStart = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.time

        val weekStart = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.time

        return when {
            msgDate.after(todayStart) ->
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(msgDate)
            msgDate.after(yesterdayStart) ->
                "Yesterday"
            msgDate.after(weekStart) ->
                SimpleDateFormat("EEE", Locale.getDefault()).format(msgDate) // Mon, Tue...
            else ->
                SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(msgDate)
        }
    }
}