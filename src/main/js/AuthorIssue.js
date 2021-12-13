import React from 'react';
import { imageForCategory } from './Category';
import { imageForSeverity } from './Severity';

export default function AuthorIssue(props) {
    let issue = props.issue;
    let SQWATCH_URL = SONAR_BASE_URL + "/project/issues?id=";
    let issueLink = `${SQWATCH_URL}${issue.projectName}&issues=${issue.key}&open=${issue.key}`;

    return (
        <li>
            {imageForSeverity(issue.severity)}
            {imageForCategory(issue.type)}
            <a href={issueLink}>{issue.message}</a><br/>
            {issue.component}
        </li>
    );
}
