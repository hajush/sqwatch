import React from 'react';
import AuthorIssues from "./AuthorIssues";
import IssueList from "./IssueList";

export default function IssueView(props) {
    return (
        <div className="m-3">
            <AuthorIssues since={props.since} kindOfIssues={props.kindOfIssues} before={props.before}
                          issues={props.issues} loaded={props.loaded}/>
            <IssueList since={props.since} issues={props.issues}
                       loaded={props.loaded} loading={props.loading}/>
        </div>
    )
}
