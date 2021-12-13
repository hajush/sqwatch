package com.sshift.sqwatch

import java.time.Instant
import java.time.temporal.ChronoUnit
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.hamcrest.MatcherAssert.*

class AssigneeCacheTest {

    @Test
    void testGetNotReady() {
        def issueKey = 'featurebranch-opal'
        AssigneeCache assigneeCache = new AssigneeCache()
        assertThat(assigneeCache.getAssignee(issueKey), emptyString())
        assertThat(assigneeCache.getExpiredAssignee(issueKey), emptyString())
    }

    @Test
    void testGetReady() {
        def issueKey = 'featurebranch-onyx'
        def assignee = 'john.smith@gmail.com'
        AssigneeCache assigneeCache = new AssigneeCache()
        assigneeCache.setAssignee(issueKey, assignee, Instant.now())
        assertThat(assigneeCache.getAssignee(issueKey), is(assignee))
        assertThat(assigneeCache.getExpiredAssignee(issueKey), emptyString())
    }

    @Test
    void testGetExpired() {
        def issueKey = 'featurebranch-jasper'
        def assignee = 'jane.doe@gmail.com'
        AssigneeCache assigneeCache = new AssigneeCache()
        assigneeCache.setAssignee(issueKey, assignee, Instant.now().minus(3, ChronoUnit.HOURS))
        assertThat(assigneeCache.getAssignee(issueKey), emptyString())
        assertThat(assigneeCache.getExpiredAssignee(issueKey), is(assignee))
    }

}
