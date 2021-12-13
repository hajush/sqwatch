package com.sshift.sqwatch

import groovy.mock.interceptor.MockFor
import groovy.mock.interceptor.StubFor
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Test
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

class IssueControllerTest {
    IssueController controller = new IssueController()
    def mockScanSonar
    def mockIssueRepository = new MockFor(IssueRepository)

    @Before
    void setUp() {
        mockScanSonar = new StubFor(ScanSonar)
        mockScanSonar.demand.getDefaultSinceDate{ -> "2020-11-07" }
    }

    @Test
    void testIssues_GoodResult() {
        mockScanSonar.demand.getIssuesSince{ since, before -> [] }
        mockScanSonar.use {
            mockIssueRepository.use {
                controller.scanSonar = new ScanSonar()
                controller.issueRepository = [
                    findByCreationDateGreaterThanEqual: {String creationDate -> [new Issue()]}
                ] as IssueRepository
                controller.issues('blah', 'blah')
            }
        }
    }

    @Test
    void testTeamStats() {
        mockScanSonar.demand.getTeamStats { team -> new TeamStats() }
        mockScanSonar.use {
            controller.scanSonar = new ScanSonar()
            controller.teamStats('blah')
        }
    }

    @Test
    void testMetrics() {
        mockScanSonar.demand.getMetrics { -> new Metrics() }
        mockScanSonar.use {
            controller.scanSonar = new ScanSonar()
            controller.metrics()
        }
    }

    @Test
    void testInitDbNada() {
        testInitDbWithInitAuthors([])
    }

    @Test
    void testInitDbAlreadyStuff() {
        testInitDbWithInitAuthors([''])
    }

    private void testInitDbWithInitAuthors(ArrayList<String> authorList) {
        def teamList = []
        def mockAuthorRepository = new MockFor(AuthorRepository)
        def mockTeamRepository = new MockFor(TeamRepository)
        MailMap emptyMailMap = new MailMap(null)

        mockScanSonar.demand.setMailMap { mailMap -> }
        mockScanSonar.demand.getMailMap { -> emptyMailMap }
        mockScanSonar.demand.updateTeamsFromDB { -> [] }
        mockScanSonar.demand.logSomething { message -> }

        mockScanSonar.use {
            mockAuthorRepository.use {
                mockTeamRepository.use {
                    controller.scanSonar = new ScanSonar()
                    controller.scanSonar.mailMap = emptyMailMap
                    controller.authorRepository = [
                            findByName: {
                                String name -> authorList.contains(name) ? new Author(name, '', 'other', []) : null
                            },
                            save      : { author -> authorList += author.name }
                    ] as AuthorRepository
                    controller.teamRepository = [
                            findByName: { String name -> new Team(name) },
                            save      : { Team team -> teamList += team.name }
                    ] as TeamRepository
                    controller.initDb()
                    MatcherAssert.assertThat(authorList.size(), is(1))
                    assertThat(authorList[0], is(''))
                    MatcherAssert.assertThat(teamList.size(), is(1))
                    assertThat(teamList[0], is('other'))
                }
            }
        }
    }

}
