package com.example.data.repository

import android.content.Context
import com.example.data.AppConstants

class AuthRepository(private val context: Context) {

    fun validateLogin(role: String, pin: String?): Boolean {
        return when (role) {
            AppConstants.ROLE_PASSENGER -> true
            AppConstants.ROLE_SUPERADMIN -> pin == AppConstants.DEMO_PIN_SUPERADMIN
            AppConstants.ROLE_PORT -> pin == AppConstants.DEMO_PIN_PORT
            AppConstants.ROLE_TERMINAL -> pin == AppConstants.DEMO_PIN_TERMINAL
            else -> false
        }
    }
}
