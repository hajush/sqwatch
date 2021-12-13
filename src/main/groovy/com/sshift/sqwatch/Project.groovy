package com.sshift.sqwatch

class Project {

    protected static final String UPCOMING = 'UPCOMING'
    private static final String UPCOMING_IS_X = "upcoming_is_";
    private List<String> main
    private String branchPrefix = UPCOMING_IS_X

    void setMain(String main) {
        this.main = main.split(',')
    }

    List<String> getMain() {
        main
    }

    void setBranchPrefix(String prefix) {
        this.branchPrefix = prefix
    }

    String getBranchPrefix() {
        this.branchPrefix
    }

    boolean isNoBranches() {
        return this.branchPrefix.startsWithIgnoreCase(UPCOMING_IS_X)
    }

    String getUpcomingPeriod() {
        return isNoBranches() ? this.branchPrefix.substring(UPCOMING_IS_X.size()) : UPCOMING
    }
}
