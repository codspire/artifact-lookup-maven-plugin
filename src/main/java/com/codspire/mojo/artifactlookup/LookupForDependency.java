package com.codspire.mojo.artifactlookup;

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
import com.codspire.mojo.utils.FileChecksum;

//http://www.vineetmanohar.com/2009/11/3-ways-to-run-java-main-from-maven/
//http://stackoverflow.com/questions/3063215/finding-the-right-version-of-the-right-jar-in-a-maven-repository
//http://stackoverflow.com/questions/25047781/how-do-i-retrieve-an-artifact-checksum-from-nexus-using-their-rest-api-via-curl

//java -jar target/nexus-lookup-spring-boot.jar "C:\Users\rnagar\Downloads\dependency-lookup" "https://oss.sonatype.org"
//java -jar target/nexus-lookup-spring-boot.jar "C:\Users\rnagar\Downloads\dependency-lookup" "http://search.maven.org"

//TODO: implement proxy
//TODO: unit tests
//TODO: traverci, git

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

	public LookupForDependency(File artifactLocation, List<String> remoteArtifactRepositoriesURL, File outputDirectory, Log log) {

		try {
			this.plugInConfig = new PropertiesConfiguration("plugin-config.properties");
			this.plugInConfig.setListDelimiter(',');
		} catch (ConfigurationException e) {
			throw new ContextedRuntimeException("Unable to load plugin-config.properties", e);
		}

		this.outputDirectory = outputDirectory;
		this.remoteArtifactRepositoriesURL = remoteArtifactRepositoriesURL;
		this.notFoundList = loadArtifacts(artifactLocation);
		this.foundList = new ArrayList<ProcessingStatus>();
		this.log = log;
	}

	private List<ProcessingStatus> loadArtifacts(File fileOrFolder) {
		List<ProcessingStatus> artifactsDetails = new ArrayList<ProcessingStatus>();

		if (fileOrFolder.isFile()) {
			artifactsDetails.add(new ProcessingStatus(fileOrFolder, FileChecksum.generateSHA1Checksum(fileOrFolder)));
		} else {
			Iterator<File> iterateFiles = FileUtils.iterateFiles(fileOrFolder, plugInConfig.getStringArray(ARTIFACT_FILE_EXTENSIONS), true);

			while (iterateFiles.hasNext()) {
				File file = iterateFiles.next();
				artifactsDetails.add(new ProcessingStatus(file, FileChecksum.generateSHA1Checksum(file)));
			}
		}
		return artifactsDetails;
	}

	public synchronized void process() {
		for (String artifactRepository : remoteArtifactRepositoriesURL) {

			if (CollectionUtils.isNotEmpty(this.notFoundList)) {
				log.info("*********** Checking against ==> " + artifactRepository);

				List<ProcessingStatus> tempNotFoundList = new ArrayList<ProcessingStatus>();
				tempNotFoundList.addAll(this.notFoundList);

				for (ProcessingStatus processingStatus : tempNotFoundList) {
					try {
						log.info("Looking up " + processingStatus.getArtifact().getAbsolutePath());
						// TODO: can ProcessResponse be reused?
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
						processingStatus.setStatusMessage("ERROR: " + processingStatus.getArtifact().getAbsolutePath() + " could not be resolved. " + e.getMessage());
						System.err.println(processingStatus.getStatusMessage());
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

				statusCSV.append(processingStatus.getArtifact().getPath() + "," + processingStatus.getSha1() + "," + (processingStatus.isError() ? "Not Found" : "Found") + ","
						+ gav.getGroupId() + "," + gav.getArtifactId() + "," + gav.getVersion() + "," + processingStatus.getArtifactRepository() + "\n");
			} else {
				statusCSV.append(processingStatus.getArtifact().getPath() + "," + processingStatus.getSha1() + "," + (processingStatus.isError() ? "Not Found" : "Found") + ",,,," + "\n");
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