package com.sshift.sqwatch

class TeamStatPoint {

    int issueCount
    int bugCount
    int vulnerabilityCount
    int codeSmellCount
    double debtInDays
    String date
    String team

    TeamStatPoint(String team, String date) {
        this.team = team
        this.date = date
    }

}
