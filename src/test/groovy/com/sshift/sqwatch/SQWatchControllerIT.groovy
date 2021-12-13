package com.sshift.sqwatch

import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit4.SpringRunner

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.startsWith
import static org.hamcrest.MatcherAssert.assertThat

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SQWatchControllerIT {
    @LocalServerPort
    private int port

    private URL base

    @Autowired
    private TestRestTemplate template

    @Before
    void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port + "/")
    }

    @Test
    @Ignore
    void getHello() {
        ResponseEntity<String> response = template.getForEntity(base.toString(), String.class)
        assertThat(response.getBody(), equalTo("Please give a date to query tossonar issues since /tossonar/YYYY-MM-DD"))
    }

    @Test
    @Ignore
    void testSonarSince() {
        SQWatchController hello = new SQWatchController()
        assertThat(hello.tossonar("2021-07-16"), startsWith("<p>Total Issues Since 2021-07-16:"))
    }
}
