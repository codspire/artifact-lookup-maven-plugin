# Artifact Lookup Maven Plugin

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/versions-maven-plugin.svg?label=License)](http://www.apache.org/licenses/)
[![Build Status](https://travis-ci.org/codspire/artifact-lookup-maven-plugin.svg?branch=master)](https://travis-ci.org/codspire/artifact-lookup-maven-plugin)
[![Dependency Status](https://www.versioneye.com/user/projects/5701a4b2fcd19a0039f1562e/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5701a4b2fcd19a0039f1562e)
[![Coverage Status](https://coveralls.io/repos/github/codspire/artifact-lookup-maven-plugin/badge.svg?branch=master)](https://coveralls.io/github/codspire/artifact-lookup-maven-plugin?branch=master)

![Artifact Lookup Maven Plugin Info](https://raw.githubusercontent.com/codspire/artifact-lookup-maven-plugin/master/src/main/resources/artifact-lookup-maven-plugin-info.png)

This "Artifact Lookup Maven Plugin" acts as a Maven command line utility that can search local jar/zip files in remote Maven repositories. The search result is returned in the form of Maven `<dependency>` coordinates (groupId, artifactId & version) that can be used in the pom file.

This could be quite beneficial for non Maven Java projects that are migrating to Maven and are faced with a daunting task of resolving the project dependencies from remote Maven repositories. This could be quite an error prone and time consuming activity if there are large number of jars scattered across many directories/sub-directories. Its becomes even harder if the local jar files are not following the version based naming convention (e.g. `httpclient.jar` as against `httpclient-4.5.2.jar`).

## Installation
```sh
$ git clone https://github.com/codspire/artifact-lookup-maven-plugin.git
```
```sh
$ mvn install
```

## Usage
```sh
$ mvn com.codspire.plugin:artifact-lookup-maven-plugin:lookup
```

This plugin follows a minimalistic approach. Available options are:
* `-no parameters-`: resolve all jars that exist in the current directory and sub-directories from default remote repositories.
* `artifactLocation`: if its a file path; resolve specified file from default remote repositories. If its a directory; resolve all jars that exist in the specified directory and sub-directories from default remote repositories.
* `repositoryUrl`: resolve all qualified jars (based on other parameters) from the specified remote repositories.

Default Maven remote repositories are determined based on user's effective Maven `settings.xml` file. If there are more than one remote repository, the jars are sequentially searched  against all repositories until the match is found.

### Examples
 
## Tested against
* https://oss.sonatype.org/content/groups/public/
* https://repo.maven.apache.org/maven2/
* https://repo1.maven.org/maven2/
* https://repository.jboss.org/nexus/content/groups/public/
* Privately hosted Sonatype Nexus repository


<script src="https://gist.github.com/codspire/8831da96879535db07e91b545d2cc3cf.js"></script>
