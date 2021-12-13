import React from 'react';
import AuthorIssueCountTable from './AuthorIssueCountTable';
import AuthorIssueListings from './AuthorIssueListings';

export default function AuthorIssues(props) {
    if (!props.loaded || props.issues.length == 0) {
        return (<p></p>)
    }

    let issuesPerAuthor = new Map();
    props.issues.forEach(issue => {
        if (!issuesPerAuthor.has(issue.author)) {
            issuesPerAuthor.set(issue.author, [issue]);
        } else {
            issuesPerAuthor.get(issue.author).push(issue);
        }
    });
    issuesPerAuthor = new Map([...issuesPerAuthor].sort((a, b) => b[1].length - a[1].length));

    return (
        <div>
            <p>{props.issues.length} {props.kindOfIssues} since {props.since} and before {props.before} with issue count per author.</p>
            <AuthorIssueCountTable issuesPerAuthor={issuesPerAuthor}/>
            <AuthorIssueListings issuesPerAuthor={issuesPerAuthor}/>
        </div>
    )
}
