package com.sshift.sqwatch

import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is

class ProjectTest {

    @Test
    void testProjectInit() {
        Project project = new Project()
        project.main = 'myproj'
        project.branchPrefix = 'featurebranch-'
        assertThat(project.getMain(), is(['myproj']))
        assertThat(project.getBranchPrefix(), is('featurebranch-'))
    }

    @Test
    void testProjectMultiMaster() {
        Project project = new Project()
        project.main = 'one,two,three'
        assertThat(project.main, is(['one','two','three']))
    }

    @Test
    void testOneWeekNoUpcoming() {
        Project project = new Project()
        project.branchPrefix = 'upcoming_is_7'
        assertThat(project.isNoBranches(), is(true))
        assertThat(project.getUpcomingPeriod(), is('7'))
        project.branchPrefix = 'Upcoming_IS_9'
        assertThat(project.isNoBranches(), is(true))
        assertThat(project.getUpcomingPeriod(), is('9'))
    }

    @Test
    void testOneWeekWithUpcoming() {
        Project project = new Project()
        project.branchPrefix = 'stuff-'
        assertThat(project.isNoBranches(), is(false))
        assertThat(project.getUpcomingPeriod(), is('UPCOMING'))
    }

    @Test
    void testEmptyBranchPrefix() {
        Project project = new Project()
        project.branchPrefix = ''
        assertThat(project.isNoBranches(), is(false))
        assertThat(project.getUpcomingPeriod(), is('UPCOMING'))
    }
}
