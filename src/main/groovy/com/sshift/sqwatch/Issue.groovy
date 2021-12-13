package com.sshift.sqwatch

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class Issue implements Comparable {

    @Id @GeneratedValue
    private Long id

    @ManyToOne @JoinColumn
    private Author authorData

    String key
    String message
    String component
    String componentName
    String projectName
    String fileName
    IssueSeverity severity
    IssueType type
    String rule
    String authorEmail
    String debt
    String creationDate
    Integer startLine
    Integer endLine
    Integer startOffset
    Integer endOffset

    Issue() {}

    Issue(Object stub, Author authorData) {
        this.authorData = authorData ?: Author.NO_AUTHOR
        this.key = stub.key
        this.message = stub.message
        this.component = stub.component
        makeProjectName()
        makeComponentName()
        makeFileName()
        this.authorEmail = this.authorData.primaryEmail
        this.severity = stub.severity as IssueSeverity
        this.type = stub.type as IssueType
        this.rule = stub.rule
        this.debt = stub.debt
        this.creationDate = stub.creationDate
        this.startLine = stub.startLine ?: (stub.textRange ? stub.textRange.startLine : null)
        this.endLine = stub.endLine ?: (stub.textRange ? stub.textRange.endLine : null)
        this.startOffset = stub.startOffset ?: (stub.textRange ? stub.textRange.startOffset : null)
        this.endOffset = stub.endOffset ?: (stub.textRange ? stub.textRange.endOffset : null)
    }

    private void makeComponentName() {
        def matches = this.component =~ /^([^\:]*)\:([\-\w]*)\/.*\/(\w*\.java)$/
        this.componentName = (matches.size() > 0 && matches[0] && matches[0][2]) ?
                "${matches[0][2]}" : null
    }

    private void makeProjectName() {
        def matches = this.component =~ /^(.+)\:([^\:])*$/
        this.projectName = matches.size() > 0 && matches[0][1] ? "${matches[0][1]}" : null
    }

    Long getId() {
        return id
    }

    private void makeFileName() {
        def matches = this.component =~ /[^\:]*\:([\-\w]*)\/.*\/(\w*\.java)$/
        this.fileName = (matches.size() > 0 && matches[0] && matches[0][2]) ?
                "${matches[0][2]}" : null
    }

    def getLink() {
        "#" + (author ?: "")
    }

    String getAuthor() {
        return authorData.name
    }

    @Override
    int compareTo(Object issue) {
        if (this.key == issue.key) {
            return 0
        }

        def severityCompare = compareSeverity(issue)
        if (severityCompare != 0) {
            return severityCompare
        }

        def typeCompare = compareType(issue)
        if (typeCompare != 0) {
            return typeCompare
        }

        if (this.rule != issue.rule) {
            return this.rule <=> issue.rule;
        }

        if (this.component != issue.component) {
            return this.component <=> issue.component
        }

        if (this.author != issue.author) {
            return this.author <=> issue.author
        }

        if (this.message != issue.message) {
            return this.message <=> issue.message
        }

        return this.key <=> issue.key
    }

    private int compareSeverity(Issue issue) {
        rankSeverity(this.severity) - rankSeverity(issue.severity)
    }

    int compareType(Issue issue) {
        rankType(this.type) - rankType(issue.type)
    }

    String getTeam() {
        return authorData.team
    }

    static int rankType(type) {
        return type?.id ?: 0
    }

    static int rankSeverity(severity) {
        return severity?.id ?: 0
    }

    double getDebtInDays() {
        return debtToDays(debt)
    }

    private double debtToDays(String debtCode) {
        if (!debtCode) {
            return 0
        } else if (debtCode.contains('d')) {
            double days = Integer.parseInt(debtCode.substring(0, debtCode.indexOf('d')))
            String hoursMins = debtCode.substring(debtCode.indexOf('d') + 1)
            return days + debtToDays(hoursMins)
        } else if (debtCode.contains('h')) {
            double hours = Integer.parseInt(debtCode.substring(0, debtCode.indexOf('h')))
            String minutes = debtCode.substring(debtCode.indexOf('h') + 1)
            return hours / 8.0 + debtToDays(minutes)
        } else if (debtCode.endsWith("min")) {
            return Integer.parseInt(debtCode[0..debtCode.size()-4]) / 60.0 / 8.0
        }
        throw new IllegalArgumentException("Not a minute, hour, or day: " + debtCode)
    }

    boolean isEquivalent(Issue issue) {
        if (!issue) {
            return false
        }
        def fieldsToCheck = [
                'debt', 'type', 'componentName', 'fileName', 'startLine', 'endLine', 'startOffset', 'endOffset']
        return fieldsToCheck
                .stream()
                .reduce(true, (result, field) -> result && (this[field] == issue[field]))
    }

    void setUpcoming() {
        creationDate = 'UPCOMING'
    }
}
