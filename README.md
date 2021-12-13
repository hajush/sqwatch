# SQWatch

## About

SQWatch is a Dashboard App that can be turned into an Information Radiator for watching a target
SonarQube instance. It can also be used through a REST API to send custom emails for new issues
to teams.

## Environment Variables

Some settings are required for the SpringBoot and JavaScript transpilation for the front end ReactJS.

The jar file must be built with the SQWATCH_SONAR_URL set or issue links in the webapp will 
go to localhost:9000.

These environment variables keep authentication tokens secure
as well as making it possible to point SQWatch at different SonarQube servers,
and different sets of scans. Something like the following would ideally be in your
.bash_profile during the development and test process. All these environment variables
should also be set on the server in a secured way. The \<passwords\> and \<authentication tokens\>
in brackets need to be replaced with the corresponding values. The SonarQube auth token is used
in the user:pass format, but the token is used in the user field, and the password left blank. So 
trailing colon is mandatory.

The SQWATCH_SONAR_BRANCH_PREFIX maybe be either a prefix or "upcoming_is_x", which means
that the flow of code does not come from feature branches so we need to count on the daily scans for
new issues. If the 'x' is replaced with a positive integer, new upcoming issues will be scanned back x days.

```text/x-sh
export SQWATCH_DB_URL=<db url, e.g. jdbc:postgresql://localhost:5432/sqwatch>
export SQWATCH_DB_USER=<db user account name here>
export SQWATCH_DB_PASS=<db user password here>
export SQWATCH_SONAR_URL=http://yoursonarqube.whatever:9000
export SQWATCH_SONAR_AUTH=<sonartoken here, don't forget colon after>:
export SQWATCH_SONAR_MASTER=<SonarQube project name for master branch scans (can be comma delimited list)>
export SQWATCH_SONAR_BRANCH_PREFIX=<prefix for SonarQube project names for feature branch scans | upcoming_is_today>
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

