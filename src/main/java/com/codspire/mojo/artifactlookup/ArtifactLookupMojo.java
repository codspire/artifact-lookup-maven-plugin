package com.codspire.mojo.artifactlookup;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * 
 *
 * @author Rakesh Nagar
 * @since 1.0
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which touches a timestamp file. mvn clean install;mvn
 * com.codspire.plugin:artifact-lookup-maven-plugin:lookup
 * -DartifactLocation=c:\\temp\\dependency
 */
// http://central.sonatype.org/pages/consumers.html
// blogpost
// linkedin blog
// license
// logging debug/info
// unit/integration tests
// code quality and ci
// http://central.sonatype.org/pages/consumers.html
// TODO: plug-in help
// why plug-in? Native maven support, used effective setting to search the, yes
// we can create a simple script but i wanted something that is maven native,
// platform independent (bat, sh)
// artifacts, place nicely
// md5 feature
// if you are moving from ant to maven, chances are that you are using some form
// of local artifacts
// even if you are using maven but manually installing the artifacts to .m2, you
// may have the same problem
// http://choosealicense.com/
// publish to maven central or nexus oss site
// site documentaton similar to maven dependency plugin
// https://oss.sonatype.org/content/groups/public/
// https://www.javacodegeeks.com/2015/04/there-is-a-mojo-in-my-dojo-how-to-write-a-maven-plugin.html
// bucket of jars to nice clean dependencies (for external and internal
// libraries)
//copyright header
//javadocs
//plug-in docs
//plug-in help
//fix all TODO and FIXME
@Mojo(requiresProject = false, name = "lookup", defaultPhase = LifecyclePhase.NONE)
public class ArtifactLookupMojo extends AbstractMojo {

	// @Component
	// private Settings settings;

	@Parameter(readonly = true, required = true, defaultValue = "${project.remoteArtifactRepositories}")
	protected List<ArtifactRepository> remoteArtifactRepositories;

	// @Parameter(readonly = true, required = true, defaultValue =
	// "${localRepository}")
	// protected ArtifactRepository localRepository;

	@Parameter(readonly = true, required = true, property = "artifactLocation", defaultValue = "/c/temp/dependency")
	protected File artifactLocation;

	@Parameter(readonly = true, required = false, property = "repositoryUrl")
	protected String repositoryUrl;

	/**
	 * Location of the output files.
	 */
	@Parameter(defaultValue = ".", property = "outputDir", required = true)
	protected File outputDirectory;

	public void execute() throws MojoExecutionException {
		try {
			lookupArtifacts();
		} catch (Exception e) {
			getLog().error("Error executing the plugin", e);
		}
	}

	private void lookupArtifacts() throws Exception {
		Log log = getLog();
		validateRemoteArtifactRepositories();
		validateArtifactLocation();

		List<String> remoteArtifactRepositoriesURL = getRemoteArtifactRepositoriesURL(remoteArtifactRepositories);

		log.info(artifactLocation.getAbsolutePath() + "is file = " + artifactLocation.isFile());

		if (log.isDebugEnabled()) {
			log.debug("Remote Artifact Repositories");
			log.debug(remoteArtifactRepositories.toString());
		}

		LookupForDependency lookupForDependency = new LookupForDependency(artifactLocation, remoteArtifactRepositoriesURL, outputDirectory, getLog());
		lookupForDependency.process();
	}

	protected void validateArtifactLocation() {
		if (!artifactLocation.exists()) {
			throw new ContextedRuntimeException("ERROR: artifactLocation property is invalid. Please provide -DartifactLocation=<file or folder path>");
		}
	}

	protected void validateRemoteArtifactRepositories() {
		if (StringUtils.isBlank(repositoryUrl) && CollectionUtils.isEmpty(remoteArtifactRepositories)) {
			throw new ContextedRuntimeException("ERROR: No remote repository found, please check your settings.xml file");
		}
	}

	protected List<String> getRemoteArtifactRepositoriesURL(List<ArtifactRepository> remoteArtifactRepositories) {

		List<String> remoteArtifactRepositoriesURLList = null;
		// prefer repositoryUrl, it will superceed settings.xml

		if (StringUtils.isNotBlank(repositoryUrl)) {
			getLog().info("Using repository: " + repositoryUrl);
			remoteArtifactRepositoriesURLList = Arrays.asList(repositoryUrl.split(","));
		} else {

			remoteArtifactRepositoriesURLList = new ArrayList<String>(remoteArtifactRepositories.size());

			for (ArtifactRepository artifactRepository : remoteArtifactRepositories) {
				remoteArtifactRepositoriesURLList.add(artifactRepository.getUrl());
			}
		}
		return remoteArtifactRepositoriesURLList;
	}
}
