package com.example.civiclens.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DateMathTest {

    @Test
    public void roundTripKeepsTheSameDate() {
        assertEquals("1970-01-01", DateMath.fromEpochDay(0));
        assertEquals("2026-10-30", DateMath.fromEpochDay(DateMath.toEpochDay("2026-10-30")));
    }

    @Test
    public void addDaysCrossesMonthAndLeapBoundaries() {
        assertEquals("2026-03-01", DateMath.addDays("2026-02-27", 2));   // 2026 is not a leap year
        assertEquals("2028-03-01", DateMath.addDays("2028-02-28", 2));   // 2028 is a leap year
    }

    @Test
    public void reminderWindowUsesLookahead() {
        String today = "2026-10-30";
        String horizon = DateMath.addDays(today, 3);
        assertEquals("2026-11-02", horizon);
        assertTrue("2026-11-02".compareTo(horizon) <= 0);   // due inside the window
        assertTrue("2026-11-05".compareTo(horizon) > 0);    // outside the window
    }
}
