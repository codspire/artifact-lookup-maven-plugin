package com.codspire.mojo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

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
}
