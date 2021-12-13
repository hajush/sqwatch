package com.sshift.sqwatch

class MailMap {

    private Map NAME_MAP
    private final static String AT_DOMAIN = "@mycompany.com"

    MailMap(InputStream mailMapStream) {
        initNameMap(mailMapStream)
    }

    private void parseMailMap(InputStream mailMapStream) {
        mailMapStream.eachLine {line -> addEmailsToNameMap(line)}
    }

    private void addEmailsToNameMap(String line) {
        line = line.trim()
        def matcher = line =~ /([^<]*)<([^>]+)>([^<]*<([^>]+)>)?/
        if (matcher.size() > 0 && matcher[0] && matcher[0].size() > 0 && matcher[0][1]) {
            String firstEmail = matcher[0][2]
            String secondEmail = matcher[0][4]
            String fullName = (matcher[0][1]).trim()
            NAME_MAP[firstEmail.trim().toLowerCase()] = fullName
            if (secondEmail) {
                NAME_MAP[secondEmail.trim().toLowerCase()] = fullName
            }
        }
    }

    String getNameFromEmail(String email) {
        email = email ? email.toLowerCase() : ""
        String answer = NAME_MAP[email]
        if (answer) {
            return answer
        } else if (email.endsWith(AT_DOMAIN)) {
            String author = email.substring(0, email.length() - AT_DOMAIN.length())
            return author.split(/\./).collect { it.capitalize() } .join(" ")
        }
        return email
    }

    int size() {
        return NAME_MAP.size()
    }

    Set<Map.Entry> updateAuthorsFromMap(AuthorRepository authorRepository) {
        NAME_MAP.entrySet().each { entry ->
            Author author = authorRepository.findByName(entry.value)
            if (!author) {
                author = new Author(entry.value, entry.key, Team.OTHER)
            } else {
                author.secondaryEmails += entry.key
            }
            authorRepository.save(author)
        }
    }

    void initNameMap(InputStream mailMapStream) {
        NAME_MAP = [:]
        parseMailMap(mailMapStream ? mailMapStream : InputStream.nullInputStream())
    }

}
