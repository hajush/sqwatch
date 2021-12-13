package com.sshift.sqwatch

import groovy.util.logging.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import java.nio.file.Path
import java.nio.file.Paths

@RestController
@Log
class IssueController {

    @Autowired ScanSonar scanSonar
    @Autowired AuthorRepository authorRepository
    @Autowired IssueRepository issueRepository
    @Autowired TeamRepository teamRepository

    @GetMapping(value = "/issues/{since}/{before}")
    @ResponseBody
    List<Issue> issues(@PathVariable String since, @PathVariable String before) {
        return scanSonar.getIssuesSince(since, before)
    }

    @GetMapping(value = "/fixed/{since}/{before}")
    @ResponseBody
    List<Issue> fixed(@PathVariable String since, @PathVariable String before) {
        return scanSonar.getIssuesResolvedAfter(since, before)
    }

    @GetMapping(value = "/team-from-email/{email}")
    @ResponseBody
    String teamFromEmail(@PathVariable String email) {
        return scanSonar.getTeamFromEmail(email)
    }

    @GetMapping(value = "/branch-issues/**")
    @ResponseBody
    List<Issue> branchIssues(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString()
        String project = requestURL.split("/branch-issues/")[1]
        return scanSonar.getProjectIssues(project)
    }

    @GetMapping(value = "/teamstats/{team}")
    @ResponseBody
    Object teamStats(@PathVariable String team) {
        return scanSonar.getTeamStats(team)
    }

    @GetMapping(value = '/teamissues/{team}/{since}/{type}')
    @ResponseBody
    Object teamIssues(@PathVariable String team, @PathVariable String since, @PathVariable String type) {
        return scanSonar.getTeamIssues(team, since, type)
    }

    @GetMapping(value = '/newupcoming/{teams}')
    @ResponseBody
    String upcomingIssues(@PathVariable String teams) {
        return scanSonar.getNewUpcomingIssues(teams)
    }

    @GetMapping(value = '/oldupcoming')
    @ResponseBody
    List<OldUpcoming> oldUpcoming() {
        return scanSonar.getOldUpcomingIssues()
    }

    @GetMapping(value = '/metrics')
    @ResponseBody
    Metrics metrics() {
        return scanSonar.getMetrics()
    }

    @GetMapping(value = '/projects')
    @ResponseBody
    List<String> projects() {
        return scanSonar.getProjects()
    }

    @GetMapping(value = '/initdb')
    @ResponseBody
    Object initDb() {
        if (!authorRepository.findByName(""))
            authorRepository.save(Author.NO_AUTHOR)
        def namesMap = scanSonar.mailMap.updateAuthorsFromMap(authorRepository)
        teamRepository.save(new Team(Team.OTHER))
        def teamsList = scanSonar.updateTeamsFromDB()
        scanSonar.logSomething("Loaded ${namesMap.size()} author entries into db from .mailmap " +
                "and ${teamsList.size()} team names from the db")
        return [namesMap, teamsList]
    }

    @GetMapping(value = '/refresh-issues')
    @ResponseBody
    Object refreshIssues() {
        refreshIssuesSchedule()
        return "Fetching issues from SonarQube initiated."
    }

    @Scheduled(cron = '0 50 * * * *')
    void refreshIssuesSchedule() {
        scanSonar.logRefreshStart()
        Runnable r = new Runnable() {
            void run() {
                List<Issue> masterIssues = scanSonar.updateMasterIssues()
                scanSonar.logSomething("Loaded ${masterIssues.size()} issues from master projects.")
                List<Issue> branchIssues = scanSonar.updateBranchIssues()
                scanSonar.logSomething("Loaded ${branchIssues.size()} issues from branch projects.")
                branchIssues.forEach({issue -> issue.setUpcoming()})
                scanSonar.replaceIssuesWithUpdates(masterIssues, branchIssues)
                scanSonar.logRefreshComplete()
            }
        }
        new Thread(r).start()
    }
}
