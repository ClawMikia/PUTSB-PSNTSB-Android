package com.cyberpunk.debttracker.util

import java.text.NumberFormat
import java.util.*

object CurrencyFormatter {

    private val currencyFormat: NumberFormat
        get() = NumberFormat.getCurrencyInstance(Locale("en", "PH")).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }

    fun format(amount: Double): String {
        return currencyFormat.format(amount)
    }
}
