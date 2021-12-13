import React from 'react';

export default function Metrics(props) {
    if (!props.loaded) {
        return <div/>
    } else return (
        <div>
            <p>Current Bug Count {props.bugs}.</p>
            <p>Current Debt {props.debt} days.</p>
            <p>Current Coverage {props.coverage}%.</p>
        </div>
    )
}
