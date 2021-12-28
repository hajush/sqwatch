import React from 'react';
import { imageForCategory } from './Category';
import { imageForSeverity } from './Severity';
import { inject, observer } from 'mobx-react';

class AuthorIssue extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        let issue = this.props.issue;
        let sonarBaseUrl = this.props.teamStore.getSonarBaseURL()
        let sonarProjectBaseUrl = `${sonarBaseUrl}/project/issues?id=${encodeURIComponent(issue.projectName)}`;
        let issueLink = `${sonarProjectBaseUrl}&issues=${issue.key}&open=${issue.key}`;

        return (
            <li>
                {imageForSeverity(issue.severity)}
                {imageForCategory(issue.type)}
                <a href={issueLink}>{issue.message}</a><br/>
                {issue.component}
            </li>
        );
    }
}

export default inject('teamStore')(observer(AuthorIssue));
