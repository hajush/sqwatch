import DateFormBase from './DateFormBase';

export default class DateFormOpen extends DateFormBase {

    constructor(props) {
        super(props);
        this.restApi = '/issues/';
        this.kindOfIssues = 'new issues';
        this.buttonText = 'Open Issues Between';
    }
}
