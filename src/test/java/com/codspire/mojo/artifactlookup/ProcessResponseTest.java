package com.codspire.mojo.artifactlookup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
@RunWith(MockitoJUnitRunner.class)
public class ProcessResponseTest {

	@Mock
	protected Log log;
	
	private PropertiesConfiguration plugInConfig;

	@Before
	public void initMock() throws Exception {
		when(log.isDebugEnabled()).thenReturn(Boolean.TRUE);
		
		plugInConfig = new PropertiesConfiguration("artifact-lookup-maven-plugin.properties");
	}

	@Test
	public void cleanupRepositoryURLShouldConvertToLowerCase() {
		ProcessResponse processResponse = new ProcessResponse();
		
		assertThat(processResponse.cleanupRepositoryURL("https://HelloWorld.Com"), equalTo("https://helloworld.com"));
	}

	@Test
	public void cleanupRepositoryURLShouldBlankForBlankURL() {
		ProcessResponse processResponse = new ProcessResponse();
		
		assertThat(processResponse.cleanupRepositoryURL(" "), equalTo(" "));
	}
	
	@Test
	public void cleanupRepositoryURLShouldRemoveLastSlash() {
		ProcessResponse processResponse = new ProcessResponse();
		
		assertThat(processResponse.cleanupRepositoryURL("https://HelloWorld.Com/"), equalTo("https://helloworld.com"));

		assertThat(processResponse.cleanupRepositoryURL("https://HelloWorld.Com"), equalTo("https://helloworld.com"));
	}

	@Test
	public void getAPIEndpointShouldReturnTheAPIEndpointBasedOnRepoUrl() throws Exception {
		
		String repository = "http://repo.maven.apache.org/maven2";
		ProcessResponse processResponse = new ProcessResponse(repository, plugInConfig, log);
		assertThat(processResponse.getAPIEndpoint(repository), equalTo(plugInConfig.getString("maven.central.repo.search.endpoint")));

		repository = "https://repo.maven.apache.org/maven2/";
		processResponse = new ProcessResponse(repository, plugInConfig, log);
		assertThat(processResponse.getAPIEndpoint(repository), equalTo(plugInConfig.getString("maven.central.repo.search.endpoint")));

		repository = "http://repo1.maven.org/maven2/";
		processResponse = new ProcessResponse(repository, plugInConfig, log);
		assertThat(processResponse.getAPIEndpoint(repository), equalTo(plugInConfig.getString("maven.central.repo.search.endpoint")));
		
		repository = "https://repo1.maven.org/maven2/";
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

	@Test(expected = ContextedRuntimeException.class)
	public void getAPIEndpointShouldThrowExceptionForInvalidNexusRepository() throws Exception {
		
		/* not a valid maven repository */
		String repository = "http://junk-maven-repo.com";
		ProcessResponse processResponse = new ProcessResponse(repository, plugInConfig, log);

		processResponse.getAPIEndpoint(repository);
	}
}