package com.sshift.sqwatch

enum IssueSeverity {
    BLOCKER(1),
    CRITICAL(2),
    MAJOR(3),
    MINOR(4),
    INFO(5)

    final def id

    IssueSeverity(def id) {
        this.id = id
    }

}
