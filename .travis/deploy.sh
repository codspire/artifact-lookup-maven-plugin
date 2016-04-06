#!/bin/bash

# Thanks to https://github.com/DracoBlue/http-response-headers/blob/master/.travis/deploy.sh
# https://github.com/blackdoor/hate/tree/master/cd
cd `dirname $0`/.. 

if [ -z "$OSSRH_JIRA_USERNAME" ]
then
    echo "error: please set OSSRH_JIRA_USERNAME and OSSRH_JIRA_PASSWORD environment variable"
    exit 1
fi

if [ -z "$OSSRH_JIRA_PASSWORD" ]
then
    echo "error: please set OSSRH_JIRA_PASSWORD environment variable"
    exit 1
fi

#if [ ! -z "$TRAVIS_TAG" ]
#then
#    echo "on a tag -> set pom.xml <version> to $TRAVIS_TAG"
#    mvn --settings .travis/settings.xml org.codehaus.mojo:versions-maven-plugin:2.1:set -DnewVersion=$TRAVIS_TAG 1>/dev/null 2>/dev/null
#else
#    echo "not on a tag -> keep snapshot version in pom.xml"
#fi

mvn deploy -DskipTests=true -P sign,build-extras --settings .travis/settings.xml -B -U