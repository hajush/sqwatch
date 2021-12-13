package com.sshift.sqwatch

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class TimeProvider {

    private static final int DAYS_IN_WEEK = 7
    private static final DateTimeFormatter SIMPLE_DATE_FORMAT =
            DateTimeFormatter.ofPattern('yyyy-MM-dd').withZone(ZoneId.systemDefault())

    Instant now() {
        return Instant.now()
    }

    String today() {
        SIMPLE_DATE_FORMAT.format(now())
    }

    String yesterday() {
        SIMPLE_DATE_FORMAT.format(now().minus(1, ChronoUnit.DAYS))
    }

    String lastWeek() {
        weeksAgo(1)
    }

    String weeksAgo(int weeksAgo) {
        SIMPLE_DATE_FORMAT.format(now().minus(weeksAgo * DAYS_IN_WEEK, ChronoUnit.DAYS))
    }

    String daysAgo(String days) {
        Integer howManyDaysAgo
        try {
            howManyDaysAgo = days.toInteger()
        } catch (NumberFormatException exc) {
            howManyDaysAgo = 1
        }
        SIMPLE_DATE_FORMAT.format(now().minus(howManyDaysAgo, ChronoUnit.DAYS))
    }
}
