package com.codspire;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//http://central.sonatype.org/pages/consumers.html
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Settings;

import com.maven.plugin.LookupForDependency;

/**
 * Goal which touches a timestamp file.
 *
 */
// http://central.sonatype.org/pages/consumers.html
@Mojo(requiresProject = false, name = "touch", defaultPhase = LifecyclePhase.NONE)
public class MyMojo extends AbstractMojo {

	@Component
	private Settings settings;

	@Parameter(readonly = true, required = true, defaultValue = "${project.remoteArtifactRepositories}")
	protected List<ArtifactRepository> remoteArtifactRepositories;

	@Parameter(readonly = true, required = true, defaultValue = "${localRepository}")
	protected ArtifactRepository localRepository;

	@Parameter(readonly = true, required = true, property = "artifactLocation", defaultValue = "/c/temp/dependency")
	protected File artifactLocation;

	/**
	 * Location of the file.
	 */
	@Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;

	private void getSettings() throws Exception {

		validateRemoteArtifactRepositories();
		validateArtifactLocation();

		List<String> remoteArtifactRepositoriesURL = getRemoteArtifactRepositoriesURL(remoteArtifactRepositories);

		System.out.println("artifactLocation=" + artifactLocation.getAbsolutePath());
		System.out.println(artifactLocation.isFile());

		// DefaultMavenSettingsBuilder defaultMavenSettingsBuilder = new
		// DefaultMavenSettingsBuilder();
		// Settings settings = defaultMavenSettingsBuilder.buildSettings();
		// System.out.println(settings);
		System.out.println(remoteArtifactRepositories);

/*		for (ArtifactRepository artifactRepository : remoteArtifactRepositories) {
			System.out.println(artifactRepository.getUrl());

			lookupForDependency.process(artifactRepository.getUrl(), artifactLocation);
		}*/
		// System.out.println(localRepository);

		LookupForDependency lookupForDependency = new LookupForDependency(artifactLocation, remoteArtifactRepositoriesURL);
		lookupForDependency.process();
		
		// getLog().info(settings.toString());
		
	}

	protected void validateArtifactLocation() {

		if (!artifactLocation.exists()) {
			throw new ContextedRuntimeException("ERROR: artifactLocation property is invalid. Please provide -DartifactLocation=<file or folder path>");
		}

	}

	protected void validateRemoteArtifactRepositories() {
		if (CollectionUtils.isEmpty(remoteArtifactRepositories)) {
			throw new ContextedRuntimeException("ERROR: No remote repository found, please check your settings.xml file");
		}
	}

	protected List<String> getRemoteArtifactRepositoriesURL(List<ArtifactRepository> remoteArtifactRepositories) {
		List<String> remoteArtifactRepositoriesURLList = new ArrayList<String>(remoteArtifactRepositories.size());

		for (ArtifactRepository artifactRepository : remoteArtifactRepositories) {
			remoteArtifactRepositoriesURLList.add(artifactRepository.getUrl());
		}

		return remoteArtifactRepositoriesURLList;
	}

	public void execute() throws MojoExecutionException {
		try {
			getSettings();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		File f = outputDirectory;

		if (!f.exists()) {
			f.mkdirs();
		}

		File touch = new File(f, "touch.txt");

		FileWriter w = null;
		try {
			w = new FileWriter(touch);

			w.write("touch.txt");
		} catch (IOException e) {
			throw new MojoExecutionException("Error creating file " + touch, e);
		} finally {
			if (w != null) {
				try {
					w.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}
}
