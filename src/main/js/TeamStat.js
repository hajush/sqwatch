import React from "react";
import { Link } from "react-router-dom";

export default function TeamStat(props) {
    const TEAM_ICON_HEIGHT = 96;
    const DATE_ICON_HEIGHT = 72;
    const STAT_ITEMS = ["issueCount", "bugCount", "vulnerabilityCount", "codeSmellCount", "debtInDays"];
    const STAT_ITEM_HEIGHT = 48
    const STAT_ITEM_VIEW = {
        issueCount: <b>TOTAL</b>,
        bugCount: <img height={STAT_ITEM_HEIGHT} src="/img/bug.svg"/>,
        vulnerabilityCount:  <img height={STAT_ITEM_HEIGHT} src="/img/sec.svg"/>,
        codeSmellCount:  <img height={STAT_ITEM_HEIGHT} src="/img/smell.svg"/>,
        debtInDays: <img height={STAT_ITEM_HEIGHT} src="/img/debt.png"/>
    };
    const METRIC_ITEM_TO_CATEGORY = {
        issueCount: 'ALL',
        bugCount: 'BUG',
        vulnerabilityCount: 'VULNERABILITY',
        codeSmellCount: 'CODE_SMELL',
        debtInDays: 'CODE_SMELL'
    };
    let teamName = props.stats[0] ? props.stats[0].team : "oh no";

    function tableHeaderKey(teamStat) {
        return teamName + "-header" + (teamStat ? "-date-" + teamStat.date : "");
    }

    function tableCellKey(metricItem, teamStat) {
        return teamName + "-row-" + metricItem + "-col-" + (teamStat ? teamStat.date : "date");
    }

    function dateView(date) {
        if (date == "UPCOMING") {
            return <img height={DATE_ICON_HEIGHT} src="/img/upcoming.png"
                        alt="Upcoming in feature branches" title="Upcoming in feature branches"/>;
        } else if(isToday(date)) {
            return <img height={DATE_ICON_HEIGHT} src="/img/today.png"
                        alt="Today (since 12:01am this morning)" title="Today (since 12:01am this morning)"/>;
        } else if (isYesterday(date)) {
            return <img height={DATE_ICON_HEIGHT} src="/img/yesterday.png"
                        alt="Found Yesterday" title="Found Yesterday"/>;
        } else if (isLastWeek(date)) {
            return <img height={DATE_ICON_HEIGHT} src="/img/lastweek.png"
                        alt="Found this past week before yesterday" title="Found this past week before yesterday"/>;
        } else {
            return date;
        }
    }

    function isToday(date) {
        return isDateEqualToDateString(new Date(), date);
    }

    function isYesterday(date) {
        let yesterdayDate = new Date();
        yesterdayDate.setDate((new Date()).getDate() - 1);
        return isDateEqualToDateString(yesterdayDate, date);
    }

    function isLastWeek(date) {
        let lastWeekDate = new Date();
        lastWeekDate.setDate((new Date()).getDate() - 7);
        return isDateEqualToDateString(lastWeekDate, date);
    }

    function isDateEqualToDateString(date, dateString) {
        return date.toISOString().slice(0, 10) == dateString;
    }

    function numberView(metricItem, teamStat) {
        let number = metricItem == "debtInDays" ? teamStat[metricItem].toFixed(2) : teamStat[metricItem];
        if (number == 0)
            return <div className="goodnews h1">✓</div>
        else return (
            <h1 className="badnews h1">
                <Link className="badnews" to={makeLink(metricItem, teamStat.date)}>{number}</Link>
            </h1>
        )
    }

    function makeLink(metricItem, date) {
        let category = METRIC_ITEM_TO_CATEGORY[metricItem];
        return `/browse/${teamName}/${date}/${category}`
    }

    function teamImage(team) {
        return team === "other" ? "img/other.png" : "images/" + team + ".png"
    }

    let header = props.stats.map(it => (
        <th className="bvr" key={tableHeaderKey(it)}>
            {dateView(it.date)}
        </th>
    ));

    let rows = STAT_ITEMS.map(item => {
        let itemValuesForDates = props.stats.map(teamStat => (
            <td className="bvr" key={tableCellKey(item, teamStat)}>
                {numberView(item, teamStat)}
            </td>
        ));
        return (
            <tr key={teamName + "-row-" + item}>
                <td className="bvr" key={tableCellKey(item)}>
                    {STAT_ITEM_VIEW[item]}
                </td>
                {itemValuesForDates}
            </tr>
        )
    });
    return (
        <div className="col-sm-6">
            <table className="m-1">
                <tbody>
                <tr key={"header-row-" + teamName}>
                    <th className="bvr" key={tableHeaderKey()}>
                        <img height={TEAM_ICON_HEIGHT} src={teamImage(teamName)}/>
                    </th>
                    {header}
                </tr>
                {rows}
                </tbody>
            </table>
        </div>
    )
}

