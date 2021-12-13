package com.sshift.sqwatch

import org.springframework.data.repository.CrudRepository

interface IssueRepository extends CrudRepository<Issue, Long> {

    List<Issue> findByCreationDateGreaterThanEqual(String creationDate)

    List<Issue> findByComponentNameAndFileNameAndRuleAndStartLine(
            String componentName, String fileName, String rule, Integer startLine)

}
