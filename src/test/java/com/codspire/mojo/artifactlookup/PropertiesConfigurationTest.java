package com.codspire.mojo.artifactlookup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Before;
import org.junit.Test;

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
public class PropertiesConfigurationTest {

	protected PropertiesConfiguration config = null;

	@Before
	public void initProperty() throws Exception {
		config = new PropertiesConfiguration("artifact-lookup-maven-plugin.properties");
		config.setListDelimiter(',');
	}

	@Test
	public void testSimpleProperty() throws Exception {
		assertThat(config.getString("default.dependency.filename"), equalTo("pom-dependencies.xml"));
	}

	@Test
	public void testDelimitedProperty() throws Exception {
		assertThat(config.getStringArray("artifact.file.extensions"), equalTo(new String[] { "jar", "JAR", "zip", "ZIP" }));
	}
}