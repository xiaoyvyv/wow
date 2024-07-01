package com.xiaoyv.wow.kts

import java.util.concurrent.ConcurrentHashMap

fun Long.formatHMS(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

private val time = ConcurrentHashMap<String, Long>()

fun resetMonitor(tag: String) {
    time[tag] = System.currentTimeMillis()
}

fun printMonitor(tag: String, reset: Boolean = false) {
    System.err.println("Spend($tag) time: " + (System.currentTimeMillis() - time.getOrDefault(tag, 0)))
    if (reset) {
        resetMonitor(tag)
    }
}