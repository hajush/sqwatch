package com.sshift.sqwatch

import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.MatcherAssert.assertThat

class SQWatchControllerTest {
    @Test
    void testIndex() {
        assertThat(new SQWatchController().index(), equalTo("index"))
    }
}
