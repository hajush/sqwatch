package com.sshift.sqwatch

import org.junit.Test

import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.MatcherAssert.assertThat

class IssueTypeTest {

    @Test
    void testId() {
        IssueType type = IssueType.SECURITY_HOTSPOT
        assertThat(type.id, is(2))
    }
}
