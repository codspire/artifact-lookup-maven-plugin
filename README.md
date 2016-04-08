# Artifact Lookup Maven Plugin

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/versions-maven-plugin.svg?label=License)](http://www.apache.org/licenses/) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.codspire.plugins/artifact-lookup-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.codspire.plugins/artifact-lookup-maven-plugin) [![Build Status](https://travis-ci.org/codspire/artifact-lookup-maven-plugin.svg?branch=master)](https://travis-ci.org/codspire/artifact-lookup-maven-plugin) [![Dependency Status](https://www.versioneye.com/user/projects/5701a4b2fcd19a0039f1562e/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5701a4b2fcd19a0039f1562e) [![Coverage Status](https://coveralls.io/repos/github/codspire/artifact-lookup-maven-plugin/badge.svg?branch=master)](https://coveralls.io/github/codspire/artifact-lookup-maven-plugin?branch=master) [![Codacy Badge](https://api.codacy.com/project/badge/grade/ed1b3ddf0664422d88d768a87f659e16)](https://www.codacy.com/app/codspire/artifact-lookup-maven-plugin)

This "Artifact Lookup Maven Plugin" acts as a Maven command line utility that can search local jar/zip files in remote Maven repositories. The search results are returned in the form of Maven `<dependency>` coordinates (groupId, artifactId & version) that can be used in the pom file.
![Artifact Lookup Maven Plugin Info](https://raw.githubusercontent.com/codspire/artifact-lookup-maven-plugin/master/src/main/resources/artifact-lookup-maven-plugin-info.png)

# Motivation
Ability to search a remote Maven repository by `jar or directory` could be quite beneficial, specially for legacy Java projects that are migrating to Maven. Such migration often involves the daunting task of locating the existing project dependencies in the remote Maven repositories which could take several days or weeks based on the size and complexity of the project. This activity becomes even harder and error prone if the jar files are scattered across many directories/sub-directories and their naming is not alignd to [version based naming convention](http://semver.org) (e.g. `httpclient.jar` as against `httpclient-4.5.2.jar`). 

## Usage
``` sh
$ mvn com.codspire.plugins:artifact-lookup-maven-plugin:lookup
```

### Usage Options
This plugin follows a minimalistic approach. Available options are:
* **`-no parameters-`**: resolve all jars that exist in the current directory and sub-directories from default remote repositories.
* **`artifactLocation`**: if its a file path; resolve specified file from default remote repositories. If its a directory; resolve all jars that exist in the specified directory and sub-directories from default remote repositories.
* **`repositoryUrl`**: resolve all qualified jars (based on other parameters) from the specified remote repositories. Supports csv format to specify multiple repositories.

## Build & Installation (if you don't have direct access to `Maven Central`)
```
$ git clone https://github.com/codspire/artifact-lookup-maven-plugin.git

$ cd artifact-lookup-maven-plugin

$ mvn install
```

## Key Points
* Arfifacts are searched based on `SHA1` checksum of the file.
* Default Maven remote repositories are determined based on user's effective Maven `settings.xml` file. 
* If there are more than one remote repository, the jars are sequentially searched against all repositories until the match is found.

## Usage Examples

### Example 1: Search all jars in a folder in default remote repository
cd to the folder that contains jar files
``` sh
$ ls
activation.jar  commons-io.jar  junit.jar
```
run the plugin

``` sh
$ mvn com.codspire.plugins:artifact-lookup-maven-plugin:lookup
```

``` xml
[INFO] Scanning for projects...
...
...
[INFO] ------------------------------------------------------------------------
[INFO] DEPENDENCIES
[INFO] ------------------------------------------------------------------------
[INFO]
<dependencies>
        <!-- Resolved from http://repo.maven.apache.org/maven2 -->
        <dependency>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
                <version>4.12</version>
        </dependency>
        <!-- Resolved from http://repo.maven.apache.org/maven2 -->
        <dependency>
                <groupId>javax.activation</groupId>
                <artifactId>activation</artifactId>
                <version>1.1</version>
        </dependency>
        <!-- Resolved from http://repo.maven.apache.org/maven2 -->
        <dependency>
                <groupId>commons-io</groupId>
                <artifactId>commons-io</artifactId>
                <version>2.4</version>
        </dependency>
</dependencies>

[INFO] ------------------------------------------------------------------------
...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
### Example 2: Search specific jar in default remote repository
``` sh
$ mvn com.codspire.plugins:artifact-lookup-maven-plugin:lookup \
-DartifactLocation=./activation.jar
```

``` xml
[INFO] Scanning for projects...
...
...
[INFO] ------------------------------------------------------------------------
[INFO] DEPENDENCIES
[INFO] ------------------------------------------------------------------------
[INFO]
<dependencies>
        <!-- Resolved from http://repo.maven.apache.org/maven2 -->
        <dependency>
                <groupId>javax.activation</groupId>
                <artifactId>activation</artifactId>
                <version>1.1</version>
        </dependency>
</dependencies>

[INFO] ------------------------------------------------------------------------
...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Example 3: Search specific jar in alternate remote repository (supersedes `settings.xml`)
``` sh
$ mvn com.codspire.plugins:artifact-lookup-maven-plugin:lookup \
 -DartifactLocation=./activation.jar \
 -DrepositoryUrl=https://oss.sonatype.org/content/groups/public/
```

``` xml
[INFO] Scanning for projects...
...
...
[INFO] ------------------------------------------------------------------------
[INFO] DEPENDENCIES
[INFO] ------------------------------------------------------------------------
[INFO]
<dependencies>
        <!-- Resolved from https://oss.sonatype.org/content/groups/public/ -->
        <dependency>
                <groupId>javax.activation</groupId>
                <artifactId>activation</artifactId>
                <version>1.1</version>
        </dependency>
</dependencies>

[INFO] ------------------------------------------------------------------------
...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
## Tested With
* https://oss.sonatype.org/content/groups/public/
* https://repo.maven.apache.org/maven2/
* https://repo1.maven.org/maven2/
* https://repository.jboss.org/nexus/content/groups/public/
* Privately hosted Sonatype Nexus repository

## Author
[Rakesh Nagar](https://github.com/codspire)

## Licence
This code is released under the Apache License Version 2.0. See [LICENSE](https://github.com/codspire/artifact-lookup-maven-plugin/blob/master/LICENSE)
