package com.example.data

object AppConstants {

    // === AUTH ===
    const val DEMO_PIN_SUPERADMIN = "1234"
    const val DEMO_PIN_PORT = "2001"
    const val DEMO_PIN_TERMINAL = "2002"

    // === PRICING ===
    const val PRICE_FERRY_REGULAR = 500.0
    const val PRICE_FERRY_STUDENT = 450.0
    const val PRICE_VAN_REGULAR = 200.0
    const val PRICE_VAN_STUDENT = 180.0

    // Commission rates
    const val COMMISSION_FERRY = 0.12   // 12%
    const val COMMISSION_VAN = 0.08     // 8%

    // === ROLES ===
    const val ROLE_SUPERADMIN = "superadmin"
    const val ROLE_PORT = "port"
    const val ROLE_TERMINAL = "terminal"
    const val ROLE_PASSENGER = "passenger"
}
