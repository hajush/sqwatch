import React from 'react';
import { inject, observer } from 'mobx-react';
import Authors from './Authors';

class TeamChoice extends React.Component {

    handleChange(event) {
        if (event.target.checked) {
            this.that.props.teamStore.addTeam(this.team);
        } else {
            this.that.props.teamStore.removeTeam(this.team);
        }
    }

    render() {
        let choices = this.props.teamStore.getAllTeams().map( team => (
                <div key={team}>
                    <label>
                        <input type="checkbox" value={team} onChange={this.handleChange.bind({team: team, that: this})}
                               checked={this.props.teamStore.hasTeam(team)}
                        />
                        {team}
                    </label>
                </div>
            )
        );
        return (
                <div className="container-fluid m-3">
                    {choices}
                    <Authors/>
                </div>
        )
    }
}

export default inject('teamStore')(observer(TeamChoice));
