package com.sshift.sqwatch

import groovy.mock.interceptor.MockFor
import org.junit.Before
import org.springframework.beans.factory.annotation.Value

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.junit.Test
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

class ScanSonarTest {
    private final static String GIANTS = 'giants'
    private final static String DOLPHINS = 'dolphins'
    private final static String VIKINGS = 'vikings'
    private final static String PACKERS = 'packers'
    private final static List<String> ALL_TEAMS = [GIANTS, DOLPHINS, PACKERS, VIKINGS, Team.OTHER]
    private final static int NUM_TEAMS = ALL_TEAMS.size()
    static def MOCK_TEAM_MAP = [
            'Franklin Roosevelt' : GIANTS,
            'Burt Lancaster': GIANTS,
            'Cary Grant': GIANTS,
            'Lawrence Olivier': GIANTS,
            'Clark Gable': GIANTS,
            'Alexander Hamilton': GIANTS,
            'Johnny Appleseed' : DOLPHINS,
            'Dante Alighieri': DOLPHINS,
            'Marlon Brando': PACKERS,
            'John Adams': PACKERS,
            'George Washington': VIKINGS,
            'Patrick Henry': VIKINGS,
            'John Wayne': VIKINGS,
            'Robert Mitchum': VIKINGS,
            'John Hancock': VIKINGS
    ]
    private static final Author ahamilton = new Author(
            'Alexander Hamilton', 'alexander.hamilton@mycompany.com', GIANTS, ['ahamilton@mycompany.com'])
    private static final Author tjefferson = new Author(
            'Thomas Jefferson', 'tjefferson@mycompany.com', Team.OTHER)

    private static MailMap MAIL_MAP = new MailMap(MailMap.class.getClassLoader().getResourceAsStream(".mailmap"))
    static AuthorRepository fakeAuthorRepository = [
            findByPrimaryEmail: { email ->
                new Author(MAIL_MAP.getNameFromEmail(email), email, getTeamMock(MAIL_MAP.getNameFromEmail(email))) }
    ] as AuthorRepository

    private static String getTeamMock(String email) {
        return MOCK_TEAM_MAP[email] ?: Team.OTHER
    }

    private ScanSonar scanSonar
    private Project project

    @Before
    void init() {
        scanSonar = new ScanSonar()
        scanSonar.SONAR_BASE_URL = 'http://mycompany.com'
        scanSonar.SONAR_BASE_MASTER_PROJECT = 'myproj'
        project = new Project()
        project.main = 'myproj'
        project.branchPrefix = 'featurebranch-'
        scanSonar.project = project
        scanSonar.SONAR_BASE_AUTH = 'fake:auth'
        scanSonar.mailMap = MAIL_MAP
        scanSonar.SQWATCH_WEEKS_OF_MASTER_ISSUES = 8
        scanSonar.SQWATCH_WEEKS_OF_BRANCH_ISSUES = 3
    }

    @Test
    void testAuthorsFromIssuesJson() {
        scanSonar.authorRepository = fakeAuthorRepository
        List<Issue> issues = scanSonar.makeIssuesFromIssueJson(
                fetchSQWatchIssueData('2020-10-08'))
        assertThat(issues, hasSize(1454))
        assertThat(issues.findAll { it.author == 'Samuel Adams' }, hasSize(20))
        assertThat(issues.findAll { it.author == 'Franklin Roosevelt' }, hasSize(1))
        assertThat(issues.findAll { it.author == 'George Washington' }, hasSize(266))
    }

    @Test
    void testStatsOffSonarJson() {
        scanSonar.authorRepository = fakeAuthorRepository
        List<Issue> issues = scanSonar.makeIssuesFromSonarJson(
                fetchSonarData('2020-11-15'))
        TeamStats teamStats = new TeamStats(ALL_TEAMS)
        teamStats.dates = ['2020-11-15', '2020-11-14', '2020-11-08']
        teamStats.addIssues(issues)
        List statPoints = teamStats.getTeamStatPoints('giants', project)
        TeamStatPoint point = statPoints[2] // 2020-11-08
        assertThat(point.issueCount, is(4))
        assertThat(point.debtInDays, closeTo((double) (25.0 / 60 / 8), (double) 0.0001))
        List statPointsAll = teamStats.getTeamStatPoints('ALL', project)
        assertThat(statPointsAll, hasSize(15))
    }

    @Test
    void testStatsFromIssuesJson() {
        scanSonar.authorRepository = fakeAuthorRepository
        List<Issue> issues = scanSonar.makeIssuesFromIssueJson(
                fetchSQWatchIssueData('2020-10-08'))
        TeamStats teamStats = new TeamStats(ALL_TEAMS)
        teamStats.dates = ['2020-11-15', '2020-11-14', '2020-11-09']
        teamStats.addIssues(issues)
        List statPoints = teamStats.getTeamStatPoints('giants', project)
        assertThat(statPoints, hasSize(3))
        assertThat(statPoints[0].issueCount, is(1))
        assertThat(statPoints[1].issueCount, is(0))
        assertThat(statPoints[2].issueCount, is(20))

        List statPointsAll = teamStats.getTeamStatPoints('ALL', project)
        assertThat(statPointsAll, hasSize(teamStats.dates.size() * NUM_TEAMS))
    }

    static String fetchSonarData(date) {
        return getTestResource('sonardata', date, getClass().getClassLoader())
    }

    static String fetchSQWatchIssueData(date) {
        return getTestResource('issues', date, getClass().getClassLoader())
    }

    static String getTestResourceDataPage(data, page) {
        getTestResource(data, page, getClass().getClassLoader())
    }

    private static String getTestResource(prefix, date, ClassLoader res) {
        def fileName = "${prefix}-${date}.json"
        if (res) {
            return res.getResource(fileName).getFile().text
        } else {
            return new File("src/test/resources/${fileName}").text
        }
    }

    @Test
    void testFetchIssues() {
        def fakeUrlFetcher = [getJsonFromURL: { url, userpass ->
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=myproj&resolved=false&createdAfter=2020-12-22')
                return getTestResourceDataPage('sonardata-2020-12-22', 1)
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=myproj&resolved=false&createdAfter=2020-12-22&p=2')
                return getTestResourceDataPage('sonardata-2020-12-22', 2)
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=myproj&resolved=false&createdAfter=2020-12-22&p=3')
                return getTestResourceDataPage('sonardata-2020-12-22', 3)
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=myproj&resolved=false&createdAfter=2020-12-22&p=4')
                return getTestResourceDataPage('sonardata-2020-12-22', 4)
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=myproj&resolved=false&createdAfter=2020-12-22&p=5')
                return getTestResourceDataPage('sonardata-2020-12-22', 5)
            return '{"total":0,"ps":100,"paging":{"pageSize":100,"total":0},"issues":[]}'
        }] as URLFetcher

        List<Issue> issues = []
        scanSonar.timeProvider = [now: { -> Instant.parse('2021-01-06T15:15:30.00Z')}] as TimeProvider
        scanSonar.urlFetcher = fakeUrlFetcher
        scanSonar.authorRepository = fakeAuthorRepository
        scanSonar.issueRepository = [
                save: { issue -> },
                deleteByProjectName: { projectName -> },
                deleteBranchIssues: { -> },
                findByCreationDateGreaterThanEqual: { creationDate -> issues.findAll { it.creationDate >= creationDate }}
        ] as IssueRepository
        issues = scanSonar.getAllIssuesCreatedAfter('2020-12-22', 'myproj')
        assertThat(issues, hasSize(483))
        List<Issue> giantsIssues = scanSonar.getTeamIssues(Team.OTHER, '2020-12-30', 'ALL')
        assertThat(giantsIssues, hasSize(74))
    }

    @Test
    void testFetchProjects() {
        def mockUrlFetcher = new MockFor(URLFetcher)
        mockUrlFetcher.demand.getJsonFromURL { url, userpass ->
            if (url == 'http://mycompany.com/api/projects/search')
                return getTestResourceDataPage('project-search', 1)
        }
        mockUrlFetcher.demand.getJsonFromURL { url, userpass ->
            if (url == 'http://mycompany.com/api/projects/search?p=2')
                return getTestResourceDataPage('project-search', 2)
        }

        mockUrlFetcher.use {
            scanSonar.urlFetcher = new URLFetcher(null)
            List<Issue> actualProjects = scanSonar.getProjects()
            assertThat(actualProjects, is([
                    'featurebranch-respect',
                    'featurebranch-memory',
                    'featurebranch-way',
                    'featurebranch-toes',
                    'featurebranch-field',
                    'featurebranch-leg',
                    'featurebranch-argument',
                    'featurebranch-snails',
                    'featurebranch-pail',
                    'featurebranch-rings',
                    'featurebranch-desk',
                    'featurebranch-crook',
                    'featurebranch-army',
                    'featurebranch-cent',
                    'featurebranch-reason',
                    'featurebranch-thumb',
                    'featurebranch-snake',
                    'featurebranch-voyage',
                    'featurebranch-fall',
                    'featurebranch-squirrel',
                    'featurebranch-stone',
                    'featurebranch-scene',
                    'featurebranch-powder',
                    'featurebranch-frogs',
                    'featurebranch-ducks',
                    'featurebranch-sofa',
                    'featurebranch-letters',
                    'featurebranch-jellyfish',
                    'featurebranch-butter',
                    'featurebranch-canvas',
                    'featurebranch-range',
                    'featurebranch-smile',
                    'featurebranch-harmony',
                    'featurebranch-cover',
                    'featurebranch-scissors',
                    'featurebranch-burst',
                    'featurebranch-statement',
                    'featurebranch-reading',
                    'featurebranch-mom',
                    'featurebranch-trees',
                    'featurebranch-scarecrow',
                    'featurebranch-card',
                    'featurebranch-theory',
                    'featurebranch-seashore',
                    'featurebranch-ray',
                    'featurebranch-engine',
                    'featurebranch-plastic',
                    'featurebranch-morning',
                    'featurebranch-oranges',
                    'featurebranch-wrist',
                    'featurebranch-secretary',
                    'featurebranch-tail',
                    'featurebranch-hair',
                    'featurebranch-room',
                    'featurebranch-box',
                    'featurebranch-boundary',
                    'featurebranch-circle',
                    'featurebranch-daughter',
                    'featurebranch-recess',
                    'featurebranch-throat',
                    'featurebranch-name',
                    'featurebranch-mice',
                    'featurebranch-clover',
                    'featurebranch-houses',
                    'featurebranch-noise',
                    'featurebranch-paper',
                    'featurebranch-peace',
                    'featurebranch-drain',
                    'featurebranch-cause',
                    'featurebranch-science',
                    'featurebranch-airplane',
                    'featurebranch-match',
                    'featurebranch-coal',
                    'featurebranch-mind',
                    'featurebranch-frame',
                    'featurebranch-stitch',
                    'featurebranch-dress',
                    'featurebranch-slave',
                    'featurebranch-fog',
                    'featurebranch-railway',
                    'featurebranch-rain',
                    'featurebranch-calculator',
                    'featurebranch-library',
                    'featurebranch-salt',
                    'featurebranch-camp',
                    'featurebranch-addition',
                    'featurebranch-laugh',
                    'featurebranch-church',
                    'featurebranch-food',
                    'featurebranch-shirt',
                    'featurebranch-week',
                    'featurebranch-steel',
                    'featurebranch-market',
                    'featurebranch-quartz',
                    'featurebranch-person',
                    'featurebranch-bait',
                    'featurebranch-position',
                    'featurebranch-nation',
                    'featurebranch-brake',
                    'featurebranch-power',
                    'featurebranch-drop',
                    'featurebranch-trade',
                    'featurebranch-back',
                    'featurebranch-month',
                    'featurebranch-cannon',
                    'featurebranch-pest',
                    'featurebranch-kiss',
                    'featurebranch-property',
                    'featurebranch-ship',
                    'featurebranch-skin',
                    'featurebranch-look',
                    'featurebranch-flavor',
                    'featurebranch-head',
                    'featurebranch-servant',
                    'featurebranch-motion',
                    'featurebranch-liquid'
            ]))
        }
    }

    @Test
    void testMetricsOneMasterProject() {
        def fakeURLFetcher = [
                getJsonFromURL: { url, userpass ->
                    if (url == 'http://mycompany.com/api/measures/component?component=myproj&metricKeys=lines,coverage,bugs')
                        return getTestResourceDataPage('metrics', 1)
                    else if (url == 'http://mycompany.com/api/issues/search?' +
                            'componentKeys=myproj&facetMode=effort&facets=types&types=CODE_SMELL&resolved=false')
                        return getTestResourceDataPage('debt', 1)
                    else
                        return ''
                }
        ] as URLFetcher

        scanSonar.urlFetcher = fakeURLFetcher
        Metrics metrics = scanSonar.getMetrics()
        assertThat(metrics.debt, is((double) 1999.1))
        assertThat(metrics.coverage, is('25.0'))
        assertThat(metrics.bugs, is(43))
    }

    @Test
    void testMetricsMultipleMasterProjects() {
        scanSonar.project.main = 'myproj,myproj2'
        def fakeURLFetcher = [
                getJsonFromURL: { url, userpass ->
                    if (url == 'http://mycompany.com/api/measures/component?component=myproj&metricKeys=lines,coverage,bugs')
                        return getTestResourceDataPage('metrics', 1)
                    else if (url == 'http://mycompany.com/api/measures/component?component=myproj2&metricKeys=lines,coverage,bugs')
                        return getTestResourceDataPage('metrics2', 1)
                    else if (url == 'http://mycompany.com/api/issues/search?' +
                            'componentKeys=myproj&facetMode=effort&facets=types&types=CODE_SMELL&resolved=false')
                        return getTestResourceDataPage('debt', 1)
                    else if (url == 'http://mycompany.com/api/issues/search?' +
                            'componentKeys=myproj2&facetMode=effort&facets=types&types=CODE_SMELL&resolved=false')
                        return getTestResourceDataPage('debt2', 1)
                    else
                        return ''
                }
        ] as URLFetcher

        scanSonar.urlFetcher = fakeURLFetcher
        Metrics metrics = scanSonar.getMetrics()
        assertThat(metrics.debt, is((double) 1999.1*2))
        assertThat(metrics.coverage, is('28.8'))
        assertThat(metrics.bugs, is(49))
    }

    @Test
    void testNoUpcoming() {
        def issues = []

        scanSonar.urlFetcher = new URLFetcher(null)
        scanSonar.authorRepository = fakeAuthorRepository
        scanSonar.issueRepository = [
                findByCreationDateGreaterThanEqual: { creationDate -> issues },
                //deleteAll: { -> },
                deleteByProjectName               : { projectName -> },
                deleteBranchIssues                : { -> },
                save                              : { issue -> }
        ] as IssueRepository
        String issueOutput = scanSonar.getNewUpcomingIssues('giants,dolphins')
        assertThat(issueOutput, is(''))
    }

    @Test
    void testTeamUpcoming() {
        def issue1 = [
                key         : 'AW7TTVuTiEm2mPY4NLRr',
                message     : 'Add a nested comment explaining why this method is empty, throw an ' +
                        'UnsupportedOperationException or complete the implementation.',
                component   : 'featurebranch-apple:' +
                        'Hand/src/main/java/com/thecompany/theproj/hand/UniformApple.java',
                severity    : 'CRITICAL',
                type        : 'CODE_SMELL',
                rule        : 'squid:S1186',
                authorEmail : 'alexander.hamilton@mycompany.com',
                creationDate: 'UPCOMING',
                team        : 'giants',
                debtInDays  : 0.0104166666625D
        ]
        def issue2 = [
                key         : 'AW7Kg7CkiEm2mPY4MV5e',
                message     : 'Remove this unused import \'com.thecompany.winter.Seasons\'.',
                component   : 'featurebranch-orange:' +
                        'Respect/src/main/java/com/thecompany/theproj/respect/MiddleWool.java',
                type        : 'CODE_SMELL',
                severity    : 'MINOR',
                rule        : 'squid:UselessImportCheck',
                authorEmail : 'tjefferson@mycompany.com',
                creationDate: 'UPCOMING',
                team        : Team.OTHER,
                effort      : '2min'
        ]

        Issue issueFeature1 = new Issue(issue1, ahamilton)
        Issue issueFeature2 = new Issue(issue2, tjefferson)
        ArrayList<Issue> issues = new ArrayList<>()
        issues.add(issueFeature1)
        issues.add(issueFeature2)
        scanSonar.urlFetcher = [] as URLFetcher // ensure no urls fetched

        def stubOldUpcomingList = []

        scanSonar.urlFetcher = new URLFetcher(null)
        scanSonar.authorRepository = fakeAuthorRepository
        scanSonar.issueRepository = [
                findByCreationDateGreaterThanEqual: { creationDate -> issues },
                //deleteAll: { -> },
                deleteByProjectName               : { projectName -> },
                deleteBranchIssues                : { -> },
                save                              : { issue -> }
        ] as IssueRepository
        scanSonar.oldUpcomingRepository = [
                save     : { oldUpcoming -> stubOldUpcomingList.add(oldUpcoming) },
                findByKey: { key -> stubOldUpcomingList.find { it.key == key } }
        ] as OldUpcomingRepository
        String issueOutput = scanSonar.getNewUpcomingIssues('giants,dolphins')
        assertThat(issueOutput, is('alexander.hamilton@mycompany.com\n' +
                '<p>Alexander Hamilton has 1 upcoming issue(s) in feature branches: \n' +
                '<ul>\n' +
                '  <li>CRITICAL - CODE_SMELL - <a href=http://mycompany.com/project/issues?' +
                'id=featurebranch-apple&' +
                'issues=AW7TTVuTiEm2mPY4NLRr&open=AW7TTVuTiEm2mPY4NLRr>' +
                'Add a nested comment explaining why this method is empty, throw an ' +
                'UnsupportedOperationException or complete the implementation.</a><br/>\n' +
                'featurebranch-apple:' +
                'Hand/src/main/java/com/thecompany/theproj/hand/UniformApple.java</li>\n' +
                '</ul>\n' +
                '</p>\n'))
    }

    @Test
    void testNewUpcomingNoticesIncludeMainBranch() {
        // new upcoming used to only be checked in the upcoming branches, but
        // we really need to check the main branch for new issues that appeared in the last x days.
        // We are hard coding this feature at a week for now (7 days)

        Issue issue1 = new Issue([
                key: '111111111',
                severity: 'CRITICAL',
                type: 'CODE_SMELL',
                authorEmail: ahamilton.getPrimaryEmail(),
                creationDate: "2022-02-10T00:00:00-0700",
                rule        : 'squid:S1186'
        ], ahamilton)
        Issue issue2 = new Issue([
                key: '9999999999',
                severity: 'CRITICAL',
                type: 'CODE_SMELL',
                authorEmail: tjefferson.getPrimaryEmail(),
                creationDate: "2022-02-09T00:00:00-0700",
                rule        : 'squid:S1186'
        ], tjefferson)
        def stubOldUpcomingList = []
        ArrayList<Issue> issues = new ArrayList<>()
        issues.add(issue1)
        issues.add(issue2)
        scanSonar.urlFetcher = [] as URLFetcher // fake the fetcher to ensure no external calls needed
        scanSonar.authorRepository = fakeAuthorRepository
        scanSonar.oldUpcomingRepository = [
                save     : { oldUpcoming -> stubOldUpcomingList.add(oldUpcoming) },
                findByKey: { stubOldUpcomingList.find { it.key == key } }
        ] as OldUpcomingRepository
        scanSonar.issueRepository = [
                findByCreationDateGreaterThanEqual: { creationDate -> issues },
                deleteByProjectName               : { projectName -> },
                deleteBranchIssues                : { -> },
                unbufferUpdatingIssues            : { ->
                    issues.each { issue -> if (issue.updating) issue.setUpdating(false) }
                },
                save                              : { issue -> }
        ] as IssueRepository
        String issueOutput = scanSonar.getNewUpcomingIssues(Team.OTHER)
        assertThat(issueOutput, is('tjefferson@mycompany.com\n' +
                '<p>Thomas Jefferson has 1 upcoming issue(s) in feature branches: \n' +
                '<ul>\n' +
                '  <li>CRITICAL - CODE_SMELL - <a href=http://mycompany.com/project/issues?id=null&issues=9999999999&open=9999999999>null</a><br/>\n' +
                'null</li>\n</ul>\n</p>\n'))
    }

    @Test
    void testNewUpcomingNoticesOnlyOnce() {
        // we want to see if new upcoming notices are raised only once
        def issue1 = [
                key         : 'AW7TTVuTiEm2mPY4NLRr',
                message     : 'Add a nested comment explaining why this method is empty, throw an ' +
                        'UnsupportedOperationException or complete the implementation.',
                component   : 'featurebranch-apple:' +
                        'Mouth/src/main/java/com/thecompany/theproj/mouth/FuelString.java',
                severity    : 'CRITICAL',
                type        : 'CODE_SMELL',
                rule        : 'squid:S1186',
                authorEmail : 'alexander.hamilton@mycompany.com',
                creationDate: 'UPCOMING',
                team        : 'giants',
                debtInDays  : 0.0104166666625D
        ]
        def issue2 = [
                key         : 'AW7Kg7CkiEm2mPY4MV5e',
                message     : 'Remove this unused import \'com.thecompany.winter.Seasons\'.',
                component   : 'featurebranch-orange:' +
                        'Respect/src/main/java/com/thecompany/theproj/respect/MiddleWool.java',
                type        : 'CODE_SMELL',
                severity    : 'MINOR',
                rule        : 'squid:UselessImportCheck',
                authorEmail : 'tjefferson@mycompany.com',
                creationDate: 'UPCOMING',
                team        : Team.OTHER,
                effort      : '2min'
        ]

        Issue issueFeature1 = new Issue(issue1, ahamilton)
        Issue issueFeature2 = new Issue(issue2, tjefferson)
        def stubOldUpcomingList = []
        ArrayList<Issue> issues = new ArrayList<>()
        issues.add(issueFeature1)
        issues.add(issueFeature2)
        scanSonar.urlFetcher = [] as URLFetcher // fake the fetcher to ensure no external calls needed
        scanSonar.authorRepository = fakeAuthorRepository
        scanSonar.oldUpcomingRepository = [
                save     : { oldUpcoming -> stubOldUpcomingList.add(oldUpcoming) },
                findByKey: { key -> stubOldUpcomingList.find { it.key == key } }
        ] as OldUpcomingRepository
        scanSonar.issueRepository = [
                findByCreationDateGreaterThanEqual: { creationDate -> issues },
                //deleteAll: { -> },
                deleteByProjectName               : { projectName -> },
                deleteBranchIssues                : { -> },
                unbufferUpdatingIssues            : { ->
                    issues.each { issue -> if (issue.updating) issue.setUpdating(false) }
                },
                save                              : { issue -> }
        ] as IssueRepository
        String issueOutput = scanSonar.getNewUpcomingIssues('giants,dolphins')
        assertThat(issueOutput, is('alexander.hamilton@mycompany.com\n' +
                '<p>Alexander Hamilton has 1 upcoming issue(s) in feature branches: \n' +
                '<ul>\n' +
                '  <li>CRITICAL - CODE_SMELL - <a href=http://mycompany.com/project/issues?' +
                'id=featurebranch-apple&' +
                'issues=AW7TTVuTiEm2mPY4NLRr&open=AW7TTVuTiEm2mPY4NLRr>' +
                'Add a nested comment explaining why this method is empty, throw an ' +
                'UnsupportedOperationException or complete the implementation.</a><br/>\n' +
                'featurebranch-apple:' +
                'Mouth/src/main/java/com/thecompany/theproj/mouth/FuelString.java</li>\n' +
                '</ul>\n' +
                '</p>\n'))
        issueOutput = scanSonar.getNewUpcomingIssues('giants,dolphins')
        assertThat(issueOutput, is(''))
        issueOutput = scanSonar.getNewUpcomingIssues(Team.OTHER)
        assertThat(issueOutput, is('tjefferson@mycompany.com\n' +
                '<p>Thomas Jefferson has 1 upcoming issue(s) in feature branches: \n' +
                '<ul>\n  <li>MINOR - CODE_SMELL - ' +
                '<a href=http://mycompany.com/project/issues?' +
                'id=featurebranch-orange&' +
                'issues=AW7Kg7CkiEm2mPY4MV5e&open=AW7Kg7CkiEm2mPY4MV5e>' +
                'Remove this unused import \'com.thecompany.winter.Seasons\'.</a>' +
                '<br/>\n' +
                'featurebranch-orange:Respect/src/main/java/com/thecompany/theproj/respect/MiddleWool.java</li>\n' +
                '</ul>\n' +
                '</p>\n'))
    }

    @Test
    void testFetchIssuesWithDupes() {
        def mockUrlFetcher = new MockFor(URLFetcher)
        mockUrlFetcher.demand.getJsonFromURL { url, userpass ->
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=myproj&resolved=false&createdAfter=2021-03-08')
                return getTestResourceDataPage('sonardata-2021-03-08', 1)
        }
        mockUrlFetcher.demand.getJsonFromURL { url, userpass ->
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=featurebranch-carrot&resolved=false&createdAfter=2021-02-15')
                return getTestResourceDataPage('sonardata-featurebranch-carrot', 1)
        }

        def formatter = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm')
        def fakeNow = LocalDateTime.parse('2021-03-08 00:00', formatter).toInstant(ZoneOffset.systemDefault())
        scanSonar.timeProvider = new TimeProvider() {
            Instant now() { fakeNow }
        }
        def allCollectedIssues = []
        mockUrlFetcher.use {
            scanSonar.urlFetcher = new URLFetcher(null)
            scanSonar.authorRepository = fakeAuthorRepository
            scanSonar.issueRepository = [
                    save: { issue -> allCollectedIssues += issue },
                    findByComponentNameAndFileNameAndRuleAndStartLine: {
                        componentName, fileName, rule, startLine ->
                            allCollectedIssues.findAll {
                                it.componentName == componentName &&
                                        it.fileName == fileName &&
                                        it.rule == rule &&
                                        it.startLine == startLine
                            }
                    },
                    deleteAll: { -> allCollectedIssues.clear()}
            ] as IssueRepository
            List<Issue> issues = scanSonar.getAllIssuesCreatedAfter('2021-03-08', 'myproj')
            issues.each { scanSonar.issueRepository.save(it) }
            assertThat(issues, hasSize(24))
            assertThat(allCollectedIssues, hasSize(24))
            List<Issue> branch9196Issues = scanSonar.getProjectIssues('featurebranch-carrot')
            List<Issue> allBranchIssues = new ArrayList<>()
            allBranchIssues.addAll(branch9196Issues)
            scanSonar.replaceIssuesWithUpdates(issues, allBranchIssues)
            assertThat(branch9196Issues, hasSize(7))
            // forced one dupe in branch9196Issues
            assertThat(allCollectedIssues, hasSize(30))
        }
        mockUrlFetcher.expect.verify()
    }

    @Test
    void testGetOldUpcomingIssues() {
        def old1 = [id: 1L, key: 'asfw3sdf', component: 'featurebranch-diamond'] as OldUpcoming
        def old2 = [id: 2L, key: '2sfsdfsa2', component: 'featurebranch-emerald'] as OldUpcoming
        def fakeOldUpcomingRepository = [
                findAll: { -> [old1, old2] }
        ] as OldUpcomingRepository

        scanSonar.oldUpcomingRepository = fakeOldUpcomingRepository
        def oldUpcomingIssues = scanSonar.getOldUpcomingIssues()
        assertThat(oldUpcomingIssues, hasSize(2))
        assertThat(oldUpcomingIssues, contains(old1, old2))
    }

    @Test
    void testGetTeamStats() {
        def issues = TeamStatsTest.threeSimpleIssues()
        // now is morning 10/9, so defaultSince is 8 weeks ago which is 8/14
        scanSonar.timeProvider = [
                now: { -> Instant.parse('2020-11-15T15:15:30.00Z')}
        ] as TimeProvider
        scanSonar.issueRepository = [
                findByCreationDateGreaterThanEqual: { creationDate -> issues },
                //deleteAll: { -> },
                deleteByProjectName: { projectName -> },
                deleteBranchIssues: { -> },
                save: { issue -> }
        ] as IssueRepository
        scanSonar.allTeams = ['giants', 'dolphins', 'packers', 'vikings', Team.OTHER]
        scanSonar.urlFetcher = [
                getJsonFromURL: { url, userpass -> '{"total":0,"ps":100,"paging":{"pageSize":100,"total":0},"issues":[]}' }
        ] as URLFetcher
        def statsList = scanSonar.getTeamStats('ALL')
        assertThat(statsList, hasSize(4 * 5)) // 4 dates, 5 teams
    }

    @Test
    void testGetAuthorFromDbWithSecondaryEmail() {
        def authors = [ahamilton, tjefferson]
        scanSonar.authorRepository = [
                save: { author -> authors += author },
                findByName: { name -> authors.find { it.name == name }},
                findByPrimaryEmail: { email -> authors.find { it.primaryEmail == email } },
                findBySecondariesContaining: { email -> authors.find { it.secondaries.contains(email) }}
        ] as AuthorRepository
        def anAuthor = scanSonar.getAuthorFromDb('ahamilton@mycompany.com')
        assertThat(anAuthor.name, is('Alexander Hamilton'))
        def aMailMapAuthor = scanSonar.getAuthorFromDb('jmonroe@me.com', false)
        assertThat(aMailMapAuthor.name, is('James Monroe'))
        def afterSavingAuthor = scanSonar.getAuthorFromDb('jmonroe@me.com', true)
        assertThat(aMailMapAuthor, is(afterSavingAuthor))
    }

    @Test
    void testGetTeamFromEmail() {
        def authors = [ahamilton, tjefferson]
        scanSonar.authorRepository = [
                //save: { author -> authors += author },
                findByName: { name -> authors.find { it.name == name }},
                findByPrimaryEmail: { email -> authors.find { it.primaryEmail == email } },
                findBySecondariesContaining: { email -> authors.find { it.secondaries.contains(email) }}
        ] as AuthorRepository
        assertThat(scanSonar.getTeamFromEmail('ahamilton@mycompany.com'), is('giants'))
        assertThat(scanSonar.getTeamFromEmail('jmonroe@me.com'), is(Team.OTHER))
        assertThat(scanSonar.getTeamFromEmail('bonjovi@yahoo.com'), is(Team.OTHER))
    }

    @Test
    void testGetResolved() {
        def fakeUrlFetcher = [getJsonFromURL: { url, userpass ->
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=myproj&resolved=true')
                return getTestResourceDataPage('resolved-2021-03-19', 1)
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=myproj&resolved=true&p=2')
                return getTestResourceDataPage('resolved-2021-03-19', 2)
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=myproj&resolved=true&p=3')
                return getTestResourceDataPage('resolved-2021-03-19', 3)
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=myproj&resolved=true&p=4')
                return getTestResourceDataPage('resolved-2021-03-19', 4)
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=myproj&resolved=true&p=5')
                return getTestResourceDataPage('resolved-2021-03-19', 5)
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=myproj&resolved=true&p=6')
                return getTestResourceDataPage('resolved-2021-03-19', 6)
        }] as URLFetcher

        List<Issue> issues = []
        scanSonar.timeProvider = [now: { -> Instant.parse('2021-03-19T23:22:30.00Z')}] as TimeProvider
        scanSonar.urlFetcher = fakeUrlFetcher
        scanSonar.authorRepository = fakeAuthorRepository
        scanSonar.issueRepository = [
                save: { issue -> },
                //deleteAll: { -> },
                deleteByProjectName: { projectName -> },
                deleteBranchIssues: { -> },
                findByCreationDateGreaterThanEqual: { creationDate -> issues.findAll { it.creationDate >= creationDate }}
        ] as IssueRepository
        issues = scanSonar.getIssuesResolvedAfter('2021-03-08', '2021-03-18', 'myproj')
        assertThat(issues, hasSize(77))
    }

    @Test
    void testUpdateBranchIssues() {
        def fakeUrlFetcher = [getJsonFromURL: { url, userpass ->
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=featurebranch-carrot&resolved=false&createdAfter=2020-01-09')
                return getTestResourceDataPage('sonardata-featurebranch-carrot', 1)
            if (url == 'http://mycompany.com/api/projects/search')
                return getTestResourceDataPage('one-project-search', 1)
        }] as URLFetcher
        def formatter = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm')
        def fakeNow = LocalDateTime.parse('2020-01-30 00:00', formatter).toInstant(ZoneOffset.systemDefault())
        scanSonar.timeProvider = new TimeProvider() {
            Instant now() { fakeNow }
        }
        scanSonar.urlFetcher = fakeUrlFetcher
        scanSonar.authorRepository = fakeAuthorRepository
        scanSonar.issueRepository = [
                save: {  ->  },
        ] as IssueRepository
        List<Issue> issues = scanSonar.updateBranchIssues()
        assertThat(issues, hasSize(7))
    }

    @Test
    void testUpdateMasterIssues() {
        // 2021-01-11 is 8 weeks before 2021-03-08
        def fakeUrlFetcher = [getJsonFromURL: { url, userpass ->
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=myproj&resolved=false&createdAfter=2021-01-11')
                return getTestResourceDataPage('sonardata-2021-03-08', 1)
        }] as URLFetcher
        def formatter = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm')
        def fakeNow = LocalDateTime.parse('2021-03-08 00:00', formatter).toInstant(ZoneOffset.systemDefault())
        scanSonar.timeProvider = new TimeProvider() {
            Instant now() { fakeNow }
        }
        scanSonar.urlFetcher = fakeUrlFetcher
        scanSonar.authorRepository = fakeAuthorRepository
        scanSonar.issueRepository = [
                save: {  ->  },
        ] as IssueRepository
        List<Issue> issues = scanSonar.updateMasterIssues()
        assertThat(issues, hasSize(24))
    }

    @Test
    void testUpdateMasterIssuesWithMultipleProjects() {
        // 2021-01-11 is 8 weeks before 2021-03-08
        def fakeUrlFetcher = [getJsonFromURL: { url, userpass ->
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=myproj-module1&resolved=false&createdAfter=2021-01-11')
                return getTestResourceDataPage('sonardata-module1', 1)
            if (url == 'http://mycompany.com/api/issues/search?componentKeys=myproj-module2&resolved=false&createdAfter=2021-01-11')
                return getTestResourceDataPage('sonardata-module2', 1)
        }] as URLFetcher
        def formatter = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm')
        def fakeNow = LocalDateTime.parse('2021-03-08 00:00', formatter).toInstant(ZoneOffset.systemDefault())
        scanSonar.project.main = 'myproj-module1,myproj-module2'
        scanSonar.timeProvider = new TimeProvider() {
            Instant now() { fakeNow }
        }
        scanSonar.urlFetcher = fakeUrlFetcher
        scanSonar.authorRepository = fakeAuthorRepository
        scanSonar.issueRepository = [
                save: {  ->  },
        ] as IssueRepository
        List<Issue> issues = scanSonar.updateMasterIssues()
        assertThat(issues, hasSize(9))
    }

}
