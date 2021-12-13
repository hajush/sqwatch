import React from 'react';

export default function AuthorIssueCountTable(props) {
    let authorRows = [];
    props.issuesPerAuthor.forEach((issues, author) => authorRows.push ((
        <tr key={author}>
            <td><a href={issues[0].link}>{author}</a></td>
            <td>{issues.length}</td>
        </tr>
    )));

    return (
        <div>
            <table className="table table-sm">
                <tbody>
                    <tr>
                        <th>Author</th>
                        <th>Issue Count</th>
                    </tr>
                    {authorRows}
                </tbody>
            </table>
        </div>
    )
}
