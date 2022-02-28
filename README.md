# SQWatch

## About

SQWatch is a web application that monitors (watches) a SonarQube instance to help monitor
code quality issues for a set of teams. It allows monitoring upcoming issues from branches 
not yet merged off of the main (MASTER) branch. It has views of the data that can be queried
and can be used as a dashboard Information Radiator to compare code quality between teams.
SQWatch can also be used through a REST API to send custom emails for new issue notifications
to teams.

## Important REST API endpoints

Some of the API is automatically generated and can be inspected at /api. Much of the
API is intended for internal use, but other endpoints are needed to access SQWatch
features. Nearly all of the returned data is via JSON except for /newupcoming/{teams}

### /api/authors/{id}
The author list is initialized via /initdb from the .mailmap configuration, and new 
authors are automatically when encountered during scans. The author list can also be referenced
and adjusted through the REST API, as there is no web GUI for changing the author list 
other than setting the author's team.
- GET - lists all authors in the author database if no ID, or only the one author with an ID.

### /api/teams/
The list of teams is necessary for newupcoming notifications. This list needs to be set 
directly in the database. The list can be viewed through a GET call to the REST API.

### /initdb
Adds author data from the conf/.mailmap configuration file to the author database.

### /newupcoming/{teams}
A text (not JSON) message is returned with HTML links that can be added to an email message.
The initial list will be a comma delimited email list of the authors using the primary_email.
The {teams} needs to be a comma delimited list of teams. SQWATCH_THRESHHOLD_* limits are used
to focus only on higher priority issues to help teams begin to improve their coding quality
standards gradually.

## Environment Variables

These environment variables keep authentication tokens secure
as well as making it possible to point SQWatch at different SonarQube servers,
and different sets of scans. Something like the following would ideally be in your
.bash_profile during the development and test process. All these environment variables
should also be set on the server in a secured way. The \<passwords\> and \<authentication tokens\>
in brackets need to be replaced with the corresponding values. The SonarQube auth token is used
in the user:pass format, but the token is used in the user field, and the password left blank. So 
trailing colon is mandatory.

The SQWATCH_SONAR_BRANCH_PREFIX is used to match all feature or future branch prefixes 
to count as 'upcoming' since they are not yet in the main branch(es).

The SQWATCH_THRESHHOLD_* environment variables are optional and allow notification messages
to be limited only to the indicated priority and above. For example, if
SQWATCH_THRESHHOLD_CODESMELL is set to CRITICAL then only BLOCKER and CRITICAL priority
codesmell notifications will be generated with the REST API call /newupcoming/{teams}.

```text/x-sh
export SQWATCH_DB_URL=<db url, e.g. jdbc:postgresql://localhost:5432/sqwatch>
export SQWATCH_DB_USER=<db user account name here>
export SQWATCH_DB_PASS=<db user password here>
export SQWATCH_SONAR_URL=http://yoursonarqube.whatever:9000
export SQWATCH_SONAR_AUTH=<sonartoken here, don't forget colon after>:
export SQWATCH_SONAR_MASTER=<SonarQube project name for master branch scans (can be comma delimited list)>
export SQWATCH_SONAR_BRANCH_PREFIX=<prefix for SonarQube project names for feature/future branch scans>
export SQWATCH_THRESHHOLD_BUG=<optional - if present BLOCKER|CRITICAL|MAJOR|MINOR>
export SQWATCH_THRESHHOLD_CODESMELL=<optional - if present BLOCKER|CRITICAL|MAJOR|MINOR>
export SQWATCH_THRESHHOLD_VULNERABILITY=<optional - if present BLOCKER|CRITICAL|MAJOR|MINOR>
```

## Building/Running

`git clone `

`cd sqwatch`

`./gradlew clean assembleFrontend bootJar`

There is now an executable JAR file at `./build/libs/sqwatch-${version}.jar`

To run at http://localhost:8080, run the following:

`./gradlew bootRun`

To build to deploy beyond the localhost, or on a different port, it will be necessary to
set the environment variable 

## Git MailMap

A GIT style .mailmap file can be created and placed in the application's 'conf/' directory to set up name to
email mappings. It can also be used to map perforce or other SCM commit ids that SonarQube pulls, and make them
correspond to emails for email notifications.

## Deploying

Deployment is not automated, but on a Red Hat Linux the process might look like so:
```text/x-sh
% scp build/libs/sqwatch-${VERSION}.jar ~:sqwatch
% ssh sqwatch
% sudo -u sqwatch /bin/bash
% cp /home/yourprodam/sqwatch-${VERSION}.jar ~
% exit
% sudo systemctl restart sqwatch
```

