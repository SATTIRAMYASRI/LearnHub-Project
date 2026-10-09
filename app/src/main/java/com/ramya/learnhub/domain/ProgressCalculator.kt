package com.ramya.learnhub.domain

import kotlin.math.roundToInt

object ProgressCalculator {

    /** Progress (%) = completed / total × 100, rounded; 0 when the course has no lessons. */
    fun percent(completed: Int, total: Int): Int {
        if (total <= 0) return 0
        return (completed.coerceIn(0, total) * 100f / total).roundToInt()
    }
}
