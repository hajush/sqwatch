import { extendObservable } from "mobx";

export default class TeamStore {

    constructor() {
        extendObservable(this, {
            allTeams: [],
            teams: [],
            authors: [],
            sonarBaseURL: String
        });
        this.addTeam = this.addTeam.bind(this);
        this.removeTeam = this.removeTeam.bind(this);
        this.hasTeam = this.hasTeam.bind(this);
        this.getAuthors = this.getAuthors.bind(this);
        this.getAllTeams = this.getAllTeams.bind(this);
        this.getTeam = this.getTeam.bind(this);
        this.setTeam = this.setTeam.bind(this);
        this.getSonarBaseURL = this.getSonarBaseURL.bind(this);
    }

    initFromDb() {
        fetch('/api/authors')
            .then(res => res.json())
            .then(data => this.authors = data._embedded.authors)
            .catch(console.log);
        fetch('/api/teams')
            .then(res => res.json())
            .then(data => this.allTeams = data._embedded.teams)
            .catch(console.log);
        fetch('/sonarqubeurl')
            .then(res => res.text())
            .then(data => this.sonarBaseURL = data)
            .catch(console.log);
    }

    hasTeam(team) {
        return this.teams.indexOf(team) > -1;
    }

    addTeam(team) {
        if (!this.hasTeam(team)) {
            this.teams.push(team);
        }
    }

    removeTeam(team) {
        let index = this.teams.indexOf(team);
        if (index > -1) {
            this.teams.splice(index, 1);
        }
    }

    getAuthors() {
        return this.authors.map(author => author.name);
    }

    getTeam(author) {
        let authorData = this.authors.find(it => it.name == author);
        return authorData ? authorData.team : "other"
    }

    setTeam(author, team) {
        let authorData = this.authors.find(it => it.name == author);
        authorData.team = team;
        fetch('/api/authors/' + authorData.id, {
            method: 'PATCH',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                team: authorData.team
            })
        })
    }

    getAllTeams() {
        return this.allTeams.map(team => team.name);
    }

    getSonarBaseURL() {
        return this.sonarBaseURL;
    }

}

