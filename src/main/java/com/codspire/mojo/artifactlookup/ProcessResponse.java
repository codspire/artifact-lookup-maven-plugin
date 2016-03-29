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
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.codspire.mojo.model.GAV;

//http://choosealicense.com/

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

	public ProcessResponse(String repository, PropertiesConfiguration plugInConfig) {
		this.plugInConfig = plugInConfig;
		this.apiEndpoint = getAPIEndpoint(repository);
	}

	public ProcessResponse() {
	}

	private String getAPIEndpoint(String repository) {
		String endpoint = null;
		repository = cleanupRepositoryURL(repository);

		if (repository.contains(plugInConfig.getString(MAVEN_CENTRAL_REPO_DOMAIN_1)) || repository.contains(plugInConfig.getString(MAVEN_CENTRAL_REPO_DOMAIN_2))) {
			endpoint = plugInConfig.getString(MAVEN_CENTRAL_REPO_SEARCH_ENDPOINT);
		} else if (repository.endsWith(plugInConfig.getString(SONATYPE_REPO_URL_ENDS_WITH))) {
			endpoint = repository.replace(plugInConfig.getString(SONATYPE_REPO_URL_ENDS_WITH), plugInConfig.getString(SONATYPE_REPO_SEARCH_ENDPOINT_ENDS_WITH));
		} else {
			throw new ContextedRuntimeException("Endpoint could not be determined for " + repository);
		}
		return endpoint;
	}

	String cleanupRepositoryURL(String repository) {
		repository = (StringUtils.isNotBlank(repository) ? repository.toLowerCase() : repository);

		if (repository.endsWith("/")) {
			repository = repository.substring(0, repository.length() - 1);
		}

		return repository;
	}

	// private String getAPIEndpointOld(String repository) {
	// String endpoint = null;
	// if (repository.toLowerCase().contains("search.maven.org")) {
	// endpoint = "http://search.maven.org/solrsearch/select?wt=xml&q=1:";
	// } else if (repository.toLowerCase().contains("oss.sonatype.org")) {
	// endpoint = "https://oss.sonatype.org/service/local/lucene/search?sha1=";
	// } else {
	// endpoint = repository + "/service/local/lucene/search?sha1=";
	// }
	// return endpoint;
	// }

	// public static void main(String[] args) {
	// // ProcessResponse processResponse = new
	// // ProcessResponse("https://oss.sonatype.org");
	// ProcessResponse processResponse = new
	// ProcessResponse("http://search.maven.org");
	// try {
	// GAV gav =
	// processResponse.lookupRepo("031c70abf97936b5aca0c31c86672b209e1091d8");
	// System.out.println(gav.getGAVXML());
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	public GAV lookupRepo(String sha1Checksum) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			String url = apiEndpoint + sha1Checksum;
			System.out.println("Request URL: " + url);
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
			try {
				responseBody = httpclient.execute(httpGet, responseHandler);

				// System.out.println("----------------------------------------");
				// System.out.println(responseBody);

				gav = getGAV(responseBody);
			} catch (Exception e) {
				throw new ContextedRuntimeException("Unable to get the http response: " + sha1Checksum, e);
			}

			if (gav == null || gav.isIncomlete()) {
				throw new ContextedRuntimeException("No GAV found for " + sha1Checksum);
			}

			return gav;
		} finally {
			try {
				httpclient.close();
			} catch (Exception e) {
			}
		}
	}

	public GAV getGAV(String xml) {
		GAV gav = null;
		try {

			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xml)));

			XPathFactory xPathfactory = XPathFactory.newInstance();
			String groupId, artifactId, version;

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
			e.printStackTrace();
		}

		return gav;
	}

	private static String getXpathValue(Document document, XPathFactory xPathfactory, String targetXpath) {
		XPath xpath = xPathfactory.newXPath();
		String xpathValue = null;
		try {
			xpathValue = xpath.compile(targetXpath).evaluate(document, XPathConstants.STRING).toString();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return xpathValue;
	}
}