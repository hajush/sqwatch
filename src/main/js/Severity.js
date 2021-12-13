import React from "react";

export const SEVERITY = {
    "BLOCKER": {img: "/img/block.svg", title: "Blocker"},
    "CRITICAL": {img: "/img/critic.svg", title: "Critical"},
    "MAJOR": {img: "/img/major.svg", title: "Major"},
    "MINOR": {img: "/img/minor.svg", title: "Minor"},
    "INFO": {img: "/img/info.svg", title: "Info"}
};

export function imageForSeverity(severity) {
    let sev = SEVERITY[severity];
    return sev ? (<img src={sev.img} title={sev.title} alt={sev.title}/>)
        : (<p>{severity}</p>);
}
