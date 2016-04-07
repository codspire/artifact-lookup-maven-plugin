package com.codspire.mojo.artifactlookup;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.codspire.mojo.model.GAV;

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
public class ProcessResponse {
	private static final String SONATYPE_GROUP_ID_XPATH = "sonatype.group.id.xpath";
	private static final String SONATYPE_ARTIFACT_ID_XPATH = "sonatype.artifact.id.xpath";
	private static final String SONATYPE_VERSION_XPATH = "sonatype.version.xpath";
	private static final String SONATYPE_REPO_URL_ENDS_WITH = "sonatype.repo.url.ends.with";
	private static final String SONATYPE_REPO_SEARCH_ENDPOINT_ENDS_WITH = "sonatype.repo.search.endpoint.ends.with";
	private static final String MAVEN_CENTRAL_REPO_DOMAIN_1 = "maven.central.repo.domain.1";
	private static final String MAVEN_CENTRAL_REPO_DOMAIN_2 = "maven.central.repo.domain.2";
	private static final String MAVEN_CENTRAL_REPO_SEARCH_ENDPOINT = "maven.central.repo.search.endpoint";

	private String apiEndpoint;
	private static final String SEARCH_MAVEN_GAV_XPATH = "search.maven.gav.xpath";

	private PropertiesConfiguration plugInConfig;
	private Log log;

	/**
	 * 
	 */
	public ProcessResponse() {
	}

	/**
	 * 
	 * @param repository
	 * @param plugInConfig
	 * @param log
	 */
	public ProcessResponse(String repository, PropertiesConfiguration plugInConfig, Log log) {
		this.plugInConfig = plugInConfig;
		this.apiEndpoint = getAPIEndpoint(repository);
		this.log = log;
	}

	/**
	 * 
	 * @param repository
	 * @return
	 */
	protected String getAPIEndpoint(String repositoryUrl) {
		String endpoint = null;
		String repository = cleanupRepositoryURL(repositoryUrl);

		if (repository.contains(plugInConfig.getString(MAVEN_CENTRAL_REPO_DOMAIN_1)) || repository.contains(plugInConfig.getString(MAVEN_CENTRAL_REPO_DOMAIN_2))) {
			endpoint = plugInConfig.getString(MAVEN_CENTRAL_REPO_SEARCH_ENDPOINT);
		} else if (repository.endsWith(plugInConfig.getString(SONATYPE_REPO_URL_ENDS_WITH))) {
			endpoint = repository.replace(plugInConfig.getString(SONATYPE_REPO_URL_ENDS_WITH), plugInConfig.getString(SONATYPE_REPO_SEARCH_ENDPOINT_ENDS_WITH));
		} else {
			throw new ContextedRuntimeException("Endpoint could not be determined for " + repository);
		}
		return endpoint;
	}

	/**
	 * 
	 * @param repository
	 * @return
	 */
	protected String cleanupRepositoryURL(String repository) {
		repository = (StringUtils.isNotBlank(repository) ? repository.toLowerCase() : repository);

		if (repository.endsWith("/")) {
			repository = repository.substring(0, repository.length() - 1);
		}

		return repository;
	}

	/**
	 * 
	 * @param sha1Checksum
	 * @return
	 */
	public GAV lookupRepo(String sha1Checksum) {

		// try {
		String url = apiEndpoint + sha1Checksum;
		log.info("Request URL: " + url);
		HttpGet httpGet = new HttpGet(url);

		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

			public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					throw new ClientProtocolException("Unexpected response status: " + status);
				}
			}

		};

		String responseBody = null;
		GAV gav;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			responseBody = httpclient.execute(httpGet, responseHandler);

			if (log.isDebugEnabled()) {
				log.debug("----------------------------------------");
				log.debug(responseBody);
				log.debug("----------------------------------------");
			}

			gav = getGAV(responseBody);

			if (log.isDebugEnabled()) {
				log.debug(gav.toString());
			}

		} catch (Exception e) {
			throw new ContextedRuntimeException("Unable to get the http response: " + url, e);
		} finally {
			try {
				httpclient.close();
			} catch (Exception e) {
			}
		}

		if (gav == null || gav.isIncomlete()) {
			throw new ContextedRuntimeException("No GAV found for " + sha1Checksum);
		}

		return gav;
	}

	/**
	 * 
	 * @param xml
	 * @return
	 */
	public GAV getGAV(String xml) {
		GAV gav = null;
		try {
			String groupId;
			String artifactId;
			String version;

			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xml)));

			XPathFactory xPathfactory = XPathFactory.newInstance();

			groupId = getXpathValue(document, xPathfactory, plugInConfig.getString(SONATYPE_GROUP_ID_XPATH));

			if (StringUtils.isNotBlank(groupId)) {

				artifactId = getXpathValue(document, xPathfactory, plugInConfig.getString(SONATYPE_ARTIFACT_ID_XPATH));
				version = getXpathValue(document, xPathfactory, plugInConfig.getString(SONATYPE_VERSION_XPATH));

				gav = new GAV(groupId, artifactId, version);
			} else {

				String[] gavArray = getXpathValue(document, xPathfactory, plugInConfig.getString(SEARCH_MAVEN_GAV_XPATH)).split(":");
				if (gavArray.length >= 3) {

					gav = new GAV(gavArray[0], gavArray[1], gavArray[2]);
				}
			}

		} catch (Exception e) {
			log.error(e);
		}

		return gav;
	}

	/**
	 * 
	 * @param document
	 * @param xPathfactory
	 * @param targetXpath
	 * @return
	 */
	private String getXpathValue(Document document, XPathFactory xPathfactory, String targetXpath) {
		XPath xpath = xPathfactory.newXPath();
		String xpathValue = null;
		try {
			xpathValue = xpath.compile(targetXpath).evaluate(document, XPathConstants.STRING).toString();
		} catch (XPathExpressionException e) {
			log.error(e);
		}
		return xpathValue;
	}
}