package com.sshift.sqwatch

import org.springframework.stereotype.Component

import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class AssigneeCache {

    private TimeProvider timeProvider = new TimeProvider()
    private def cache = [:] // issueKey -> [String assignee, Instant timestamp]

    String getAssignee(String issueKey) {
        getFromCache(issueKey)
    }

    String getExpiredAssignee(String issueKey) {
        getFromCacheExpired(issueKey)
    }

    void setAssignee(String issueKey, String assignee, Instant timestamp) {
        setInCache(issueKey, assignee, timestamp)
    }

    private String getFromCache(String issueKey) {
        synchronized (cache) {
            def entry = cache[issueKey]
            entry && freshEntry(entry) ? entry.assignee : ""
        }
    }

    private String getFromCacheExpired(String issueKey) {
        synchronized (cache) {
            def entry = cache[issueKey]
            entry && !freshEntry(entry) ? entry.assignee : ""
        }
    }

    private boolean freshEntry(def entry) {
        Instant now = timeProvider.now()
        Instant twoHoursAgo = now.minus(2, ChronoUnit.HOURS)
        boolean fresh = entry.timestamp.isAfter(twoHoursAgo)
        return fresh
    }

    private void setInCache(String issueKey, String assignee, Instant timestamp) {
        synchronized (cache) {
            cache[issueKey] = [assignee: assignee, timestamp: timestamp]
        }
    }

}

