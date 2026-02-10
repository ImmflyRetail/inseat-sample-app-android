package com.immflyretail.inseat.sampleapp.core.extension

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/**
 * Converts a BigDecimal to a localized String with exactly 2 decimal places.
 * US/UK Example: 1234.5 -> "1,234.50"
 * Spain Example: 1234.5 -> "1.234,50"
 */
fun BigDecimal.toLocalizedMoney(
    locale: Locale = Locale.getDefault()
): String {
    val formatter = NumberFormat.getInstance(locale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return formatter.format(this)
}