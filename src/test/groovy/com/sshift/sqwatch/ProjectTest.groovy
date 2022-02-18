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
    void testOneWeekWithUpcoming() {
        Project project = new Project()
        project.branchPrefix = 'stuff-'
        assertThat(project.isNoBranches(), is(false))
        assertThat(project.getUpcomingPeriod(), is('7'))
    }

    @Test
    void testEmptyBranchPrefix() {
        Project project = new Project()
        project.branchPrefix = ''
        assertThat(project.isNoBranches(), is(true))
        assertThat(project.getUpcomingPeriod(), is('7'))
    }
}
