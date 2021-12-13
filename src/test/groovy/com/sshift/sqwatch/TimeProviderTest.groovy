package com.sshift.sqwatch

import org.junit.Test

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

import static org.hamcrest.Matchers.matchesPattern
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is

class TimeProviderTest {

    @Test
    void testTodaysDate() {
        def timeProvider = new TimeProvider()
        assertThat(timeProvider.today(), matchesPattern('^\\d{4}-\\d{2}-\\d{2}\$'))
    }

    @Test
    void testDaysAgo() {
        def fakeNow = LocalDateTime.parse('2021-11-24T00:00:00').toInstant(ZoneOffset.systemDefault())
        def timeProvider = new TimeProvider() {
            Instant now() { fakeNow }
        }
        assertThat(timeProvider.daysAgo('1'), is('2021-11-23'))
        assertThat(timeProvider.daysAgo('notanumber'), is('2021-11-23'))
        assertThat(timeProvider.daysAgo('8'), is('2021-11-16'))
    }
}
