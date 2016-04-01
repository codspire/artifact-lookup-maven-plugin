package com.codspire.mojo.artifactlookup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

//mojo test example
//https://github.com/spotify/docker-maven-plugin/blob/master/src/test/java/com/spotify/docker/BuildMojoTest.java
@RunWith(MockitoJUnitRunner.class)
public class ProcessResponseTest {

	@Mock
	Log log;

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

	@Test
	public void getAPIEndpointShouldReturnTheAPIEndpointBasedOnRepoUrl() throws Exception {

		PropertiesConfiguration plugInConfig = new PropertiesConfiguration("plugin-config.properties");
		System.out.println(log);
		String repository = "http://repo.maven.apache.org/maven2";
		ProcessResponse processResponse = new ProcessResponse(repository, plugInConfig, log);

		assertThat(processResponse.getAPIEndpoint(repository), equalTo(plugInConfig.getString("maven.central.repo.search.endpoint")));

		repository = "https://repo.maven.apache.org/maven2/";

		processResponse = new ProcessResponse(repository, plugInConfig, log);

		assertThat(processResponse.getAPIEndpoint(repository), equalTo(plugInConfig.getString("maven.central.repo.search.endpoint")));

		repository = "https://repository.jboss.org/nexus/content/groups/public/";
		processResponse = new ProcessResponse(repository, plugInConfig, log);

		assertThat(processResponse.getAPIEndpoint(repository), equalTo("https://repository.jboss.org/nexus/service/local/lucene/search?sha1="));

		repository = "https://repository.jboss.org/nexus/content/groups/public/";
		processResponse = new ProcessResponse(repository, plugInConfig, log);

		assertThat(processResponse.getAPIEndpoint(repository), equalTo("https://repository.jboss.org/nexus/service/local/lucene/search?sha1="));

		repository = "https://oss.sonatype.org/content/groups/public/";
		processResponse = new ProcessResponse(repository, plugInConfig, log);

		assertThat(processResponse.getAPIEndpoint(repository), equalTo("https://oss.sonatype.org/service/local/lucene/search?sha1="));
	}
}