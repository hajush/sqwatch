package com.sshift.sqwatch

import org.springframework.data.repository.CrudRepository

interface AuthorRepository extends CrudRepository<Author, Long> {

    Author findByName(String name)

    Author findByPrimaryEmail(String primaryEmail)

    Author findBySecondariesContaining(String secondaryEmail)

}
