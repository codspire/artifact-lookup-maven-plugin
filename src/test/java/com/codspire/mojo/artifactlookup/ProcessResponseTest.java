package com.codspire.mojo.artifactlookup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;


//mojo test example
//https://github.com/spotify/docker-maven-plugin/blob/master/src/test/java/com/spotify/docker/BuildMojoTest.java
public class ProcessResponseTest {

	@Test
	public void cleanupRepositoryURLShouldConvertToLowerCase() {
		ProcessResponse processResponse = new ProcessResponse();
		assertThat(processResponse.cleanupRepositoryURL("https://HelloWorld.Com"), equalTo("https://helloworld.com"));
	}

	@Test
	public void cleanupRepositoryURLShouldRemoveLastSlash() {
		ProcessResponse processResponse = new ProcessResponse();
		assertThat(processResponse.cleanupRepositoryURL("https://HelloWorld.Com/"), equalTo("https://helloworld.com"));

		assertThat(processResponse.cleanupRepositoryURL("https://HelloWorld.Com"), equalTo("https://helloworld.com"));
	}
}