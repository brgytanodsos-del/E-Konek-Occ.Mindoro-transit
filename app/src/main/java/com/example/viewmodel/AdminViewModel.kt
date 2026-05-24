package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.repository.AdminRepository
import com.example.data.repository.TransactionRepository
import com.example.utils.generateId
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val transactionRepository = TransactionRepository(db.dao())
    private val adminRepository = AdminRepository(db.dao())

    val transactions = transactionRepository.getAllTransactions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val auditLogs = adminRepository.getAllAuditLogs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val payouts = adminRepository.getAllPayouts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refundTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction.copy(status = "Refunded"))
        }
    }

    fun markAllAsPaid() {
        viewModelScope.launch {
            val completed = transactions.value.filter { it.status == "Completed" && !it.paid }
            if (completed.isNotEmpty()) {
                val totalAmount = completed.sumOf { it.commissionAmount }
                val count = completed.size
                adminRepository.insertPayout(Payout(
                    id = generateId(),
                    date = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
                    totalAmount = totalAmount,
                    transactionCount = count
                ))
                transactionRepository.markAllAsPaid()
            }
        }
    }
}
