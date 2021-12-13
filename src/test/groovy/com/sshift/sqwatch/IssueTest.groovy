package com.sshift.sqwatch

import groovy.json.JsonSlurper
import org.junit.Test
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class IssueTest {
    private static final double DAY_DELTA = 0.0001
    private static final Author tjefferson = new Author("Thomas Jefferson", "tjefferson@mycompany.com", "other")
    private static final Author johnny = new Author("Johnny Appleseed", "Johnny.Appleseed@mycompany.com", "dolphins")
    private static final Author davey = new Author("Davy Crockett", "davy.crockett@mycompany.com", "packers")
    private static final Author franklin = new Author("Franklin Roosevelt", "franklin.roosevelt@mycompany.com", "giants")
    private static final Author sadams = new Author("Samuel Adams", "sadams@mycompany.com", "other")
    private static final Author bfranklin = new Author("Benjamin Franklin", "benjamin.franklin@mycompany.com", "other")
    private static final Author apeet = new Author("Amanda Peet", "apeet@othercompany.com", "other", ["apeet"])

    @Test
    void what() {
        Issue actualLast = new Issue([
                "key": "3d93kHD3Kd83d8e9dk4O",
                "message": "\"beHereNowStatus's\" @Transactional requirement is incompatible with the one for this method.",
                "component": "myproj:Shaman/src/main/java/com/thecompany/theproj/Horse.java",
                "author": "sadams@mycompany.com",
                "severity": "INFO",
                "type": "BUG",
                "rule": "squid:S2229",
                "shortComponent": "Shaman-Horse.java"
        ], sadams)
        Issue shouldBeLast = new Issue([
                "key": "54dlL0d9e0L3df3D98u2",
                "message": "Complete the task associated to this TODO comment.",
                "component": "myproj:ComponentA/src/main/java/com/thecompany/Camel.java",
                "author": "davy.crockett@mycompany.com",
                "severity": "INFO",
                "type": "CODE_SMELL",
                "rule": "squid:S1135",
                "shortComponent": "ComponentA-Camel.java"
        ], davey)
        assertThat("bad rank", Issue.rankType(IssueType.BUG), is(1))
        assertThat("not right type", actualLast.type, is(IssueType.BUG))
        assertThat("ranktype problem!", Issue.rankType(actualLast.type), is(1))
        assertThat("concrete issue rank type attempt failed", actualLast.compareType(shouldBeLast), is(-3))
        assertThat("compare issue failed", actualLast <=> shouldBeLast, is(-1))
    }

    @Test
    void testComponentNameExtraction() {
        Issue issue = new Issue([
                "key": "54dlL0d9e0L3df3D98u2",
                "message": "Complete the task associated to this TODO comment.",
                "component": "myproj:ComponentA/src/main/java/com/thecompany/Camel.java",
                "author": "davy.crockett@mycompany.com",
                "severity": "INFO",
                "type": "CODE_SMELL",
                "rule": "squid:S1135",
                "shortComponent": "ComponentA-Camel.java"], davey)

        assertThat(issue.componentName, is("ComponentA"))
    }

    @Test
    void testProjectNameExtraction() {
        Issue issue = new Issue([
                "key": "54dlL0d9e0L3df3D98u2",
                "message": "Complete the task associated to this TODO comment.",
                "component": "myproj:ComponentA/src/main/java/com/thecompany/Camel.java",
                "author": "davy.crockett@mycompany.com",
                "severity": "INFO",
                "type": "CODE_SMELL",
                "rule": "squid:S1135",
                "shortComponent": "ComponentA-Camel.java"], davey)
        assertThat(issue.projectName, is("myproj"))
    }

    @Test
    void testProjectNameWithColonExtraction() {
        Issue issue = new Issue([
                "key": "54dlL0d9e0L3df3D98u2",
                "message": "Make sure that using a regular expression is safe here.",
                "component": "mycompany:myproject:hand/src/main/java/com/thecompany/Something.java",
                "type": "SECURITY_HOTSPOT",
                "rule": "java:S4784",
                "author": "benjamin.franklin@mycompany.com"], bfranklin)
        assertThat(issue.projectName, is('mycompany:myproject'))
    }

    @Test
    void testFileNameExtraction() {
        Issue issue = new Issue([
                "key": "s3dl28dl40-d2Dk38fLD",
                "message": "Complete the task associated to this TODO comment.",
                "component": "myproj:ComponentA/src/main/java/com/thecompany/Camel.java",
                "author": "davy.crockett@mycompany.com",
                "severity": "INFO",
                "type": "CODE_SMELL",
                "rule": "squid:S1135",
                "shortComponent": "ComponentA-Camel.java"], davey)

        assertThat(issue.fileName, is("Camel.java"))
    }

    @Test
    void testSortingProblem() {
        JsonSlurper jsonSlurper = new JsonSlurper()
        ClassLoader classLoader = getClass().getClassLoader()
        MailMap mailMap = new MailMap(classLoader.getResourceAsStream('.mailmap'))
        File file = new File(classLoader.getResource("issues-2020-10-08.json").getFile())
        def resultJson = jsonSlurper.parseText(file.text)
        def issues = resultJson.collect {
            new Issue(it, new Author(mailMap.getNameFromEmail(it.author), it.author,"other")) }
        def sortedIssues = issues.sort()
        assertThat(issues.size(), is(1454))
        assertThat(sortedIssues.first().key, is("V-VXV9nGCXkwP5KspSvm"))
        assertThat(sortedIssues.last().key, is("bQuBTJ2grj992I0Skh1g"))
    }

    @Test
    void canWeFixEmail() {
        Issue issue = new Issue([
                "key": "test",
                "message": "N/A.",
                "component": "h:dd",
                "author": "davy.crockett@mycompany.com",
                "severity": "INFO",
                "type": "CODE_SMELL",
                "rule": "squid:S1135",
                "shortComponent": "ComponentA-Camel.java.java"], davey)
        assertThat(issue.authorEmail, is("davy.crockett@mycompany.com"))
        assertThat(issue.author, is("Davy Crockett"))
    }

    @Test
    void canWeFixAnotherEmail() {
        Issue issue = new Issue([
                "key": "test",
                "message": "N/A.",
                "component": "h:dd",
                "author": "tjefferson@mycompany.com",
                "severity": "INFO",
                "type": "CODE_SMELL",
                "rule": "squid:S1135",
                "shortComponent": "ComponentA-Camel.java.java"], tjefferson)
        assertThat(issue.authorEmail, is("tjefferson@mycompany.com"))
        assertThat(issue.author, is("Thomas Jefferson"))
    }

    @Test
    void authorsBelongToTeams() {
        Issue issue = new Issue([
                "key": "test",
                "message": "Yo",
                "component": "projectname:TestComponent/package/TestFile.java",
                "author": "franklin.roOsevelt@mycompany.com",
                "severity": "INFO",
                "type": "CODE_SMELL",
                "rule": "squid:S1135",
                "debt": "30min",
                "shortComponent": "TestComponent-TestFile.java"], franklin)
        assertThat(issue.team, is('giants'))
    }

    @Test
    void anotherTeam() {
        Issue issue = new Issue([
                "key"           : "test",
                "message"       : "Yo",
                "component"     : "projectname:TestComponent/package/TestFile.java",
                "author"        : "Johnny.Appleseed@mycompany.com",
                "severity"      : "INFO",
                "type"          : "CODE_SMELL",
                "rule"          : "squid:S1135",
                "debt"          : "30min",
                "shortComponent": "TestComponent-TestFile.java"], johnny)

        assertThat(issue.team, is('dolphins'))
    }

    @Test
    void testLink() {
        Issue issue = new Issue([
                "key": "test",
                "message": "N/A.",
                "component": "projectname:TestComponent/package/TestFile.java",
                "author": "tjefferson@mycompany.com",
                "severity": "INFO",
                "type": "CODE_SMELL",
                "rule": "squid:S1135",
                "shortComponent": "TestComponent-TestFile.java"], tjefferson)

        assertThat(issue.link, is("#Thomas Jefferson"))
    }

    @Test
    void issuesWithTheSameKey_compareIsZero() {
        Issue issue1 = new Issue([
                "key": "test",
                "message": "N/A.",
                "component": "projectname:TestComponent/package/TestFile.java",
                "author": "tjefferson@mycompany.com",
                "severity": "INFO",
                "type": "CODE_SMELL",
                "rule": "squid:S1135",
                "shortComponent": "TestComponent-TestFile.java"], tjefferson)
        Issue issue2 = new Issue([
                "key": "test",
                "message": "N/A.",
                "component": "projectname:TestComponent/package/TestFile.java",
                "author": "tjefferson@mycompany.com",
                "severity": "INFO",
                "type": "CODE_SMELL",
                "rule": "squid:S1135",
                "shortComponent": "TestComponent-TestFile.java.java"], tjefferson)

        assertThat(issue1.compareTo(issue2), equalTo(0))
    }

    @Test
    void issueDebtMins() {
        Issue issue = new Issue([
                "key": "test",
                "message": "N/A.",
                "component": "h:dd",
                "author": "tjefferson@mycompany.com",
                "severity": "INFO",
                "type": "CODE_SMELL",
                "rule": "squid:S1135",
                "debt": "10min",
                "shortComponent": "TestComponent-TestFile.java.java"], tjefferson)
        assertThat(issue.debtInDays, closeTo((double)10.0 / 480, DAY_DELTA))
    }

    @Test
    void issueDebtHours() {
        Issue issue = new Issue([
                "key": "test",
                "message": "N/A.",
                "component": "projectname:TestComponent/package/TestFile.java",
                "author": "tjefferson@mycompany.com",
                "severity": "INFO",
                "type": "CODE_SMELL",
                "rule": "squid:S1135",
                "debt": "4h",
                "shortComponent": "TestComponent-TestFile.java.java"], tjefferson)
        assertThat(issue.debtInDays, closeTo((double)4 / 8, DAY_DELTA))
    }

    @Test
    void issueDebtHoursMins() {
        Issue issue = new Issue([
                "key": "test",
                "message": "N/A.",
                "component": "projectname:TestComponent/package/TestFile.java",
                "author": "tjefferson@mycompany.com",
                "severity": "INFO",
                "type": "CODE_SMELL",
                "rule": "squid:S1135",
                "debt": "3h15min",
                "shortComponent": "TestComponent-TestFile.java"], tjefferson)
        assertThat(issue.debtInDays, closeTo((double)(3 + 15.0/60) / 8, DAY_DELTA))
    }

    @Test
    void issueDebtDaysHours() {
        Issue issue = new Issue([
                "key": "test",
                "message": "N/A.",
                "component": "projectname:TestComponent/package/TestFile.java",
                "author": "Davy Crockett",
                "severity": "CRITICAL",
                "type": "CODE_SMELL",
                "rule": "squid:S1135",
                "debt": "4d2h",
                "shortComponent": "TestComponent-TestFile.java"], davey)
        assertThat(issue.debtInDays, closeTo((double)(4 + 2.0/8) , DAY_DELTA))
    }

    @Test
    void issueBadType() {
        Issue issue = new Issue([
                "key": "test",
                "component": "projectname:TestComponent/package/TestFile.java"
        ], davey)
        assertThat(issue.type, nullValue())
        assertThat(Issue.rankType(issue.type), is(0))
    }

    @Test
    void issueMissingDebt() {
        Issue issue = new Issue([
                "key": "test",
                "component": "projectname:TestComponent/package/TestFile.java"
        ], davey)
        assertThat(issue.debtInDays, is((double)0))
    }

    @Test
    void issueEmptyDebt() {
        Issue issue = new Issue([
                "key": "test",
                "component": "projectname:TestComponent/package/TestFile.java",
                "debt": ""
        ], davey)
        assertThat(issue.debtInDays, is((double)0))
    }

    @Test(expected = IllegalArgumentException.class)
    void issueBrokenDebtCode() {
        Issue issue = new Issue([
                "key": "test",
                "component": "projectname:TestComponent/package/TestFile.java",
                "debt": "4pigs"
        ], null)
        issue.debtInDays
    }

    @Test(expected = IllegalArgumentException.class)
    void issueGoodHoursBrokenMinutes() {
        Issue issue = new Issue([
                'key': 'test',
                'component': 'projectname:TestComponent/package/TestFile.java',
                'debt': '3h4mins'
        ], null)
        issue.debtInDays
    }

    @Test
    void issueNoAuthorLink() {
        Issue issue = new Issue(['key': 'test'], null)
        assertThat(issue.link, is('#'))
    }

    @Test
    void compareIssuesSameButKey() {
        def issueStub = [
                'key'           : 'test1',
                'message'       : 'N/A.',
                'component'     : 'projectname:TestComponent/package/TestFile.java',
                'author'        : 'tjefferson@mycompany.com',
                'severity'      : 'INFO',
                'type'          : 'CODE_SMELL',
                'rule'          : 'squid:S1135',
                'debt'          : '10min',
                'shortComponent': 'TestComponent-TestFile.java']
        Issue issue = new Issue(issueStub, tjefferson)
        Issue issueCopy = new Issue(issueStub, tjefferson)
        issueStub.key = "test2"
        Issue issue2 = new Issue(issueStub, tjefferson)
        assertThat(issue <=> issueCopy, is(0))
        assertThat(issue2, greaterThan(issue))
        assertThat(issue, lessThan(issue2))
        assertTrue(issue.isEquivalent(issue2))
    }

    @Test
    void checkEquivalence() {
        def issueStub = [
                'key': '2233dd',
                'type': 'CODE_SMELL',
                'debt': '5min',
                'component': 'myproj:Peril/com/danger/TestFile.java',
                'fileName': 'TestFile.java',
                'startLine': 17,
                'endLine': 17,
                'startOffset': 12,
                'endOffset': 19]
        def issue1 = issueStub as Issue
        assertFalse(issue1.isEquivalent(null))
        def issue2 = issueStub.clone() as Issue
        assert(issue1.isEquivalent(issue2))
        issue2.key = 'dd3322'
        assert(issue1.isEquivalent(issue2))
        issue2.endOffset = 18
        assertFalse(issue1.isEquivalent(issue2))
        issue2.endOffset = issue1.endOffset
        issue2.startOffset = 9
        assertFalse(issue1.isEquivalent(issue2))
        issue2.startOffset = issue1.startOffset
        issue2.endLine = 18
        assertFalse(issue1.isEquivalent(issue2))
        issue2.endLine = issue1.endLine
        issue2.startLine = 14
        assertFalse(issue1.isEquivalent(issue2))
        issue2.startLine = issue1.startLine
        issue2.fileName = 'StartWindow.java'
        assertFalse(issue1.isEquivalent(issue2))
        issue2.fileName = issue1.fileName
        issue2.componentName = 'myproj:SRA/com/sra/TestFile.java'
        assertFalse(issue1.isEquivalent(issue2))
        issue2.componentName = issue1.componentName
        issue2.type = 'BUG'
        assertFalse(issue1.isEquivalent(issue2))
        issue2.type = issue1.type
        issue2.debt = '1h'
        assertFalse(issue1.isEquivalent(issue2))
    }

    @Test
    void testUpcomingDateForFeatureBranch() {
        def issueStub = [
                'key'         : '2344tttt3',
                'type'        : 'BUG',
                'component'   : 'featurebranch-diamond:' +
                        'Hand/src/test/java/com/thecompany/theproj/hand/MountainRiverTest.java',
                'creationDate': '2021-02-02T12:27:40-0700'
        ]
        Issue issueFeature = new Issue(issueStub, franklin)
        Project myProject = new Project()
        myProject.main = 'myproj,myproj2'
        myProject.branchPrefix = 'featurebranch-'
        issueFeature.setUpcoming()
        assertThat(issueFeature.creationDate, is('UPCOMING'))
        issueStub.component = 'myproj:' +
                'Hand/src/test/java/com/thecompany/theproj/hand/MountainRiverTest.java'
        Issue issueMaster = new Issue(issueStub, franklin)
        assertThat(issueMaster.creationDate, is('2021-02-02T12:27:40-0700'))
    }

    @Test
    void canWeAccessId() {
        Issue issue = new Issue([
                'key': 'test',
                'component': 'projectname:TestComponent/package/TestFile.java',
                'debt': ''
        ], davey)
        assertThat(issue.getId(), nullValue(Long))
    }

    @Test
    void issueWithP4StyleAuthor() {
        Issue issue = new Issue([
                'key'           : '1a2b3c',
                'message'       : 'N/A',
                'component'     : 'projectname:TestComponent/package/TestFile.java',
                'author'        : 'apeet',
                'severity'      : 'INFO',
                'type'          : 'CODE_SMELL',
                'rule'          : 'squid:S1135',
                'debt'          : '10min',
                'shortComponent': 'TestComponent-TestFile.java'
        ], apeet)
        assertThat(issue.authorEmail, is(apeet.primaryEmail))
    }
}
