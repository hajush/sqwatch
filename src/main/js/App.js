import React from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter as Router, Switch, Route, NavLink } from "react-router-dom";
import Home from './Home';
import DateFormOpen from './DateFormOpen';
import DateFormResolved from './DateFormResolved';
import Teams from './Teams';
import TeamChoice from './TeamChoice';
import TeamIssues from './TeamIssues';
import TeamStore from './TeamStore';
import { Provider } from 'mobx-react';

const teamStore = new TeamStore();
teamStore.initFromDb();

class App extends React.Component {
	render() {
		return (
			<Provider teamStore={teamStore}>
				<Router>
					<div>
						<nav className="navbar navbar-expand-lg navbar-light">
							<a className="navbar-brand" href="#">
								<img src="/img/sqwatch-icon.png"
									 className="d-inline-block align-top" alt="SQWatch Logo"/>
								SQWatch <small>Watching SonarQube Issues with Team Focus</small>
							</a>
							<button className="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav"
									aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
								<span className="navbar-toggler-icon"></span>
							</button>
							<div className="collapse navbar-collapse" id="navbarNav">
								<ul className="navbar-nav">
									<NavLink className="nav-item" exact={true} activeClassName="active" className="nav-link" to="/">
										Home
									</NavLink>
									<NavLink className="nav-item" activeClassName="active" className="nav-link" to="/since">
										Open Issues
									</NavLink>
									<NavLink className="nav-item" activeClassName="active" className="nav-link" to="/resolved">
										Resolved Issues
									</NavLink>
									<NavLink className="nav-item" activeClassName="active" className="nav-link" to="/teams">
										Team View
									</NavLink>
									<NavLink className="nav-item" activeClassName="active" className="nav-link" to="/choose-teams">
										Choose Teams
									</NavLink>
								</ul>
							</div>
						</nav>

						<Switch>
							<Route path="/choose-teams">
								<TeamChoice />
							</Route>
							<Route path="/teams">
								<Teams />
							</Route>
							<Route path="/since">
								<DateFormOpen />
							</Route>
							<Route path="/resolved">
								<DateFormResolved />
							</Route>
							<Route path="/browse/:team/:since/:type" component={TeamIssues}/>
							<Route path="/">
								<Home />
							</Route>
						</Switch>
					</div>
				</Router>
			</Provider>
		);
	}
}

ReactDOM.render(
	<App />,
	document.getElementById('react')
);
