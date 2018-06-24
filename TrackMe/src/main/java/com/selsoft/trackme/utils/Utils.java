package com.selsoft.trackme.utils;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Utils {

	private static Random random = new Random((new Date()).getTime());

	/**
	 * Encrypts the string along with salt
	 * 
	 * @param password
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("restriction")
	public static String encryptPassword(String password) {

		// encryption
		BASE64Encoder encoder = new BASE64Encoder();

		// let's create some dummy salt
		byte[] salt = new byte[8];
		random.nextBytes(salt);
		return encoder.encode(salt) + encoder.encode(password.getBytes());

	}

	/**
	 * Decrypts the string and removes the salt
	 * 
	 * @param password
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("restriction")
	public static String decryptPassword(String password) {

		// decryption

		if (password.length() > 12) {
			String cipher = password.substring(12);
			BASE64Decoder decoder = new BASE64Decoder();
			try {
				return new String(decoder.decodeBuffer(cipher));
			} catch (IOException e) {
				// throw custom exception object
			}
		}
		return null;
	}
}
