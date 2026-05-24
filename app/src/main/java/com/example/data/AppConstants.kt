package com.example.data

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object AppConstants {
    val ROLE_PINS = mapOf(
        "port" to "2001",
        "terminal" to "2002",
        "passenger" to null,
        "superadmin" to "1234"
    )

    val COMMISSION_RATES = mapOf(
        "ferry_regular" to 50.0,
        "ferry_student" to 30.0,
        "ferry_senior" to 25.0,
        "ferry_pwd" to 25.0,
        "van_per_seat" to 20.0,
        "bus_per_seat" to 15.0
    )

    const val PRICE_FERRY_REGULAR = 500.0
    const val PRICE_FERRY_STUDENT = 350.0
    const val PRICE_FERRY_SENIOR_PWD = 300.0
    const val PRICE_VAN_SEAT = 200.0
    const val PRICE_BUS_SEAT = 150.0

    val GPS_ROUTES = mapOf(
        "Mamburao → Abra Port" to listOf(
            listOf(13.2167, 120.5833),
            listOf(13.25, 120.59),
            listOf(13.30, 120.60),
            listOf(13.35, 120.61),
            listOf(13.40, 120.62),
            listOf(13.45, 120.63)
        ),
        "Abra Port → Mamburao" to listOf(
            listOf(13.45, 120.63),
            listOf(13.40, 120.62),
            listOf(13.35, 120.61),
            listOf(13.30, 120.60),
            listOf(13.25, 120.59),
            listOf(13.2167, 120.5833)
        ),
        "Mamburao → San Jose" to listOf(
            listOf(13.2167, 120.5833),
            listOf(13.15, 120.60),
            listOf(13.05, 120.65),
            listOf(12.95, 120.70),
            listOf(12.85, 120.75),
            listOf(12.75, 120.80)
        ),
        "San Jose → Mamburao" to listOf(
            listOf(12.75, 120.80),
            listOf(12.85, 120.75),
            listOf(12.95, 120.70),
            listOf(13.05, 120.65),
            listOf(13.15, 120.60),
            listOf(13.2167, 120.5833)
        ),
        "Mamburao → Calintaan" to listOf(
            listOf(13.2167,120.5833),
            listOf(13.20,120.55),
            listOf(13.18,120.52),
            listOf(13.16,120.49)
        ),
        "Calintaan → Mamburao" to listOf(
            listOf(13.16,120.49),
            listOf(13.18,120.52),
            listOf(13.20,120.55),
            listOf(13.2167,120.5833)
        ),
        "Mamburao → Paluan" to listOf(
            listOf(13.2167,120.5833),
            listOf(13.25,120.55),
            listOf(13.30,120.52),
            listOf(13.35,120.50),
            listOf(13.40,120.48)
        ),
        "Paluan → Mamburao" to listOf(
            listOf(13.40,120.48),
            listOf(13.35,120.50),
            listOf(13.30,120.52),
            listOf(13.25,120.55),
            listOf(13.2167,120.5833)
        ),
        "Mamburao → Sablayan" to listOf(
            listOf(13.2167,120.5833),
            listOf(13.15,120.55),
            listOf(13.05,120.52),
            listOf(12.95,120.50),
            listOf(12.85,120.48)
        ),
        "Sablayan → Mamburao" to listOf(
            listOf(12.85,120.48),
            listOf(12.95,120.50),
            listOf(13.05,120.52),
            listOf(13.15,120.55),
            listOf(13.2167,120.5833)
        ),
        "default" to listOf(
            listOf(13.2167,120.5833),
            listOf(13.22,120.585),
            listOf(13.23,120.59)
        )
    )
}

fun generateId() = UUID.randomUUID().toString().substring(0, 9)

fun formatPST(iso: String): String {
    return try {
        val dt = ZonedDateTime.parse(iso)
        dt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))
    } catch (e: Exception) {
        iso
    }
}

fun formatPST(timestamp: Long): String {
    return try {
        val dt = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp), java.time.ZoneId.of("Asia/Manila"))
        dt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))
    } catch (e: Exception) {
        timestamp.toString()
    }
}

fun getCommission(type: String, ticketType: String, seats: Int): Double {
    return when (type) {
        "Ferry" -> {
            when (ticketType) {
                "Regular" -> AppConstants.COMMISSION_RATES["ferry_regular"] ?: 0.0
                "Student" -> AppConstants.COMMISSION_RATES["ferry_student"] ?: 0.0
                "Senior" -> AppConstants.COMMISSION_RATES["ferry_senior"] ?: 0.0
                "PWD" -> AppConstants.COMMISSION_RATES["ferry_pwd"] ?: 0.0
                else -> 0.0
            }
        }
        "Van" -> (AppConstants.COMMISSION_RATES["van_per_seat"] ?: 0.0) * seats
        "Bus" -> (AppConstants.COMMISSION_RATES["bus_per_seat"] ?: 0.0) * seats
        else -> 0.0
    }
}

fun getGrossAmount(type: String, ticketType: String, seats: Int): Double {
    return when (type) {
        "Ferry" -> {
            when (ticketType) {
                "Regular" -> AppConstants.PRICE_FERRY_REGULAR
                "Student" -> AppConstants.PRICE_FERRY_STUDENT
                "Senior", "PWD" -> AppConstants.PRICE_FERRY_SENIOR_PWD
                else -> 0.0
            }
        }
        "Van" -> AppConstants.PRICE_VAN_SEAT * seats
        "Bus" -> AppConstants.PRICE_BUS_SEAT * seats
        else -> 0.0
    }
}
