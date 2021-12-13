import React from 'react';
import AuthorIssueList from "./AuthorIssueList";

export default function AuthorIssueListings(props) {

    let authorPanels = [];
    props.issuesPerAuthor.forEach((issues, author) => authorPanels.push (

        <section key={author} id={author}>{author} {issues.length} issues
            <AuthorIssueList since="" issues={issues}/>
        </section>
    ));

    return (
        <div>
            {authorPanels}
        </div>
    )
}
