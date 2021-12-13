package com.sshift.sqwatch

import org.junit.Test
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

class TeamTest {

    @Test
    void testTeamName() {
        Team team = new Team('cowboys')
        assertThat(team.name, is('cowboys'))
    }

    @Test
    void testTeamViewing() {
        Team team = new Team('vikings')
        assertThat(team.viewing, is(false))
        team.viewing = true
        assertThat(team.viewing, is(true))
    }
}
