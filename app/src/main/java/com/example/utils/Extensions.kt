package com.example.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toDateString(format: String = "MMM dd, hh:mm a"): String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Double.format(digits: Int = 2): String {
    return String.format(Locale.getDefault(), "%.${digits}f", this)
}

fun generateId(): String = java.util.UUID.randomUUID().toString().substring(0, 9)
