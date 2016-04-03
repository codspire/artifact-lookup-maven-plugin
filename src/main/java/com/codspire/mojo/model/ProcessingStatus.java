package com.codspire.mojo.model;

import java.io.File;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
public class ProcessingStatus {
	private File artifact;
	private GAV gav;
	private boolean isError = false;
	private String statusMessage;
	private String artifactRepository;
	private String sha1;

	public File getArtifact() {
		return artifact;
	}

//	public void setArtifact(File artifact) {
//		this.artifact = artifact;
//	}

	public String getSha1() {
		return sha1;
	}

//	public void setSha1(String sha1) {
//		this.sha1 = sha1;
//	}

	public String getArtifactRepository() {
		return artifactRepository;
	}

	public void setArtifactRepository(String artifactRepository) {
		this.artifactRepository = artifactRepository;
	}

	public GAV getGav() {
		return gav;
	}

	public void setGav(GAV gav) {
		this.gav = gav;
	}

	public boolean isError() {
		return isError;
	}

	public void markError() {
		this.isError = true;
	}

	public void markSuccess() {
		this.isError = false;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

//	public ProcessingStatus() {
//	}

	public ProcessingStatus(File artifact, String sha1) {
		super();
		this.artifact = artifact;
		this.sha1 = sha1;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(artifact).append(sha1).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ProcessingStatus) {
			final ProcessingStatus other = (ProcessingStatus) obj;
			return new EqualsBuilder().append(artifact, other.artifact).append(sha1, other.sha1).isEquals();
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("artifact", artifact).append("artifactRepository", artifactRepository).append("gav", gav).append("isError", isError).append("sha1", sha1)
				.append("statusMessage", statusMessage).toString();
	}
}
