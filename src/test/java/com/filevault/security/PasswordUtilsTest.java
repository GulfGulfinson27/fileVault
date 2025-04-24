package com.filevault.security;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordUtils class.
 */
public class PasswordUtilsTest {

    @Test
    public void testGenerateKeyFromPassword() {
        String password = "securePassword123";
        byte[] key = PasswordUtils.generateKeyFromPassword(password);

        assertNotNull(key, "Generated key should not be null");
        assertEquals(32, key.length, "Generated key should have a length of 32 bytes");
    }

    @Test
    public void testGenerateKeyFromPasswordWithSalt() {
        String password = "securePassword123";
        byte[] salt = PasswordUtils.generateSalt();
        byte[] key = PasswordUtils.generateKeyFromPassword(password, salt);

        assertNotNull(key, "Generated key should not be null");
        assertEquals(32, key.length, "Generated key should have a length of 32 bytes");
    }

    @Test
    public void testGenerateSalt() {
        byte[] salt1 = PasswordUtils.generateSalt();
        byte[] salt2 = PasswordUtils.generateSalt();

        assertNotNull(salt1, "Generated salt should not be null");
        assertNotNull(salt2, "Generated salt should not be null");
        assertEquals(16, salt1.length, "Generated salt should have a length of 16 bytes");
        assertEquals(16, salt2.length, "Generated salt should have a length of 16 bytes");
        assertFalse(Arrays.equals(salt1, salt2), "Generated salts should be unique");
    }

    @Test
    public void testDefaultSaltInitialization() {
        byte[] defaultSalt = PasswordUtils.generateKeyFromPassword("test");

        assertNotNull(defaultSalt, "Default salt should not be null");
        assertEquals(32, defaultSalt.length, "Default salt should generate a key of 32 bytes");
    }
}