import React from 'react';
import AuthorIssue from './AuthorIssue';

export default function AuthorIssueList(props) {
    if (props.issues.length == 0) {
        return <p/>
    }
    const issues = props.issues.map(issue => <AuthorIssue key={issue.key} issue={issue}/>);
    return (
        <ul>
            {issues}
        </ul>
    )
}
