import React from "react";

export const CATEGORY = {
    "BUG": {img: "/img/bug.svg", title: "Bug"},
    "SECURITY_HOTSPOT": {img: "/img/hot.svg", title: "Security Hotspot"},
    "VULNERABILITY": {img: "/img/sec.svg", title: "Vulnerability"},
    "CODE_SMELL": {img: "/img/smell.svg", title: "Code Smell"}
};

export function imageForCategory(category) {
    let cat = CATEGORY[category];
    return cat ? (<img src={cat.img} height="16" title={cat.title} alt={cat.title}/>)
        : (<p>{category}</p>);
}
