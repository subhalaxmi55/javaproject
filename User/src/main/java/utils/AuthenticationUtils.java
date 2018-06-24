package com.selsoft.user.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationUtils {
    
	private static final Random RANDOM_VALUE = new SecureRandom();
	
    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    
    public String createSaltValue(int length) {
        StringBuilder returnValue = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            returnValue.append(ALPHA_NUMERIC.charAt(RANDOM_VALUE.nextInt(ALPHA_NUMERIC.length())));
        }
        return new String(returnValue);
    }
    
    public String createSecureUserId(int length) {
        return createSaltValue(length);
    }
    
    public String createSecurePassword(String password, String salt) throws InvalidKeySpecException {
 
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());
        return Base64.getEncoder().encodeToString(securePassword);
 
    }
    
    private byte[] hash(char[] password, byte[] salt) throws InvalidKeySpecException {
    	
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(password, Character.MIN_VALUE);
        
        try {
        	
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
        
    }
    
    public SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        SecretKey returnValue = null;
        KeyGenerator secretKeyGenerator = KeyGenerator.getInstance("DESede");
        secretKeyGenerator.init(112);
        returnValue = secretKeyGenerator.generateKey();
        return returnValue;
    }
    
    public byte[] encrypt(String securePassword, String accessTokenMaterial) throws InvalidKeySpecException {
        return hash(securePassword.toCharArray(), accessTokenMaterial.getBytes());
    }
 
}