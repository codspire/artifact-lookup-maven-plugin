package com.codspire.mojo.model;

import org.apache.commons.lang3.StringUtils;

public class GAV {

	private String groupId;
	private String artifactId;
	private String version;

	// private String sha1;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public GAV(String groupId, String artifactId, String version) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}

	public boolean isIncomlete() {
		return StringUtils.isBlank(groupId) || StringUtils.isBlank(artifactId) || StringUtils.isBlank(version);
	}

	public String getGAVXML() {

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("\t<dependency>\n");

		stringBuilder.append("\t\t<groupId>" + groupId + "</groupId>\n");

		stringBuilder.append("\t\t<artifactId>" + artifactId + "</artifactId>\n");

		stringBuilder.append("\t\t<version>" + version + "</version>\n");

		stringBuilder.append("\t</dependency>\n");

		return stringBuilder.toString();
	}

	// public String getSha1() {
	// return sha1;
	// }
	//
	// public void setSha1(String sha1) {
	// this.sha1 = sha1;
	// }
}
