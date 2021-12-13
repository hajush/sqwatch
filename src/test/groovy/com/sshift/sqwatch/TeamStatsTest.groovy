package com.sshift.sqwatch

import groovy.mock.interceptor.StubFor
import org.junit.Before
import org.junit.Test
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

class TeamStatsTest {

    private static final double DAY_DELTA = 0.0001
    private static final int DEFAULT_COLUMNS = 4
    private static final Author FRANKLYN = new Author(
            'Franklin Roosevelt', 'franklin.roosevelt@mycompany.com', 'giants')
    private static final Author ALEXANDER = new Author(
            'Alexander Hamilton', 'alexander.hamilton@mycompany.com', 'giants')
    private project = new Project()
    private final static List<String> ALL_TEAMS = ['giants', 'dolphins', 'packers', 'vikings', Team.OTHER]

    @Before
    void init() {
        project = new Project()
        project.main = 'myproj'
        project.branchPrefix = 'branches-'
    }

    @Test
    void testNoIssues() {
        TeamStats stats = new TeamStats(ALL_TEAMS)
        ScanSonar scanSonar = new ScanSonar()
        stats.setDates(scanSonar.dateSet())
        def points = stats.getTeamStatPoints('giants', project)
        assertThat(points, hasSize(DEFAULT_COLUMNS))
        points.forEach(this.&assertZeroPoint)
    }

    private void assertZeroPoint(TeamStatPoint point) {
        assertThat(point.bugCount, is(0))
        assertThat(point.vulnerabilityCount, is(0))
        assertThat(point.codeSmellCount, is(0))
        assertThat(point.debtInDays, is((double)0.0))
        assertThat(point.issueCount, is(0))
    }

    @Test
    void testZeroTablesForAllTeamsWhenNoIssues() {
        TeamStats stats = new TeamStats(ALL_TEAMS)
        ScanSonar scanSonar = new ScanSonar()
        stats.setDates(scanSonar.dateSet())
        def points = stats.getTeamStatPoints(TeamStats.ALL_TEAMS, project)
        assertThat(points, hasSize(ALL_TEAMS.size() * DEFAULT_COLUMNS))
        points.forEach(this.&assertZeroPoint)
    }

    @Test
    void testOneMatch() {
        TeamStats stats = new TeamStats(ALL_TEAMS)
        Issue oneIssue = new Issue([
                'key': 'test',
                'message': 'Yo',
                'component': 'h:dd',
                'author': 'frankliN.roosevelt@mycompany.com',
                'severity': 'INFO',
                'type': 'CODE_SMELL',
                'rule': 'squid:S1135',
                'debt': '30min',
                'creationDate': '2020-11-15T00:24:39-0700',
                'shortComponent': 'component-File.java'],
                new Author('Franklin Roosevelt', 'franklin.roosevelt@mycompany.com', 'giants'))
        stats.addIssues([oneIssue])
        List<TeamStatPoint> statPoints = stats.getTeamStatPoints('giants', project)
        assertThat(statPoints[0].team, is('giants'))
        assertThat(statPoints[0].issueCount, is(1))
        assertThat(statPoints[0].codeSmellCount, is(1))
        assertThat(statPoints[0].bugCount, is(0))
        assertThat(statPoints[0].vulnerabilityCount, is(0))
        assertThat(statPoints[0].debtInDays, closeTo((double)(30.0 / (60 * 8)), DAY_DELTA))
    }

    @Test
    void testThreeIssueThreeDates() {
        TeamStats stats = makeSimpleStats()
        def TODAY = '2020-11-15'
        def YESTERDAY = '2020-11-14'
        def LAST_WEEK = '2020-11-08'
        stats.dates = [TODAY, YESTERDAY, LAST_WEEK]
        List<TeamStatPoint> statPoints = stats.getTeamStatPoints('giants', project)
        def greenOnly = statPoints.findAll { it.team == 'giants' }
        assertThat(greenOnly, hasSize(3)) // one for each date point
        validateStats(greenOnly, TODAY, 1, 0, 5)
        validateStats(greenOnly, YESTERDAY, 1, 0, 10)
        validateStats(greenOnly, LAST_WEEK, 1, 0, 20)
    }

    @Test
    void testThreeIssueAllDates() {
        TeamStats stats = makeSimpleStats()
        List<TeamStatPoint> statPoints = stats.getTeamStatPoints('giants', project)
        assertThat(statPoints, hasSize(1)) // one for each date point
        validateStats(statPoints, '!ALL', 3, 0, 35)
    }

    private TeamStats makeSimpleStats() {
        TeamStats stats = new TeamStats(ALL_TEAMS)
        stats.addIssues(threeSimpleIssues())
        stats
    }

    static List<Issue> threeSimpleIssues() {[
            new Issue([
                //Added "today", pretend today is 11/15/2020
                'key'         : 'test1',
                'component'   : 'myproj:Shaman/src/main/java/com/thecompany/theproj/Siren.java',
                'author'      : 'franklin.roosevelt@mycompany.com',
                'debt'        : '5min',
                'type'        : 'CODE_SMELL',
                'creationDate': '2020-11-15T00:24:39-0700'], FRANKLYN),
            new Issue([
                //Added before today and on or after 10/8
                'key'         : 'test2',
                'component'   : 'myproj:Shaman/src/main/java/com/thecompany/theproj/Siren.java',
                'author'      : 'franklin.roosevelt@mycompany.com',
                'debt'        : '10min',
                'type'        : 'CODE_SMELL',
                'creationDate': '2020-11-14T00:24:39-0700'], FRANKLYN),
            new Issue([
                //Added before and on or after 10/2
                'key'         : 'test3',
                'component'   : 'myproj:Shaman/src/main/java/com/thecompany/theproj/Siren.java',
                'author'      : 'franklin.roosevelt@mycompany.com',
                'debt'        : '20min',
                'type'        : 'CODE_SMELL',
                'creationDate': '2020-11-08T00:24:39-0700'], FRANKLYN)
    ]}

    @Test
    void testAllTogether() {
        TeamStats stats = new TeamStats(ALL_TEAMS)
        ScanSonar scanSonar = new ScanSonar()

        def mockAuthorRepository = new StubFor(AuthorRepository)
        mockAuthorRepository.use {
            scanSonar.authorRepository = ScanSonarTest.fakeAuthorRepository
            List<Issue> issues = scanSonar.makeIssuesFromSonarJson(
                    ScanSonarTest.fetchSonarData('2020-11-12'))
            stats.addIssues(issues)
            List<TeamStatPoint> statPoints = stats.getTeamStatPoints('ALL', project)
            def greenOnly = statPoints.findAll { it.team == 'giants' }
            assertThat(greenOnly, hasSize(1)) // one for each date point
            def greenToday = greenOnly.find { it.date == TeamStats.ALL_DATES }
            assertThat(greenToday.bugCount, is(0))
            assertThat(greenToday.issueCount, is(10))
            assertThat(greenToday.debtInDays, closeTo((double) 0.21458, DAY_DELTA))
        }
    }

    @Test
    void testGreenOnly() {
        TeamStats stats = new TeamStats(ALL_TEAMS)
        ScanSonar scanSonar = new ScanSonar()

        def mockAuthorRepository = new StubFor(AuthorRepository)
        mockAuthorRepository.use {
            scanSonar.authorRepository = ScanSonarTest.fakeAuthorRepository
            List<Issue> issues = scanSonar.makeIssuesFromSonarJson(
                    ScanSonarTest.fetchSonarData('2020-11-12'))
            stats.addIssues(issues)
            List<TeamStatPoint> statPoints = stats.getTeamStatPoints('giants', project)
            assertThat(statPoints, hasSize(1)) // one for each date point
            def greenToday = statPoints.find { it.date == TeamStats.ALL_DATES }
            assertThat(greenToday.bugCount, is(0))
            assertThat(greenToday.issueCount, is(10))
            assertThat(greenToday.debtInDays, closeTo((double) 0.21458, DAY_DELTA))
        }
    }


    @Test
    void testTodayYesterdayLastWeek() {
        TeamStats stats = new TeamStats(ALL_TEAMS)
        ScanSonar scanSonar = new ScanSonar()
        def mockAuthorRepository = new StubFor(AuthorRepository)
        mockAuthorRepository.use {
            scanSonar.authorRepository = ScanSonarTest.fakeAuthorRepository

            List<Issue> issues = scanSonar.makeIssuesFromSonarJson(
                    ScanSonarTest.fetchSonarData('2020-11-12'))
            stats.addIssues(issues)
            def TODAY = '2020-11-16' // 54 issues (10 green, 37 other, 7 vikings)
            def YESTERDAY = '2020-11-15' // 28 issues (4 green, x other)
            def LAST_WEEK = '2020-11-09' // 0 issues
            stats.dates = [TODAY, YESTERDAY, LAST_WEEK]
            List<TeamStatPoint> statPoints = stats.getTeamStatPoints('ALL', project)

            def giantsOnly = statPoints.findAll { it.team == 'giants' }
            assertThat(giantsOnly, hasSize(3)) // one for each date point
            validateStats(giantsOnly, TODAY, 6, 0, 78)
            validateStats(giantsOnly, YESTERDAY, 4, 0, 25)
            validateStats(giantsOnly, LAST_WEEK, 0, 0, 0)

            def otherOnly = statPoints.findAll { it.team == Team.OTHER }
            assertThat(otherOnly, hasSize(3)) // one for each date point
            validateStats(otherOnly, TODAY, 20, 1, 299)
            validateStats(otherOnly, YESTERDAY, 10, 0, 775)
            validateStats(otherOnly, LAST_WEEK, 7, 1, 122)

            def vikingsOnly = statPoints.findAll { it.team == 'vikings' }
            assertThat(vikingsOnly, hasSize(3)) // one for each date point
            validateStats(vikingsOnly, TODAY, 0, 0, 0)
            validateStats(vikingsOnly, YESTERDAY, 0, 0, 0)
            // last week 5 andrey - 34, 2 vivek 40
            validateStats(vikingsOnly, LAST_WEEK, 7, 0, 74)
        }
    }

    @Test
    void addOneUpcoming() {
        TeamStats stats = makeSimpleStats()
        def UPCOMING = 'UPCOMING'
        def TODAY = '2020-11-15'
        def YESTERDAY = '2020-11-14'
        def LAST_WEEK = '2020-11-08'
        stats.dates = [UPCOMING, TODAY, YESTERDAY, LAST_WEEK]
        stats.addIssues([new Issue([
                'key'         : 'test4',
                'component'   : 'featurebranch-quartz:Shaman/src/main/java/com/thecompany/theproj/Siren.java',
                'author'      : 'franklin.roosevelt@mycompany.com',
                'debt'        : '15min',
                'type'        : 'CODE_SMELL',
                'creationDate': '2020-12-08T00:24:39-0700'], FRANKLYN)])
        List<TeamStatPoint> statPoints = stats.getTeamStatPoints('giants', project)
        def greenOnly = statPoints.findAll { it.team == 'giants' }
        assertThat(greenOnly, hasSize(4)) // one for each date point
        validateStats(greenOnly, UPCOMING, 1, 0, 15)
        validateStats(greenOnly, TODAY, 1, 0, 5)
        validateStats(greenOnly, YESTERDAY, 1, 0, 10)
        validateStats(greenOnly, LAST_WEEK, 1, 0, 20)
    }

    @Test
    void addDuplicateUpcomingsWithDifferentKeys() {
        TeamStats stats = new TeamStats(ALL_TEAMS)
        ScanSonar scanSonar = new ScanSonar()

        def mockAuthorRepository = new StubFor(AuthorRepository)
        mockAuthorRepository.use {
            scanSonar.authorRepository = ScanSonarTest.fakeAuthorRepository
            List<Issue> issues = scanSonar.makeIssuesFromSonarJson(
                    ScanSonarTest.fetchSonarData('2020-11-12'))
            stats.addIssues(issues)

            // add two duplicates but from branch components
            stats.addIssues([
                    new Issue([
                            'key'         : 'AW20MA7fi32mTkBv55tx',
                            'rule'        : 'squid:S00117',
                            'severity'    : 'MINOR',
                            'component'   : 'featurebranch-quartz:Hand/src/main/java/com/thecompany/theproj/SnowWriting.java',
                            'textRange'   : ['startLine': 38, 'endLine': 38, 'startOffset': 26, 'endOffset': 41],
                            'message'     : 'Rename this local variable to match the regular expression "^[a-z][a-zA-Z0-9]*\$".',
                            'debt'        : '2min',
                            'author'      : 'alexander.hamilton@mycompany.com',
                            'creationDate': '2020-11-16T00:26:03-0700',
                            'type'        : 'CODE_SMELL',
                    ], ALEXANDER),
                    new Issue([
                            'key'         : 'AW20MA7fi32mTkBv55ty',
                            'rule'        : 'squid:S00117',
                            'severity'    : 'MINOR',
                            'component'   : 'featurebranch-diamond:Hand/src/main/java/com/thecompany/theproj/SnowWriting.java',
                            'textRange'   : ['startLine': 74, 'endLine': 74, 'startOffset': 26, 'endOffset': 41],
                            'status'      : 'OPEN',
                            'message'     : 'Rename this local variable to match the regular expression "^[a-z][a-zA-Z0-9]*\$".',
                            'debt'        : '2min',
                            'author'      : 'alexander.hamilton@mycompany.com',
                            'creationDate': '2020-11-16T00:26:03-0700',
                            'type'        : 'CODE_SMELL',
                    ], ALEXANDER)
            ])

            List<TeamStatPoint> statPoints = stats.getTeamStatPoints('ALL', project)
            def greenOnly = statPoints.findAll { it.team == 'giants' }
            assertThat(greenOnly, hasSize(1)) // one for each date point
            def greenToday = greenOnly.find { it.date == TeamStats.ALL_DATES }
            assertThat(greenToday.bugCount, is(0))
            assertThat(greenToday.issueCount, is(10))
            assertThat(greenToday.debtInDays, closeTo((double) 0.21458, DAY_DELTA))
        }
    }

    private void validateStats(stats, date, issueCount, bugCount, debtMinutes) {
        def statPoint = stats.find { it.date == date }
        def message = "expected ${statMessage(issueCount, bugCount, debtMinutes)} actual " +
                "${statMessage(statPoint.issueCount, statPoint.bugCount, statPoint.debtInDays*8*60)}"
        assertThat(message, statPoint.bugCount, is(bugCount))
        assertThat(message, statPoint.issueCount, is(issueCount))
        assertThat(message, statPoint.debtInDays, closeTo((double) (debtMinutes/60.0) / 8, DAY_DELTA))
    }

    private String statMessage(issueCount, bugCount, debtMinutes) {
        return "${issueCount}, ${bugCount}, ${debtMinutes}"
    }
}
