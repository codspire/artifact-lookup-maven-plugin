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

	// public static void main(String[] args) throws Exception {
	// String filePath =
	// "C:/Users/rnagar/Downloads/spring-integration-core-4.2.0.RELEASE.jar";
	// FileChecksum fileChecksum = new FileChecksum();
	//
	// String sha1Checksum = fileChecksum.generateSHA1ChecksumV1(filePath);
	// System.out.println("generateSHA1ChecksumV1=" + sha1Checksum);
	//
	// sha1Checksum = fileChecksum.generateSHA1ChecksumV2(filePath);
	// System.out.println("generateSHA1ChecksumV2=" + sha1Checksum);
	// }

	public static String generateSHA1ChecksumV2(File file) {
		FileInputStream fis = null;
		String sha1 = null;
		try {
			fis = new FileInputStream(file);
			sha1 = DigestUtils.sha1Hex(fis);
		} catch (Exception e) {
			// FIXME: add logger for maven
			System.err.println(e.getMessage());
		} finally {
			IOUtils.closeQuietly(fis);
		}
		return sha1;
	}

	// public static String generateSHA1ChecksumV2(String filePath) throws
	// Exception {
	// FileInputStream fis = null;
	// try {
	// fis = new FileInputStream(new File(filePath));
	// return DigestUtils.sha1Hex(fis);
	// } finally {
	// IOUtils.closeQuietly(fis);
	// }
	// }

	// public String generateSHA1ChecksumV1(String filePath) throws Exception {
	// return generateChecksum(filePath, Hash.SHA1);
	// }

	// private String generateChecksum(String filePath, Hash sha1) throws
	// Exception {
	// return toHex(checksum(Hash.SHA1, new File(filePath))).toLowerCase();
	// }

	// private byte[] checksum(Hash hash, File input) throws Exception {
	// // TODO: use java 8 try to handle ip exception
	//
	// InputStream in = null;
	// try {
	// in = new FileInputStream(input);
	//
	// MessageDigest digest = MessageDigest.getInstance(hash.getName());
	//
	// byte[] block = new byte[4096];
	// int length;
	// while ((length = in.read(block)) > 0) {
	// digest.update(block, 0, length);
	// }
	// return digest.digest();
	// } finally {
	// IOUtils.closeQuietly(in);
	//
	// }
	// }

	// private String toHex(byte[] bytes) {
	// return DatatypeConverter.printHexBinary(bytes);
	// }
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
