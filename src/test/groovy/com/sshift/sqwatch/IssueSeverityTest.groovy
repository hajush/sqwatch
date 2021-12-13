package com.sshift.sqwatch

import org.junit.Test

import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.MatcherAssert.assertThat

class IssueSeverityTest {

    @Test
    void testId() {
        IssueSeverity sev = IssueSeverity.BLOCKER
        assertThat(sev.id, is(1))
    }

    @Test
    void testCompareSeverity() {
        assertThat(IssueSeverity.INFO > IssueSeverity.BLOCKER, is(true))
    }
}
