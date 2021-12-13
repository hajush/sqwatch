import React from 'react';
import IssueList from './IssueList';
import Metrics from './Metrics';
import AuthorIssues from './AuthorIssues'

export default class Home extends React.Component {

	constructor(props) {
		super(props);
		this.state = {
			issues: [],
			coverage: "",
			debt: 0,
			bugs: 0,
			metricsLoaded: false,
			issuesLoaded: false,
			issuesLoading: false
		};
	}

	componentDidMount() {
		fetch('/issues/today/UPCOMING')
			.then(res => res.json())
			.then(data => this.setState({ issues: data, issuesLoaded: true, issuesLoading: false }))
			.catch(console.log);
		fetch('/metrics')
			.then(res => res.json())
			.then(data => this.setState({
				coverage: data.coverage, debt: data.debt, bugs: data.bugs, metricsLoaded: true}))
			.catch(console.log);
	}

	render() {
		return (
			<div className="container m-3">
				<Metrics coverage={this.state.coverage} debt={this.state.debt}
						 bugs={this.state.bugs} loaded={this.state.metricsLoaded}/>
				<IssueList since="today" issues={this.state.issues}
						   loaded={this.state.issuesLoaded}
						   loading={this.state.issuesLoading}
				/>
				<AuthorIssues since="today" before="Upcoming" kindOfIssues="new issues"
							  issues={this.state.issues} loaded={this.state.issuesLoaded}/>
			</div>
		)
	}
}
