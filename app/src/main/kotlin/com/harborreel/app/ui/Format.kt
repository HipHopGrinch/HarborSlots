package com.harborreel.app.ui

import java.util.Locale

fun Long.grouped(): String = "%,d".format(Locale.US, this)

fun Int.grouped(): String = "%,d".format(Locale.US, this)

fun Long.money(): String {
    val negative = this < 0
    val abs = kotlin.math.abs(this)
    val dollars = abs / 100
    val cents = (abs % 100).toInt()
    val body = "$%,d.%02d".format(Locale.US, dollars, cents)
    return if (negative) "-$body" else body
}

fun denomLabel(cents: Int): String = when {
    cents < 100 -> "$cents¢"
    cents % 100 == 0 -> "$${cents / 100}"
    else -> cents.toLong().money()
}

fun parseDollarsToCents(raw: String): Long? {
    val cleaned = raw.trim().removePrefix("$").replace(",", "")
    if (cleaned.isEmpty()) return null
    val parts = cleaned.split('.')
    if (parts.size > 2) return null
    val dollars = parts[0].toLongOrNull() ?: return null
    if (dollars < 0) return null
    val cents = if (parts.size == 1) {
        0
    } else {
        parts[1].padEnd(2, '0').take(2).toIntOrNull() ?: return null
    }
    return dollars * 100 + cents
}
