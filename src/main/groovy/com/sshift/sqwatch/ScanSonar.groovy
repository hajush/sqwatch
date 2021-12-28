package com.sshift.sqwatch

import groovy.json.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import javax.transaction.Transactional

@Component
class ScanSonar {
    private static final String ISSUES_SEARCH = '/api/issues/search'
    private static final String PROJECTS_SEARCH = '/api/projects/search'
    private static final int MINUTES_PER_DAY = 480

    // SonarQube will not allow fetching more than 10000 issues
    private static final int MAX_ISSUES = 10000

    private Logger logger = LoggerFactory.getLogger(ScanSonar.class)
    private JsonSlurper jsonSlurper = new JsonSlurper()

    TimeProvider timeProvider = new TimeProvider()
    URLFetcher urlFetcher = new URLFetcher(logger)

    @Value('${sqwatch.sonarurl}')
    private String SONAR_BASE_URL

    @Value('${sqwatch.sonarauth}')
    private String SONAR_BASE_AUTH

    @Value('${sqwatch.sonarmaster}')
    private String SONAR_BASE_MASTER_PROJECT

    @Value('${sqwatch.sonarbranchprefix}')
    private String SONAR_BASE_BRANCH_PREFIX

    @Value('${sqwatch.threshholdcodesmell}')
    private String THRESHHOLD_CODESMELL
    @Value('${sqwatch.threshholdbug}')
    private String THRESHHOLD_BUG
    @Value('${sqwatch.threshholdvulnerability}')
    private String THRESHHOLD_VULNERABILITY
    @Value('${sqwatch.weeksmasterissues}')
    private int SQWATCH_WEEKS_OF_MASTER_ISSUES
    @Value('${sqwatch.weeksbranchissues}')
    private int SQWATCH_WEEKS_OF_BRANCH_ISSUES

    private IssueSeverity bugThreshhold = IssueSeverity.INFO
    private IssueSeverity smellThreshhold = IssueSeverity.INFO
    private IssueSeverity vulnerabilityThreshhold = IssueSeverity.INFO

    private List<String> allTeams
    private Project project
    MailMap mailMap

    @Autowired TeamRepository teamRepository
    @Autowired AuthorRepository authorRepository
    @Autowired IssueRepository issueRepository
    @Autowired OldUpcomingRepository oldUpcomingRepository
    @Autowired ResourceLoader resourceLoader

    @PostConstruct
    void init(){
        updateTeamsFromDB()
        project = new Project()
        project.main = SONAR_BASE_MASTER_PROJECT
        project.branchPrefix = SONAR_BASE_BRANCH_PREFIX
        bugThreshhold = THRESHHOLD_BUG as IssueSeverity
        smellThreshhold = THRESHHOLD_CODESMELL as IssueSeverity
        vulnerabilityThreshhold = THRESHHOLD_VULNERABILITY as IssueSeverity
        Resource mailMapResource = resourceLoader.getResource('file:conf/.mailmap')

        mailMap = new MailMap((mailMapResource && mailMapResource.exists())
                ? mailMapResource.inputStream : InputStream.nullInputStream())
    }

    String getSonarBaseURL() {
        return SONAR_BASE_URL;
    }

    List<String> updateTeamsFromDB() {
        allTeams = teamRepository.findAll().collect(team -> team.name)
    }

    /** This method exists to allow reference to the TeamStats visualization and provides click through
     * on the webapp. The intent is that the since matches the column date, the type, the row, and the team
     * corresponds to the specific table.
     * @param team The chosen team
     * @param since The date column from the team stats so this must match one of {@link #dateSet()}.
     * @param type The type, 'CODE_SMELL', 'VULNERABILITY', or 'BUG', or 'ALL' gives all three types
     * @return a list of issues
     */
    List<Issue> getTeamIssues(String team, String since, String type) {
        List<String> dates = dateSet()
        List<Issue> issues = getIssuesSince(since)

        List<Issue> resultIssues = issues.findAll { Issue issue ->
            if (teamTypeMatch(issue, team, type)) {
                def date = dates.find { issue.creationDate >= it }
                return date && date == since
            }
            false
        }
        logger.info('Team Issues ' + team + ', ' + since + ', ' + type + ' total issues ' + resultIssues.size())
        return resultIssues
    }

    private boolean teamTypeMatch(Issue issue, String team, String type) {
        (team == 'ALL' || issue.team == team) && (type == 'ALL' || type == issue.type.toString())
    }

    List<TeamStatPoint> getTeamStats(String team) {
        TeamStats teamStats = new TeamStats(allTeams)
        teamStats.dates = dateSet()
        def issues = getIssuesSince(dateSet()[-1])
        teamStats.addIssues(issues)
        return teamStats.getTeamStatPoints(team, project)
    }

    List<String> dateSet() {
        [Project.UPCOMING, timeProvider.today(), timeProvider.yesterday(), timeProvider.lastWeek()]
    }

    List<OldUpcoming> getOldUpcomingIssues() {
        List<OldUpcoming> result = oldUpcomingRepository.findAll()
        logger.info('Posting oldupcoming issues - total = ' + result.size())
        result
    }

    String getNewUpcomingIssues(String teams) {
        logSomething("Showing new upcoming for ${teams} with severities ${THRESHHOLD_BUG} ${THRESHHOLD_CODESMELL} ${THRESHHOLD_VULNERABILITY}")
        String newUpcomingIsWhen = project.isNoBranches() ? timeProvider.daysAgo(project.getUpcomingPeriod()) : Project.UPCOMING;
        String output = ""
        List<String> teamsArray = teams.split(',')
        List<Issue> issues = getIssuesSince(newUpcomingIsWhen).findAll {
            issue -> teamsArray.contains(issue.team) && aboveThreshhold(issue)}
        issues = filterOldUpcoming(issues)
        addNewToOldUpcoming(issues)
        Set<Author> authors = issues.collect { it.authorData }.toSet()
        if (authors.size() == 0) {
            return ''
        }
        output += authors.collect { it.primaryEmail }.join(', ') + '\n'
        authors.each { author ->
            List<Issue> authorIssues = issues.findAll { it.authorData == author }
            output += "<p>${author.name} has ${authorIssues.size()} upcoming issue(s) in feature branches: \n"
            output += issueList(authorIssues)
            output += "</p>\n"
        }
        output
    }

    private String issueList(List<Issue> issues) {
        String output = "<ul>\n"
        issues.each { issue ->
            output += issueListItem(issue)
        }
        output += "</ul>\n"
        output
    }

    private String issueListItem(Issue issue) {
        "  <li>${issue.severity} - ${issue.type} - <a href=${issueLink(issue)}>${issue.message}</a><br/>\n" +
                "${issue.component}</li>\n"
    }

    private String issueLink(Issue issue) {
        "${SONAR_BASE_URL}/project/issues?id=${issue.projectName}&issues=${issue.key}&open=${issue.key}"
    }

    private List<Issue> filterOldUpcoming(List<Issue> issues) {
        issues.findAll { issue ->
            !oldUpcomingRepository.findByKey(issue.key)
        }
    }

    private void addNewToOldUpcoming(List<Issue> newUpcoming) {
        newUpcoming.forEach { issue ->
            if (!oldUpcomingRepository.findByKey(issue.key)) {
                OldUpcoming nowOldIssue = new OldUpcoming()
                nowOldIssue.key = issue.key
                nowOldIssue.component = issue.component
                oldUpcomingRepository.save(nowOldIssue)
            }
        }
    }

    List<Issue> getIssuesSince(String since, String before = Project.UPCOMING) {
        if (since == 'today') {
            since = timeProvider.today()
        }
        def issues = issueRepository.findByCreationDateGreaterThanEqual(since)
        return issues.findAll{ it.creationDate <= before }.sort()
    }

    Metrics getMetrics() {
        Metrics metrics = new Metrics()
        setCoverageAndBugs(metrics)
        setDebt(metrics)
        return metrics
    }

    private void setDebt(Metrics metrics) {
        metrics.debt = 0
        project.main.forEach({prj ->
            def debtLocation = ISSUES_SEARCH +
                    "?componentKeys=${prj}" +
                    '&facetMode=effort' +
                    '&facets=types' +
                    '&types=CODE_SMELL' +
                    '&resolved=false'

            metrics.debt += (getJsonFromURL(debtLocation).effortTotal / MINUTES_PER_DAY).round(1)
        })
    }

    private void setCoverageAndBugs(Metrics metrics) {
        metrics.coverage = 0
        def totalLines = 0
        def coveredLines = 0
        project.main.forEach { prj ->
            def coverageLocation = "/api/measures/component?component=${prj}&metricKeys=lines,coverage,bugs"
            def measures = getJsonFromURL(coverageLocation).component.measures
            def lines = Integer.parseInt(measures.find{element -> element.metric == "lines"}["value"])
            metrics.bugs += Integer.parseInt(measures.find{element -> element.metric == "bugs"}["value"])
            coveredLines += Double.parseDouble(measures.find{element -> element.metric == "coverage"}["value"]) * lines / 100.0
            totalLines += lines
        }

        Double beforeRound = coveredLines * 100 / totalLines
        metrics.coverage = beforeRound.round(1)
    }

    List<Issue> makeIssuesFromSonarJson(String sonarqubeIssueJason) {
        def resultJson = jsonSlurper.parseText(sonarqubeIssueJason)
        return resultJson.issues.collect {
            new Issue(it, getAuthorFromDb(it.author))
        }
    }

    List<Issue> makeIssuesFromIssueJson(String issueJason) {
        def resultJson = jsonSlurper.parseText(issueJason)
        return resultJson.collect {
            // we've already processed the original SonarQube issue data, and need to reprocess
            // todo let's keep original SonarQube issue data, move authorEmail back to author, switch to authorName
            it.author = it.authorEmail
            new Issue(it, getAuthorFromDb(it.authorEmail))
        }
    }

    List<Issue> updateMasterIssues() {
        List<Issue> masterIssues = []
        project.main.forEach { prj ->
            masterIssues.addAll(getAllIssuesCreatedAfter(getIssuesSinceDate(), prj))
        }
        return masterIssues
    }

    private String getIssuesSinceDate() {
        return timeProvider.weeksAgo(SQWATCH_WEEKS_OF_MASTER_ISSUES)
    }

    List<Issue> updateBranchIssues() {
        List<String> projects = getProjects()
        List<Issue> branchIssues = new ArrayList<>()
        projects.forEach { project ->
            logger.info('Loading issues for feature/bug branch ' + project)
            branchIssues.addAll(getProjectIssues(project))
        }
        logger.info('Finished loading all branch issues')
        branchIssues
    }

    @Transactional
    void replaceIssuesWithUpdates(List<Issue> updatedMasterIssues, List<Issue> updatedBranchIssues) {
        issueRepository.deleteAll()
        updatedMasterIssues.each { issue ->
            issueRepository.save(issue)
        }
        updatedBranchIssues.each { issue ->
            if (!isDupe(issue)) {
                issueRepository.save(issue)
            }
        }
    }

    private isDupe(Issue issue) {
        List<Issue> possibleDupes =
                issueRepository.findByComponentNameAndFileNameAndRuleAndStartLine(
                        issue.componentName, issue.fileName, issue.rule, issue.startLine)
        possibleDupes.find { dupe -> issue.isEquivalent(dupe) }
    }

    List<Issue> getProjectIssues(String project) {
        getAllIssuesCreatedAfter(
                timeProvider.weeksAgo(SQWATCH_WEEKS_OF_BRANCH_ISSUES), URLEncoder.encode(project, 'UTF-8'), 1)
    }

    private List<Issue> getAllIssuesCreatedAfter(afterDate, project, pageNo = 1, updating = false) {
        def pageQuery = pageNo == 1 ? '' : '&p=' + pageNo
        def issuesLocation = ISSUES_SEARCH + "?componentKeys=${project}&resolved=false&createdAfter="
        def fullUrl = issuesLocation + afterDate + pageQuery
        def resultJson = getJsonFromURL(fullUrl)
        def total = resultJson.total
        def pageSize = resultJson.ps
        def collectedResult = resultJson.issues.collect { issueStub ->
            Author authorData = getAuthorFromDb(issueStub.author)
            return new Issue(issueStub, authorData)
        }
        def nextPage = nextPage(total, pageNo, pageSize)

        if (nextPage == -1) {
            return collectedResult
        }
        return collectedResult + getAllIssuesCreatedAfter(afterDate, project, nextPage, updating)
    }

    List<Issue> getIssuesResolvedAfter(afterDate, beforeDate, project, pageNo = 1) {
        def pageQuery = pageNo == 1 ? '' : '&p=' + pageNo
        def issuesLocation = ISSUES_SEARCH + "?componentKeys=${project}&resolved=true"
        def fullUrl = issuesLocation + pageQuery
        def resultJson = getJsonFromURL(fullUrl)
        def total = resultJson.total
        def pageSize = resultJson.ps
        def filteredResult = resultJson.issues.findAll { it.closeDate > afterDate && it.closeDate < beforeDate }
        def collectedResult = filteredResult.collect { issueStub ->
            Author authorData = getAuthorFromDb(issueStub.author)
            Issue issue = new Issue(issueStub, authorData)
            return issue
        }
        def nextPage = nextPage(total, pageNo, pageSize)

        if (nextPage == -1) {
            return collectedResult
        }
        return collectedResult + getIssuesResolvedAfter(afterDate, beforeDate, project, nextPage)
    }

    String getTeamFromEmail(String authorEmail) {
        getAuthorFromDb(authorEmail, false)?.team ?: Team.OTHER
    }

    private Author getAuthorFromDb(String authorEmail, boolean save = true) {
        Author authorData = authorRepository.findByPrimaryEmail(authorEmail)
        if (!authorData) {
            authorData = authorRepository.findBySecondariesContaining(authorEmail)
        }
        if (!authorData) {
            authorData = getAuthorFromMailMapAndSave(authorEmail, save, authorData)
        }
        return authorData
    }

    private Author getAuthorFromMailMapAndSave(String authorEmail, boolean save, Author authorData) {
        String authorName = mailMap.getNameFromEmail(authorEmail)
        authorData = authorRepository.findByName(authorName)
        if (authorData) {
            authorData.secondaryEmails += authorEmail
        } else {
            authorData = new Author(authorName, authorEmail, Team.OTHER)
        }
        if (save) {
            authorRepository.save(authorData)
        }
        authorData
    }

    List<String> getProjects(int pageNo = 1) {
        if (project.isNoBranches()) {
            return []
        }
        def pageQuery = pageNo == 1 ? '' : '?p=' + pageNo
        def resultJson = getJsonFromURL(PROJECTS_SEARCH + pageQuery)
        def total = resultJson.paging.total
        def pageSize = resultJson.paging.pageSize
        def collectedResult = resultJson.components.collect { it }
        def nextPage = nextPage(total, pageNo, pageSize)
        def projectNames = collectedResult.findAll { it.key.startsWith(project.branchPrefix)} .collect { it.key }

        if (nextPage == -1) {
            return projectNames
        }
        return projectNames + getProjects(nextPage)
    }

    private static nextPage(totalIssues, pageIndex, pageSize) {
        pageSize * pageIndex >= (Math.min(totalIssues, MAX_ISSUES)) ? -1 : pageIndex + 1
    }

    private Object getJsonFromURL(String apiLocation) {
        jsonSlurper.parseText(urlFetcher.getJsonFromURL(SONAR_BASE_URL + apiLocation, SONAR_BASE_AUTH))
    }

    void logRefreshStart() {
        logger.info("Refreshing SQWatch issue database from ${SONAR_BASE_URL}.")
    }

    void logRefreshComplete() {
        logger.info("Fetched ${issueRepository.count()} issues from ${SONAR_BASE_URL}")
    }

    void logSomething(String message) {
        logger.info(message);
    }

    boolean aboveThreshhold(Issue issue) {
        return (issue.type == IssueType.BUG && issue.severity <= bugThreshhold) ||
                (issue.type == IssueType.CODE_SMELL && issue.severity <= smellThreshhold) ||
                (issue.type == IssueType.VULNERABILITY && issue.severity <= vulnerabilityThreshhold) ||
                (issue.type == IssueType.SECURITY_HOTSPOT && issue.severity <= vulnerabilityThreshhold)
    }
}
