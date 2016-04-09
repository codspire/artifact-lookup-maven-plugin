package com.codspire.mojo.artifactlookup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;

import org.junit.Test;

import com.codspire.mojo.model.GAV;
import com.codspire.mojo.model.ProcessingStatus;

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
public class ModelTest {

	@Test
	public void gavIsIncompleteShouldReturnCorrectStatus() {

		assertThat((new GAV("", "", "")).isIncomlete(), is(true));

		assertThat((new GAV("g", "a", "")).isIncomlete(), is(true));
		
		assertThat((new GAV("g", " ", "")).isIncomlete(), is(true));

		assertThat((new GAV("", "a", "")).isIncomlete(), is(true));

		assertThat((new GAV("", "a", "v")).isIncomlete(), is(true));

		assertThat((new GAV("g", "a", "v")).isIncomlete(), is(false));
	}

	@Test
	public void processingStatusEqualsShouldReturnCorrectStatus() {

		assertThat((new ProcessingStatus(new File("file"), "a")).equals(new ProcessingStatus(new File("file"), "a")), is(true));

		assertThat((new ProcessingStatus(new File("file"), "a")).equals(new ProcessingStatus(new File("file2"), "a")), is(false));

		assertThat((new ProcessingStatus(new File("file"), "a")).equals(new ProcessingStatus(new File("file"), "b")), is(false));

		assertThat((new ProcessingStatus(new File("file"), "a")).equals(new Object()), is(false));
	}

}