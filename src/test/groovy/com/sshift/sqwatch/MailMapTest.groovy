package com.sshift.sqwatch

import org.junit.Before
import org.junit.Test
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is

class MailMapTest {
    private MailMap mailMap

    @Before
    void init() {
        ClassLoader classLoader = getClass().getClassLoader()
        mailMap = new MailMap(classLoader.getResourceAsStream(".mailmap"))
    }

    @Test
    void georgeW_fullName() {
        assertThat(mailMap.getNameFromEmail('george.washington@mycompany.com'), is('George Washington'))
    }

    @Test
    void georgeW_short() {
        assertThat(mailMap.getNameFromEmail('george@mycompany.com'), is('George Washington'))
    }

    @Test
    void copernicus() {
        assertThat(mailMap.getNameFromEmail('ncopernicus@mycompany.com'), is('Nicolaus Copernicus'))
    }

    @Test
    void newton() {
        assertThat(mailMap.getNameFromEmail('isaac.newton@mycompany.com'), is('Isaac Newton'))
    }

    @Test
    void companyEmail() {
        assertThat(mailMap.getNameFromEmail('harolD.shinsatO@mycompany.com'), is('Harold Shinsato'))
    }

    @Test
    void nameMap() {
        assertThat(mailMap.getNameFromEmail('tjefferson@mycompany.com'), is('Thomas Jefferson'))
    }

    @Test
    void testEmptyEmail() {
        assertThat(mailMap.getNameFromEmail(''), is(''))
    }

    @Test
    void testMailMapIntoAuthorRepository() {
        HashSet<Author> authorSet = new HashSet<>()
        AuthorRepository fakeAuthorRepository = [
                save: { author ->
                    authorSet.add(author)
                },
                findByName: { name ->
                    authorSet.find { author -> author.name == name }
                }
        ] as AuthorRepository
        mailMap.updateAuthorsFromMap(fakeAuthorRepository)
        assertThat(authorSet.size(), is(88))
    }

    @Test
    void testPerforceStyle() {
        ClassLoader classLoader = getClass().getClassLoader()
        MailMap p4mailMap = new MailMap(classLoader.getResourceAsStream('.p4mailmap'))
        assertThat(p4mailMap.getNameFromEmail('amanda@somecompany.com'), is('Amanda Peet'))
    }

    @Test
    void testInitMailMapNoStream() {
        MailMap emptyMailMap = new MailMap(null)
        assertThat(emptyMailMap.size(), is(0))
    }
}
