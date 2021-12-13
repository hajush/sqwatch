package com.sshift.sqwatch

import org.springframework.data.repository.CrudRepository

interface TeamRepository extends CrudRepository<Team, Long> {

    Team findByName(String name)

    List<Team> findAll();
}
