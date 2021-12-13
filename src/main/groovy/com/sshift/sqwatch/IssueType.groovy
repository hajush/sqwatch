package com.sshift.sqwatch

enum IssueType {
    BUG(1),
    SECURITY_HOTSPOT(2),
    VULNERABILITY(3),
    CODE_SMELL(4)

    final def id

    IssueType(def id) {
        this.id = id
    }
}
