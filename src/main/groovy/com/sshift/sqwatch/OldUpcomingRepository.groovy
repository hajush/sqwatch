package com.sshift.sqwatch

import org.springframework.data.repository.CrudRepository

// This remembers upcoming issues we've retrieved and sent notices so we don't repeat ourselves in the notice emails
interface OldUpcomingRepository extends CrudRepository<OldUpcoming, Long> {

    OldUpcoming findByKey(String key)

}
