import React from 'react';
import { imageForCategory } from './Category';
import { imageForSeverity } from './Severity';

export default function Issue(props) {
    let issue = props.issue;
    let SQWATCH_URL = SONAR_BASE_URL + "/project/issues?id=";
    let issueLink = `${SQWATCH_URL}${encodeURIComponent(issue.projectName)}&issues=${issue.key}&open=${issue.key}`;

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
