import React from 'react';
import { inject, observer } from 'mobx-react';
import TeamStat from './TeamStat'

const SEC_PER_MIN = 60;
const MILLI_PER_SEC = 1000;
const FIFTEEN_MINS = MILLI_PER_SEC * SEC_PER_MIN * 15;

class Teams extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            teamStats: [],
            loaded: false
        };
    }

    componentDidMount() {
        this.getData();
        this.intervalID = setInterval(this.getData.bind(this), FIFTEEN_MINS);
    }

    componentWillUnmount() {
        clearInterval(this.intervalID);
    }

    getData() {
        fetch('/teamstats/ALL')
            .then(res => res.json())
            .then(data => this.setState({ teamStats: data, loaded: true }))
            .catch(console.log);
    }

    render() {
        let TEAMS = this.props.teamStore.getAllTeams();
        let teams = this.state.teamStats.reduce((all, stat) => all.add(stat.team), new Set());
        teams = Array.from(teams).sort((team1, team2) => TEAMS.indexOf(team1) - TEAMS.indexOf(team2))
        let statsByTeam = new Map();
        teams.forEach(team => {
            statsByTeam[team] = this.state.teamStats.filter(stats => stats.team == team);
        });

        let allTeamStats = teams.filter(this.props.teamStore.hasTeam).map(team => (
            <TeamStat key={team + "-table"} stats={statsByTeam[team] ? statsByTeam[team] : []}/>
        ));

        return (
            <div className="container-fluid col-sm-12 mt-3 mr-4">
                <div className="row">
                    {allTeamStats}
                </div>
            </div>
        )
    }
}

export default inject('teamStore')(observer(Teams));
