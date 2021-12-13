import React from 'react';
import BootstrapTable from 'react-bootstrap-table-next';
import { inject, observer } from 'mobx-react';

const OPTION_CHAR = '_';

class Authors extends React.Component {

    constructor(props) {
        super(props);
    }

    handleChange(event) {
        this.that.props.teamStore.setTeam(this.author, this.that.teamFromOption(event.target.value));
    }

    teamFromOption(option) {
        return option.slice(0, option.indexOf(OPTION_CHAR));
    }

    teamOptions(author) {
        let keyValue = team => team + OPTION_CHAR + author;
        return this.props.teamStore.getAllTeams().map(team => (
            <option key={keyValue(team)} value={keyValue(team)}>{team}</option>
        ))
    }

    currentTeam(author) {
        return this.props.teamStore.getTeam(author) + OPTION_CHAR + author;
    }

    createHandleChange(author, that) {
        return this.handleChange.bind({author: author, that: that});
    }

    render() {
        let columns = [
            {
                text: 'Full Name',
                dataField: 'name',
                hidden: true
            },
            {
                text: 'First Name',
                dataField: 'first',
                sort: true
            },
            {
                text: 'Last Name',
                dataField: 'last',
                sort: true
            },
            {
                text: 'Team',
                dataField: 'team',
                hidden: true
            },
            {
                text: 'Choose Team',
                dataField: 'choose',
                sort: true,
                sortValue: (cell, row) => row['team']
            }
        ];
        let authorTeamChoice = this.props.teamStore.getAuthors().map( author => {return {
            'name': author,
            'first': author.indexOf(' ') != -1 ? author.slice(0, author.indexOf(' ')) : author,
            'last': author.slice(author.indexOf(' ') + 1),
            'team': this.props.teamStore.getTeam(author),
            'choose':
                (
                <label>
                    <select value={this.currentTeam(author)} onChange={this.createHandleChange(author, this)}>
                        {this.teamOptions(author)}
                    </select>
                </label>
            )
        }});

        return (
            <BootstrapTable keyField='name'
                            columns={columns}
                            data={authorTeamChoice}
                            className="container-fluid" />
        )
    }
}

export default inject('teamStore')(observer(Authors));
