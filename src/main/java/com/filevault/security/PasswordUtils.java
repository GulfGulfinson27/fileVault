package com.filevault.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * Utility class for password-based operations like key derivation.
 */
public class PasswordUtils {

    // Default iterations for PBKDF2
    private static final int ITERATIONS = 65536;
    // Key length in bits
    private static final int KEY_LENGTH = 256;
    // Salt length in bytes
    private static final int SALT_LENGTH = 16;

    private static final byte[] DEFAULT_SALT = initDefaultSalt();

    private static byte[] initDefaultSalt() {
        // We use a fixed salt here for simplicity, but ideally this should be 
        // stored separately for each user
        byte[] salt = new byte[SALT_LENGTH];
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            random.nextBytes(salt);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error generating salt: " + e.getMessage());
            // Fallback to a less secure but still usable random generator
            new SecureRandom().nextBytes(salt);
        }
        return salt;
    }

    /**
     * Generates a cryptographic key from a password.
     * Uses PBKDF2 with HMAC-SHA256 for key derivation.
     * 
     * @param password The password to derive the key from
     * @return The derived key as a byte array
     */
    public static byte[] generateKeyFromPassword(String password) {
        return generateKeyFromPassword(password, DEFAULT_SALT);
    }

    /**
     * Generates a cryptographic key from a password with a specific salt.
     * Uses PBKDF2 with HMAC-SHA256 for key derivation.
     * 
     * @param password The password to derive the key from
     * @param salt The salt to use for key derivation
     * @return The derived key as a byte array
     */
    public static byte[] generateKeyFromPassword(String password, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("Error generating key from password: " + e.getMessage());
            throw new RuntimeException("Failed to generate key from password", e);
        }
    }

    /**
     * Generates a random salt for key derivation.
     * 
     * @return A random salt as a byte array
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            random.nextBytes(salt);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error generating salt: " + e.getMessage());
            // Fallback to a less secure but still usable random generator
            new SecureRandom().nextBytes(salt);
        }
        return salt;
    }
} 