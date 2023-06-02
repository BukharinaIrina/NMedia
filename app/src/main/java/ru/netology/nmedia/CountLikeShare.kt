package ru.netology.nmedia

import kotlin.math.floor
import kotlin.math.roundToLong

object CountLikeShare {

    private fun prefixToCount(value: Long): String {
        return if (value in 1_000..999_999) {
            "K"
        } else if (value >= 1_000_000) {
            "M"
        } else {
            ""
        }
    }

    fun counter(value: Long): String {
        val count = when (value) {
            in 0..999 -> value
            in 1_000..9_999 -> {
                if (((floor(value.toDouble() / 100).roundToLong()) % 10) == 0L) {
                    floor(value.toDouble() / 1_000).roundToLong()
                } else {
                    floor(value.toDouble() / 100) / 10
                }
            }

            in 10_000..999_999 -> floor(value.toDouble() / 1_000).roundToLong()
            in 1_000_000..999_999_999 -> {
                if ((((floor(value.toDouble() / 100_000).roundToLong()) % 10) == 0L)) {
                    floor(value.toDouble() / 1_000_000).roundToLong()
                } else {
                    floor(value.toDouble() / 100_000) / 10
                }
            }

            else -> 0
        }
        return count.toString() + prefixToCount(value)
    }
}

