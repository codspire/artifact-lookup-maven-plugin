package com.codspire.mojo.artifactlookup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
//mojo test example
//https://github.com/spotify/docker-maven-plugin/blob/master/src/test/java/com/spotify/docker/BuildMojoTest.java

//http://maven.apache.org/plugin-testing/maven-plugin-testing-harness/getting-started/index.html
//http://stackoverflow.com/questions/15779351/component-lookup-exception-with-org-apache-maven-repository-repositorysystem-in
public class ArtifactLookupMojoTest {

	private static final File ARTIFACT_LOCATION = new File("src/test/resources/jars");
	private static final File OUTPUT_DIRECTORY = new File("target");

	private static PropertiesConfiguration plugInConfig;

	@BeforeClass
	public static void initBefore() throws Exception {
		plugInConfig = new PropertiesConfiguration("plugin-config.properties");
	}

	//@After
	public void cleanup() {

		FileUtils.deleteQuietly(new File(OUTPUT_DIRECTORY.getAbsoluteFile() + File.separator + plugInConfig.getString("default.dependency.filename")));
		FileUtils.deleteQuietly(new File(OUTPUT_DIRECTORY.getAbsoluteFile() + File.separator + plugInConfig.getString("default.lookup.status.filename")));
	}

	@Test
	public void artifactLookupMojoShouldReturnMatchingDependenciesBasedOnRepositoryUrl() throws Exception {

		ArtifactLookupMojo artifactLookupMojo = new ArtifactLookupMojo();

		artifactLookupMojo.artifactLocation = ARTIFACT_LOCATION;
		artifactLookupMojo.outputDirectory = OUTPUT_DIRECTORY;
		artifactLookupMojo.repositoryUrl = "https://oss.sonatype.org/content/groups/public/";

		artifactLookupMojo.execute();

		assertThat("The files differ!", getExpectedFileContent("expected-pom-dependencies-1.xml"), equalTo(getGeneratedFileContent("default.dependency.filename")));
		assertThat("The files differ!", getExpectedFileContent("extected-dependency-status-1.csv"), equalTo(getGeneratedFileContent("default.lookup.status.filename")));

	}

	private String getGeneratedFileContent(String fileType) throws Exception {
		return FileUtils.readFileToString(new File(OUTPUT_DIRECTORY.getAbsoluteFile() + File.separator + plugInConfig.getString(fileType)), "utf-8");
	}

	private String getExpectedFileContent(String fileName) throws Exception {
		return FileUtils.readFileToString(new File("src/test/resources/jars" + File.separator + fileName), "utf-8");
	}

	@Test
	public void artifactLookupMojoShouldReturnMatchingDependenciesBasedOnArtifactRepository() throws Exception {

		ArtifactLookupMojo artifactLookupMojo = new ArtifactLookupMojo();

		artifactLookupMojo.artifactLocation = ARTIFACT_LOCATION;
		artifactLookupMojo.outputDirectory = OUTPUT_DIRECTORY;
		artifactLookupMojo.remoteArtifactRepositories = getArtifactRepositories();

		artifactLookupMojo.execute();

		assertThat("The files differ!", getExpectedFileContent("expected-pom-dependencies-2.xml"), equalTo(getGeneratedFileContent("default.dependency.filename")));
		assertThat("The files differ!", getExpectedFileContent("extected-dependency-status-2.csv"), equalTo(getGeneratedFileContent("default.lookup.status.filename")));
	}

	@Test
	public void artifactLookupMojoShouldReturnMatchingDependenciesBasedOnArtifactRepositoryForSingleFile() throws Exception {

		ArtifactLookupMojo artifactLookupMojo = new ArtifactLookupMojo();

		artifactLookupMojo.artifactLocation = new File(ARTIFACT_LOCATION.getPath() + File.separator + "commons-io.jar");
		artifactLookupMojo.outputDirectory = OUTPUT_DIRECTORY;
		artifactLookupMojo.remoteArtifactRepositories = getArtifactRepositories();

		artifactLookupMojo.execute();

		assertThat("The files differ!", getExpectedFileContent("expected-pom-dependencies-3.xml"), equalTo(getGeneratedFileContent("default.dependency.filename")));
		assertThat("The files differ!", getExpectedFileContent("extected-dependency-status-3.csv"), equalTo(getGeneratedFileContent("default.lookup.status.filename")));
	}

	private List<ArtifactRepository> getArtifactRepositories() {
		List<ArtifactRepository> artifactRepositories = new ArrayList<ArtifactRepository>();

		ArtifactRepository repo = new MavenArtifactRepository();
		repo.setUrl("https://oss.sonatype.org/content/groups/public/");
		artifactRepositories.add(repo);

		repo = new MavenArtifactRepository();
		repo.setUrl("https://repo.maven.apache.org/maven2/");
		artifactRepositories.add(repo);

		return artifactRepositories;
	}	
}