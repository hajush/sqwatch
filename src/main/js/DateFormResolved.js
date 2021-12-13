import DateFormBase from './DateFormBase';

export default class DateFormResolved extends DateFormBase {

    constructor(props) {
        super(props);
        this.restApi = '/fixed/';
        this.kindOfIssues = 'resolved issues';
        this.buttonText = 'Resolved Issues Between';
    }
}
