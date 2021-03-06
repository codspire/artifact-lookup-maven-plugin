package com.codspire.mojo.artifactlookup;

import static com.codspire.mojo.utils.FileChecksum.generateSHA1Checksum;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.maven.plugin.logging.Log;

import com.codspire.mojo.model.GAV;
import com.codspire.mojo.model.ProcessingStatus;

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
public class LookupForDependency {
	private static final String DEFAULT_DEPENDENCY_FILENAME = "default.dependency.filename";
	private static final String DEFAULT_LOOKUP_STATUS_FILENAME = "default.lookup.status.filename";
	private static final String ARTIFACT_FILE_EXTENSIONS = "artifact.file.extensions";

	private List<ProcessingStatus> foundList = null;
	private List<ProcessingStatus> notFoundList = null;
	private List<String> remoteArtifactRepositoriesURL;
	private PropertiesConfiguration plugInConfig = null;
	private File outputDirectory;
	private Log log;

	public LookupForDependency(File artifactLocation, boolean recursive, List<String> remoteArtifactRepositoriesURL, File outputDirectory, Log log) {

		try {
			this.plugInConfig = new PropertiesConfiguration("artifact-lookup-maven-plugin.properties");
			this.plugInConfig.setListDelimiter(',');
		} catch (ConfigurationException e) {
			throw new ContextedRuntimeException("Unable to load artifact-lookup-maven-plugin.properties", e);
		}

		this.log = log;
		this.outputDirectory = outputDirectory;
		this.remoteArtifactRepositoriesURL = remoteArtifactRepositoriesURL;
		this.notFoundList = loadArtifacts(artifactLocation, recursive);
		this.foundList = new ArrayList<ProcessingStatus>();
	}

	private List<ProcessingStatus> loadArtifacts(File fileOrFolder, boolean recursive) {
		if (log.isDebugEnabled()) {
			log.debug("Artifact Location: " + fileOrFolder + " Find recursive = " + recursive);
		}

		List<ProcessingStatus> artifactsDetails = new ArrayList<ProcessingStatus>();

		if (fileOrFolder.isFile()) {
			artifactsDetails.add(new ProcessingStatus(fileOrFolder, getChecksumForFile(fileOrFolder)));
		} else {
			Iterator<File> iterateFiles = FileUtils.iterateFiles(fileOrFolder, plugInConfig.getStringArray(ARTIFACT_FILE_EXTENSIONS), recursive);

			while (iterateFiles.hasNext()) {
				File file = iterateFiles.next();
				artifactsDetails.add(new ProcessingStatus(file, getChecksumForFile(file)));
			}
		}

		return artifactsDetails;
	}

	protected String getChecksumForFile(File file) {

		try {
			return generateSHA1Checksum(file);
		} catch (Exception e) {
			log.error("Error generating SHA1 Checksum for " + file.getPath() + ". " + e.getMessage());
			return null;
		}
	}

	public synchronized void process() {
		for (String artifactRepository : remoteArtifactRepositoriesURL) {

			if (CollectionUtils.isNotEmpty(this.notFoundList)) {
				log.info("------------------------------------------------------------------------");
				log.info("*********** Checking against ==> " + artifactRepository);
				log.info("------------------------------------------------------------------------");

				List<ProcessingStatus> tempNotFoundList = new ArrayList<ProcessingStatus>();
				tempNotFoundList.addAll(this.notFoundList);

				for (ProcessingStatus processingStatus : tempNotFoundList) {
					try {
						if (log.isDebugEnabled()) {
							log.info("Looking up " + processingStatus.toString());
						}
						log.info("Looking up " + processingStatus.getArtifact().getAbsolutePath());

						ProcessResponse processResponse = new ProcessResponse(artifactRepository, plugInConfig, log);

						GAV gav = processResponse.lookupRepo(processingStatus.getSha1());

						processingStatus.setGav(gav);
						processingStatus.markSuccess();
						processingStatus.setStatusMessage("Success");
						processingStatus.setArtifactRepository(artifactRepository);

						this.notFoundList.remove(processingStatus);
						this.foundList.add(processingStatus);
					} catch (Exception e) {
						processingStatus.markError();
						processingStatus.setStatusMessage("WARN: " + processingStatus.getArtifact().getAbsolutePath() + " could not be resolved. " + e.getMessage());
						log.warn(processingStatus.getStatusMessage());
					}

					log.info("Not Found Count = " + this.notFoundList.size());
					log.info("Found Count = " + this.foundList.size());
				}
			}
		}
		writePOMDependencies();
		writeCSVFile();
	}

	private synchronized void writePOMDependencies() {

		StringBuilder dependencyPom = new StringBuilder();

		dependencyPom.append("\n<dependencies>\n");

		for (ProcessingStatus processingStatus : this.foundList) {

			GAV gav = processingStatus.getGav();

			if (!processingStatus.isError() && gav != null) {
				dependencyPom.append("\t<!-- Resolved from " + processingStatus.getArtifactRepository() + " -->\n");
				dependencyPom.append(gav.getGAVXML());
			}
		}

		dependencyPom.append("</dependencies>\n");

		log.info("------------------------------------------------------------------------");
		log.info("DEPENDENCIES");
		log.info("------------------------------------------------------------------------");
		log.info(dependencyPom.toString());
		log.info("------------------------------------------------------------------------");
		writeToFile(dependencyPom, outputDirectory + File.separator + plugInConfig.getString(DEFAULT_DEPENDENCY_FILENAME));
	}

	private synchronized void writeCSVFile() {

		StringBuilder statusCSV = new StringBuilder();

		statusCSV.append("File,SHA1,Status,GroupId,ArtifactId,Version,ArtifactRepository\n");
		List<ProcessingStatus> allProcessingStatus = new ArrayList<ProcessingStatus>();
		allProcessingStatus.addAll(foundList);
		allProcessingStatus.addAll(notFoundList);

		for (ProcessingStatus processingStatus : allProcessingStatus) {

			GAV gav = processingStatus.getGav();

			if (!processingStatus.isError() && gav != null) {
				statusCSV.append(processingStatus.getArtifact().getPath() + "," + processingStatus.getSha1() + "," + "Found" + "," + gav.getGroupId() + "," + gav.getArtifactId() + ","
						+ gav.getVersion() + "," + processingStatus.getArtifactRepository() + "\n");
			} else {
				statusCSV.append(processingStatus.getArtifact().getPath() + "," + processingStatus.getSha1() + "," + "Not Found" + ",,,," + "\n");
			}
		}

		if (log.isDebugEnabled()) {
			log.debug(statusCSV.toString());
		}

		writeToFile(statusCSV, outputDirectory + File.separator + plugInConfig.getString(DEFAULT_LOOKUP_STATUS_FILENAME));
	}

	private void writeToFile(StringBuilder stringBuilder, String fileName) {
		try {
			File outputFile = new File(fileName);
			FileUtils.write(outputFile, stringBuilder.toString());
			log.info("\nCreated dependencies file at: " + outputFile.getAbsolutePath());
		} catch (Exception e) {
			log.error("ERROR: Could not create " + fileName, e);
		}
	}
}