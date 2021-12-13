package com.sshift.sqwatch

class TeamStats {
    public static final ALL_DATES = '!ALL'
    public static final ALL_TEAMS = 'ALL'

    private List<String> allTeams
    private final stats = []
    private final issues = []
    private dates = [ALL_DATES]
    private datesReverse = dates.reverse()

    TeamStats(List<String> allTeams) {
        this.allTeams = allTeams
    }

    void addIssues(List<Issue> moreIssues) {
        moreIssues.each { oneMoreIssue ->
            Issue equivalentIssue = this.issues.find { oldIssue -> oneMoreIssue.isEquivalent(oldIssue) }
            if (!equivalentIssue) {
                this.issues.add(oneMoreIssue)
            }
        }
    }

    List<TeamStatPoint> getTeamStatPoints(String team, Project project) {
        processIssuesIntoStats(project)
        List<TeamStatPoint> result = []
        def allStats = stats.findAll {
            team == ALL_TEAMS || it.team == team
        }
        return allStats.collect(result, { it })
    }

    private TeamStatPoint findTeamStatPoint(String team, String date) {
        TeamStatPoint teamStatPoint = stats.find { it.team == team && (it.date == ALL_DATES || it.date == date)}
        teamStatPoint
    }

    private void processIssuesIntoStats(Project project) {
        zeroTablesForAllTeams()
        issues.each { Issue issue ->
            String issueDate = getCreationDate(issue, project)
            def date = dates.find { issueDate >= it }
            if (date) {
                TeamStatPoint teamStatPoint = findTeamStatPoint(issue.team, date)
                if (teamStatPoint) {
                    countIssue(teamStatPoint, issue)
                }
            }
        }
    }

    private String getCreationDate(Issue issue, Project project) {
        project.main.contains(issue.projectName) ? issue.creationDate.substring(0, 10) : "UPCOMING"
    }

    private void zeroTablesForAllTeams() {
        stats.clear()
        allTeams.forEach { team ->
            datesReverse.forEach { date ->
                stats.add(new TeamStatPoint(team, date))
            }
        }
    }

    private void countIssue(TeamStatPoint teamStatPoint, Issue issue) {
        teamStatPoint.issueCount++
        if (issue.type == IssueType.BUG) {
            teamStatPoint.bugCount++
        } else if (issue.type == IssueType.CODE_SMELL) {
            teamStatPoint.codeSmellCount++
            teamStatPoint.debtInDays += issue.debtInDays
        } else if (issue.type == IssueType.VULNERABILITY) {
            teamStatPoint.vulnerabilityCount++
        }
    }

    void setDates(List<String> dates) {
        this.dates = dates
        this.datesReverse = dates.reverse()
    }

}

