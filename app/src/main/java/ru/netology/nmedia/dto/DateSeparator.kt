package ru.netology.nmedia.dto

import android.content.Context
import ru.netology.nmedia.R

enum class DateSeparator {
    TODAY,
    YESTERDAY,
    LAST_WEEK,
}

fun DateSeparator.getText(context: Context): String = context.getString(
    when (this) {
        DateSeparator.TODAY -> R.string.today
        DateSeparator.YESTERDAY -> R.string.yesterday
        DateSeparator.LAST_WEEK -> R.string.last_week
    }
)