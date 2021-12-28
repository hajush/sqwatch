import React from 'react';
import { imageForCategory } from './Category';
import { imageForSeverity } from './Severity';
import {inject, observer} from "mobx-react";

class Issue extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        let issue = this.props.issue;
        let sonarBaseUrl = this.props.teamStore.getSonarBaseURL()
        let sonarProjectBaseUrl = `${sonarBaseUrl}/project/issues?id=${encodeURIComponent(issue.projectName)}`;
        let issueLink = `${sonarProjectBaseUrl}&issues=${issue.key}&open=${issue.key}`;

        return (
            <tr>
                <td>{imageForSeverity(issue.severity)}</td>
                <td>{imageForCategory(issue.type)}</td>
                <td><a href={issueLink}>{issue.message.slice(0, 50)}</a></td>
                <td>{issue.componentName}</td>
                <td>{issue.fileName}</td>
                <td>{issue.author}</td>
            </tr>
        );
    }
}
export default inject('teamStore')(observer(Issue));
