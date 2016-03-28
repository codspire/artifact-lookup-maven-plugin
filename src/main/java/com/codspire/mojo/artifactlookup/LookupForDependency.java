package com.codspire.mojo.artifactlookup;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

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
	private static final String DEFAULT_LOOKUP_REPO = "https://oss.sonatype.org/content/groups/public/";
	private static final String DEFAULT_DEPENCENCY_FILE_NAME = "pom-dependencies.xml";
	private static final String DEFAULT_STATUS_FILE_NAME = "dependency-status.csv";

	private List<ProcessingStatus> foundList = null;
	private List<ProcessingStatus> notFoundList = null;
	private List<String> remoteArtifactRepositoriesURL;

	public LookupForDependency(File artifactLocation, List<String> remoteArtifactRepositoriesURL) {
		this.remoteArtifactRepositoriesURL = remoteArtifactRepositoriesURL;
		this.notFoundList = loadArtifacts(artifactLocation);
		this.foundList = new ArrayList<ProcessingStatus>();
	}

	@SuppressWarnings("unchecked")
	private List<ProcessingStatus> loadArtifacts(File fileOrFolder) {
		List<ProcessingStatus> artifactsDetails = new ArrayList<ProcessingStatus>();

		if (fileOrFolder.isFile()) {
			artifactsDetails.add(new ProcessingStatus(fileOrFolder, FileChecksum.generateSHA1ChecksumV2(fileOrFolder)));
		} else {
			// TODO: use properties file
			Iterator<File> iterateFiles = FileUtils.iterateFiles(fileOrFolder, new String[] { "jar", "JAR", "zip", "ZIP" }, true);

			while (iterateFiles.hasNext()) {
				File file = iterateFiles.next();
				artifactsDetails.add(new ProcessingStatus(file, FileChecksum.generateSHA1ChecksumV2(file)));
			}
		}
		return artifactsDetails;
	}

	public LookupForDependency() {
	}

	public static void main(String[] args) {

		if (args != null && args.length > 0) {
			String path = args[0];
			String lookupRepo = null;

			File fileOrFolder = new File(path);
			String[] files;

			if (fileOrFolder.isFile()) {
				files = new String[] { fileOrFolder.getAbsolutePath() };
			} else {

				Iterator<File> filesList = FileUtils.iterateFiles(fileOrFolder, new String[] { "jar", "JAR", "zip", "ZIP" }, true);

				files = getFilePaths(filesList);
			}

			if (args.length > 1) {
				lookupRepo = args[1];
			}

			LookupForDependency lookupForDependency = new LookupForDependency();

			lookupForDependency.process(lookupRepo, files);
		}
	}

	public void process(String lookupRepo, File artifactLocation) {
		String[] files;

		if (artifactLocation.isFile()) {
			files = new String[] { artifactLocation.getAbsolutePath() };
		} else {
			Iterator<File> filesList = FileUtils.iterateFiles(artifactLocation, new String[] { "jar", "JAR", "zip", "ZIP" }, true);
			files = getFilePaths(filesList);
		}

		process(lookupRepo, files);
	}

	private static String[] getFilePaths(Iterator<File> filesList) {
		List<String> filesPath = new ArrayList<String>();

		while (filesList.hasNext()) {
			filesPath.add(filesList.next().getAbsolutePath());
		}
		return filesPath.toArray(new String[0]);
	}

	private List<ProcessingStatus> process(String lookupRepo, String... jars) {
		String finalLookupRepo = StringUtils.isNotBlank(lookupRepo) ? lookupRepo : DEFAULT_LOOKUP_REPO;
		FileChecksum fileChecksum = new FileChecksum();
		List<ProcessingStatus> processingStatusList = new ArrayList<ProcessingStatus>();

		ProcessResponse processResponse = new ProcessResponse(finalLookupRepo);

		System.out.println("Using Repo: " + finalLookupRepo);

		String sha1;

		ProcessingStatus processingStatus = null;

		for (String jar : jars) {
			try {
				processingStatus = new ProcessingStatus();
				// processingStatus.setFilePath(jar);

				sha1 = fileChecksum.generateSHA1ChecksumV2(jar);
				GAV gav = processResponse.lookupRepo(sha1);

				processingStatus.setGav(gav);
				processingStatus.markSuccess();
				processingStatus.setStatusMessage("Success");
				processingStatus.setArtifactRepository(lookupRepo);

			} catch (Exception e) {
				processingStatus.markError();
				processingStatus.setStatusMessage("ERROR: " + jar + " could not be resolved. " + e.getMessage());
				System.err.println(processingStatus.getStatusMessage());
			}

			processingStatusList.add(processingStatus);
		}

		writeToFile(processingStatusList);

		return processingStatusList;
	}

	private void writeToFile(List<ProcessingStatus> processingStatusList) {
		StringBuilder dependencyPom = new StringBuilder();
		StringBuilder statusCSV = new StringBuilder();

		dependencyPom.append("\n<dependencies>\n");
		statusCSV.append("File,Status,GroupId,ArtifactId,Version\n");

		for (ProcessingStatus processingStatus : processingStatusList) {

			GAV gav = processingStatus.getGav();

			if (!processingStatus.isError() && gav != null) {
				dependencyPom.append("<!-- Resolved from " + processingStatus.getArtifactRepository() + "-->\n");
				dependencyPom.append(gav.getGAVXML());

				statusCSV.append(processingStatus.getArtifact().getAbsolutePath() + "," + (processingStatus.isError() ? "Not Found" : "Found") + "," + gav.getGroupId() + "," + gav.getArtifactId()
						+ "," + gav.getVersion() + "\n");
			} else {
				statusCSV.append(processingStatus.getArtifact().getAbsolutePath() + "," + (processingStatus.isError() ? "Not Found" : "Found") + ",,," + "\n");
			}
		}

		dependencyPom.append("</dependencies>\n");

		writeToFile(dependencyPom, DEFAULT_DEPENCENCY_FILE_NAME);
		writeToFile(statusCSV, DEFAULT_STATUS_FILE_NAME);
	}

	private void writeToFile(StringBuilder stringBuilder, String fileName) {
		try {
			File outputFile = new File(fileName);
			FileUtils.write(outputFile, stringBuilder.toString());
			System.out.println("\nCreated dependencies file at: " + outputFile.getAbsolutePath());

			System.out.println(stringBuilder.toString());

		} catch (Exception e) {
			System.err.println("ERROR: Could not create " + fileName);
		}
	}

	public synchronized void process() {
		for (String artifactRepository : remoteArtifactRepositoriesURL) {

			if (CollectionUtils.isNotEmpty(this.notFoundList)) {
				System.out.println("*********** Checking against ==> " + artifactRepository);

				List<ProcessingStatus> tempNotFoundList = new ArrayList<ProcessingStatus>();
				tempNotFoundList.addAll(this.notFoundList);

				for (ProcessingStatus processingStatus : tempNotFoundList) {
					try {
						System.out.println("Looking up "+processingStatus.getArtifact().getAbsolutePath());
						// TODO: can ProcessResponse be reused?
						ProcessResponse processResponse = new ProcessResponse(artifactRepository);
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

					System.out.println("notFoundList size = " + this.notFoundList.size());
					System.out.println("FoundList size = " + this.foundList.size());
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

		writeToFile(dependencyPom, DEFAULT_DEPENCENCY_FILE_NAME);
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

				statusCSV.append(processingStatus.getArtifact().getAbsolutePath() + "," + processingStatus.getSha1() + "," + (processingStatus.isError() ? "Not Found" : "Found") + ","
						+ gav.getGroupId() + "," + gav.getArtifactId() + "," + gav.getVersion() + "," + processingStatus.getArtifactRepository() + "\n");
			} else {
				statusCSV.append(processingStatus.getArtifact().getAbsolutePath() + "," + processingStatus.getSha1() + "," + (processingStatus.isError() ? "Not Found" : "Found") + ",,,," + "\n");
			}
		}

		writeToFile(statusCSV, DEFAULT_STATUS_FILE_NAME);
	}
}