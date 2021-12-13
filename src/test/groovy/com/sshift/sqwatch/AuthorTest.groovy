package com.sshift.sqwatch

import org.junit.Test
import static org.junit.Assert.assertNotEquals
import static org.junit.Assert.assertEquals
import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.Matchers.*

class AuthorTest {

    @Test
    void compareAuthorToNonAuthor() {
        Author author = new Author('Amy', 'amy@test.com','tiger')
        assertThat(author, notNullValue())
        assertThat(author, not('Amy'))
    }

    @Test
    void compareSameAuthor() {
        Author author1 = new Author('David', 'david@test.com','tiger')
        Author author2 = new Author('David', 'david@test.com','tiger')
        assertEquals(author1, author1)
        assertEquals(author1, author2)
    }

    @Test
    void compareDifferentAuthor() {
        Author author1 = new Author('David', 'david@test.com','tiger')
        Author author2 = new Author('Amy', 'amy@test.com','tiger')
        assertNotEquals(author1, author2)
    }

    @Test
    void compareSimilarAuthors() {
        // We really want author names to be unique in the database, but this is intended to be thorough
        Author author1 = new Author('David', 'david1@test.com', 'tiger')
        Author author2 = new Author('David', 'david1@test.com', 'tiger', ['bad@email.net'])
        Author author3 = new Author('David', 'david1@test.com', 'hoodlums')
        assertNotEquals(author1, author2)
        assertNotEquals(author1, author3)
    }

    @Test
    void testHash() {
        Author author = new Author('JD Salinger', 'jds@test.com', 'novelists')
        def hash = author.hashCode()
        assertThat(hash, notNullValue())
    }

    @Test
    void testSettingStuff() {
        Author author = new Author('J Appleseed', 'ja@test.com', 'legends')
        author.name = 'Johnny Appleseed'
        author.secondaryEmails = ['japple@gmail.com']
        author.team = 'american legends'
        author.primaryEmail = 'owner@appleseedindustries.com'
        assertThat(author.name, is('Johnny Appleseed'))
        assertThat(author.secondaryEmails, is(['japple@gmail.com']))
        assertThat(author.team, is('american legends'))
        assertThat(author.primaryEmail, is('owner@appleseedindustries.com'))
    }
    
    @Test
    void testSecondaryEmails() {
        def sec1 = 'ya@yes.com'
        def sec2 = 'jb@who.com'
        Author author = new Author('Jack Black', 'jb@no.com','fooey', [sec1, sec2])
        assertThat(author.secondaryEmails, is([sec1, sec2]))
        assertThat(author.secondaries, is(sec1 + ' ' + sec2))
        author.setSecondaryEmails([sec1, sec2, sec2, sec1])
        assertThat(author.secondaryEmails, contains(sec1, sec2))
        assertThat(author.secondaryEmails, hasSize(2))
    }

    @Test
    void testSecondaries() {
        def sec1 = 'ya@yes.com'
        def sec2 = 'jb@who.com'
        Author author = new Author('Jack Black', 'jb@no.com', 'fooey')
        assertThat(author.secondaryEmails, empty())
        author.setSecondaries(sec1 + ' ' + sec2 + ' ' + sec1)
        assertThat(author.secondaries, is(sec1 + ' ' + sec2))
        assertThat(author.secondaryEmails, containsInAnyOrder(sec2, sec1))
        assertThat(author.secondaryEmails, hasSize(2))
    }

    @Test
    void compareAuthorsDifferentIds() {
        Author author1 = new Author('John', 'john@test.com','tiger')
        Author author2 = new Author('John', 'john@test.com','tiger')
        author1.id = 11
        author2.id = 15
        assertNotEquals(author1, author2)
    }

    @Test
    void compareAuthorToNullOrRawString() {
        Author author = new Author('Jake','j@t.com','tiger')
        assertThat(author.equals(null), is(false))
        assertThat(author.equals('Jake'), is(false))
    }
}
