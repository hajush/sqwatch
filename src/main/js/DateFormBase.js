import React from 'react';
import IssueView from './IssueView';

export default class DateFormBase extends React.Component {

    constructor(props) {
        super(props);
        this.state = {since: '', before: '', issues: [], issuesLoaded: false, issuesLoading: false};
        this.handleChangeSince = this.handleChangeSince.bind(this);
        this.handleChangeBefore = this.handleChangeBefore.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleChangeSince(event) {
        this.setState({since: event.target.value});
    }

    handleChangeBefore(event) {
        this.setState({before: event.target.value});
    }

    handleSubmit(event) {
        this.setState({issues: [], issuesLoaded: false, issuesLoading: true});
        fetch(this.restApi + this.state.since + '/' + (this.state.before || 'UPCOMING'))
            .then(res => res.json())
            .then(data => this.setState({ issues: data, issuesLoaded: true, issuesLoading: false }))
            .catch(console.log);
        event.preventDefault();
    }

    render() {
        return (
            <div className="container m-3">
                <form className="form-group" onSubmit={this.handleSubmit}>
                    <div className="input-group mb-3">
                        <div className="input-group-prepend">
                            <button className="btn btn-primary" type="button" id="button-addon1"
                                    onClick={this.handleSubmit}>{this.buttonText}</button>
                        </div>
                        <input type="text" className="form-control" placeholder="YYYY-MM-DD <Since>"
                               value={this.state.since} onChange={this.handleChangeSince} id="dateField1" />
                        <input type="text" className="form-control" placeholder="YYYY-MM-DD <Until>"
                               value={this.state.before} onChange={this.handleChangeBefore} id="dateField2" />
                    </div>
                </form>

                <IssueView since={this.state.since} before={this.state.before} kindOfIssues={this.kindOfIssues}
                           issues={this.state.issues}
                           loaded={this.state.issuesLoaded} loading={this.state.issuesLoading} />
            </div>
        );
    }
}
