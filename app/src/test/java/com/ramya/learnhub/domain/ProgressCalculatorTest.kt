package com.ramya.learnhub.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCalculatorTest {
    @Test fun `rounds to whole percentage`() {
        assertEquals(33, ProgressCalculator.percent(1, 3))
        assertEquals(67, ProgressCalculator.percent(2, 3))
    }

    @Test fun `none and all completed`() {
        assertEquals(0, ProgressCalculator.percent(0, 20))
        assertEquals(100, ProgressCalculator.percent(20, 20))
    }

    @Test fun `no lessons returns zero without dividing by zero`() {
        assertEquals(0, ProgressCalculator.percent(0, 0))
    }
}
