package com.codspire.mojo.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
public class GAV {

	private String groupId;
	private String artifactId;
	private String version;

	public String getGroupId() {
		return groupId;
	}

//	public void setGroupId(String groupId) {
//		this.groupId = groupId;
//	}

	public String getArtifactId() {
		return artifactId;
	}

//	public void setArtifactId(String artifactId) {
//		this.artifactId = artifactId;
//	}

	public String getVersion() {
		return version;
	}

//	public void setVersion(String version) {
//		this.version = version;
//	}

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

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("groupId", groupId).append("artifactId", artifactId).append("version", version).toString();
	}
}
