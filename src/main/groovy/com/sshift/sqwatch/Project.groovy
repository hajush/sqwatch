package com.sshift.sqwatch

class Project {

    protected static final String UPCOMING = 'UPCOMING'
    private List<String> main
    private String branchPrefix = ''

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
        return this.branchPrefix.length() == 0
    }

    String getUpcomingPeriod() {
        return '7'
    }
}
