package com.sshift.sqwatch

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Team {

    static String OTHER = 'other'
    private @Id String name
    private boolean viewing

    Team() {}

    Team(String name) {
        this.name = name;
    }

    String getName() {
        return name
    }

    boolean getViewing() {
        return viewing
    }

    void setViewing(boolean viewing) {
        this.viewing = viewing
    }

}
