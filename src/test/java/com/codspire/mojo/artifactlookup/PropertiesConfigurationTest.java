package com.codspire.mojo.artifactlookup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Before;
import org.junit.Test;

public class PropertiesConfigurationTest {

	PropertiesConfiguration config = null;

	@Before
	public void initProperty() throws Exception {
		config = new PropertiesConfiguration("plugin-config.properties");
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