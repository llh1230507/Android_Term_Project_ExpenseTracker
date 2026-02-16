package com.example.expensetracker.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Expense(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val date: Long = 0L
) {
    fun dateText(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(date))
    }
}