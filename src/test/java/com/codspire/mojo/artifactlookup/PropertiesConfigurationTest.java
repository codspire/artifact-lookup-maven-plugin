package com.codspire.mojo.artifactlookup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;

public class PropertiesConfigurationTest {

	@Test
	public void loadProperty() throws Exception {

		PropertiesConfiguration config = new PropertiesConfiguration("plugin-config.properties");
		assertThat(config.getString("test"), equalTo("1234"));

		config.setListDelimiter(',');
		assertThat(config.getStringArray("artifact.file.extensions"), equalTo(new String[] { "jar", "JAR", "zip", "ZIP" }));

	}
}