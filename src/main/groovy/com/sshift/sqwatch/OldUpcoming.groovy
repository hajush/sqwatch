package com.sshift.sqwatch

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

// We're remembering upcoming issues we've already sent email notices about, and we only really need the key.
@Entity
class OldUpcoming {

    @Id @GeneratedValue
    private Long id

    String key
    String component

    Long getId() {
        return id
    }

}
