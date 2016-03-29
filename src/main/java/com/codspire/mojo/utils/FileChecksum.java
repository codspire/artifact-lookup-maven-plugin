package com.codspire.mojo.utils;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

//http://memorynotfound.com/calculate-file-checksum-java/
//http://choosealicense.com/
//TODO: use the implementation from http://omtlab.com/how-to-generate-md5-and-sha1-checksum-in-java/
//TODO: remove maven build warning
public class FileChecksum {

	public static String generateSHA1ChecksumV2(File file) {
		FileInputStream fis = null;
		String sha1 = null;
		try {
			fis = new FileInputStream(file);
			sha1 = DigestUtils.sha1Hex(fis);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fis);
		}
		return sha1;
	}
}

enum Hash {

	MD5("MD5"), SHA1("SHA1"), SHA256("SHA-256"), SHA512("SHA-512");

	private String name;

	Hash(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
