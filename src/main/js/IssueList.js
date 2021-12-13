import React from 'react';
import Issue from './Issue';

export default function IssueList(props) {
    if (!props.loading && !props.loaded) {
        return <p/>
    } else if (props.loading) {
        return <p>Loading ...</p>
    } else if (props.loaded && props.issues.length == 0) {
        return <p>No new SonarQube issues today!</p>
    }
    const issues = props.issues.map(issue => <Issue key={issue.key} issue={issue}/>);
    return (
        <div>
            <p>{issues.length} new issues since {props.since}, sorted by severity.</p>
            <table className="table table-sm">
                <tbody>
                <tr>
                    <th><img src="/img/severity.png" alt="Severity" title="Severity" height="26"/></th>
                    <th><img src="/img/category.png" alt="Severity" title="Category" height="26"/></th>
                    <th>Message</th>
                    <th>Component</th>
                    <th>File</th>
                    <th>Author</th>
                </tr>
                {issues}
                </tbody>
            </table>
        </div>
    )
}
