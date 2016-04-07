package com.codspire.mojo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.io.FileUtils;
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
public class FileChecksumTest {

	@Test
	public void validateSHA1Checksum() throws Exception {

		File file = new File("validateSHA1Checksum_1.txt");
		FileUtils.write(file, "this is test data for generating sha1 checksum");
		assertThat(FileChecksum.generateSHA1Checksum(file), equalTo("5ff2e623c2bd6b1b003c662159b05e751220bdde"));

		FileUtils.deleteQuietly(file);

		file = new File("validateSHA1Checksum_2.txt");
		FileUtils.write(file, "this is another test data for generating sha1 checksum");
		assertThat(FileChecksum.generateSHA1Checksum(file), equalTo("58f2997b964283847176ff771fdca42d0c094822"));

		FileUtils.deleteQuietly(file);
	}

	@Test(expected = FileNotFoundException.class)
	public void validateSHA1ChecksumIsNullForInvalidFile() throws Exception {

		File file = new File("validateSHA1Checksum_707.txt");
		assertThat(FileChecksum.generateSHA1Checksum(file), equalTo(null));
	}
}
