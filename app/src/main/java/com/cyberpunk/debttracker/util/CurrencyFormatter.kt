package com.cyberpunk.debttracker.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    private val pesoFormat: NumberFormat = NumberFormat.getInstance(Locale("en", "PH")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    fun format(amount: Double): String = "₱${pesoFormat.format(amount)}"

    fun formatShort(amount: Double): String {
        return when {
            amount >= 1_000_000 -> "₱${String.format("%.1fM", amount / 1_000_000)}"
            amount >= 1_000     -> "₱${String.format("%.1fK", amount / 1_000)}"
            else                -> format(amount)
        }
    }

    fun formatSigned(amount: Double, isDebt: Boolean): String {
        val formatted = format(kotlin.math.abs(amount))
        return if (isDebt) "-$formatted" else "+$formatted"
    }
}
