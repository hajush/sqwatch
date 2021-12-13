import React from 'react';
import IssueView from "./IssueView";

export default class TeamIssues extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            issues: [],
            issuesLoaded: false,
            issuesLoading: false
        };
    }

    componentDidMount() {
        this.setState({issues: [], issuesLoaded: false, issuesLoading: true});
        fetch('/teamissues/' + this.props.match.params.team + '/'
            + this.props.match.params.since + '/' + this.props.match.params.type)
            .then(res => res.json())
            .then(data => this.setState({ issues: data, issuesLoaded: true, issuesLoading: false }))
            .catch(console.log);
    }

    render() {
        return (
            <div>
                <IssueView since={this.props.match.params.since} before="Upcoming" kindOfIssues="new issues"
                               issues={this.state.issues}
                               loaded={this.state.issuesLoaded} loading={this.state.issuesLoading}/>
            </div>
        )
    }
}