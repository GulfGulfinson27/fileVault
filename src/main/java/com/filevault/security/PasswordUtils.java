package com.filevault.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import com.filevault.util.LoggingUtil;

/**
 * Hilfsklasse für passwortbasierte Operationen wie Schlüsselableitung.
 */
public class PasswordUtils {

    /** Standardanzahl der Iterationen für PBKDF2 */
    private static final int ITERATIONS = 65536;
    
    /** Schlüssellänge in Bits */
    private static final int KEY_LENGTH = 256;
    
    /** Länge des Salts in Bytes */
    private static final int SALT_LENGTH = 16;

    /** Standard-Salt für die Schlüsselableitung */
    private static final byte[] DEFAULT_SALT = initDefaultSalt();

    /**
     * Initialisiert den Standard-Salt.
     * 
     * @return Der initialisierte Standard-Salt
     */
    private static byte[] initDefaultSalt() {
        LoggingUtil.logInfo("PasswordUtils", "Initializing default salt.");
        byte[] salt = new byte[SALT_LENGTH];
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            random.nextBytes(salt);
        } catch (NoSuchAlgorithmException e) {
            LoggingUtil.logError("PasswordUtils", "Error generating default salt: " + e.getMessage());
            new SecureRandom().nextBytes(salt);
        }
        LoggingUtil.logInfo("PasswordUtils", "Default salt initialized.");
        return salt;
    }

    /**
     * Generiert einen kryptografischen Schlüssel aus einem Passwort.
     * Verwendet PBKDF2 mit HMAC-SHA256 für die Schlüsselableitung.
     * 
     * @param password Das Passwort, aus dem der Schlüssel abgeleitet werden soll
     * @return Der abgeleitete Schlüssel als Byte-Array
     */
    public static byte[] generateKeyFromPassword(String password) {
        LoggingUtil.logInfo("PasswordUtils", "Generating key from password.");
        byte[] key = generateKeyFromPassword(password, DEFAULT_SALT);
        LoggingUtil.logInfo("PasswordUtils", "Key generation from password completed.");
        return key;
    }

    /**
     * Generiert einen kryptografischen Schlüssel aus einem Passwort mit einem spezifischen Salt.
     * Verwendet PBKDF2 mit HMAC-SHA256 für die Schlüsselableitung.
     * 
     * @param password Das Passwort, aus dem der Schlüssel abgeleitet werden soll
     * @param salt Der Salt, der für die Schlüsselableitung verwendet werden soll
     * @return Der abgeleitete Schlüssel als Byte-Array
     */
    public static byte[] generateKeyFromPassword(String password, byte[] salt) {
        LoggingUtil.logInfo("PasswordUtils", "Generating key from password with custom salt.");
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] key = factory.generateSecret(spec).getEncoded();
            LoggingUtil.logInfo("PasswordUtils", "Key generation with custom salt completed.");
            return key;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LoggingUtil.logError("PasswordUtils", "Error generating key from password: " + e.getMessage());
            throw new RuntimeException("Fehler beim Generieren des Schlüssels aus dem Passwort", e);
        }
    }

    /**
     * Generiert einen zufälligen Salt für die Schlüsselableitung.
     * 
     * @return Ein zufälliger Salt als Byte-Array
     */
    public static byte[] generateSalt() {
        LoggingUtil.logInfo("PasswordUtils", "Generating random salt.");
        byte[] salt = new byte[SALT_LENGTH];
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            random.nextBytes(salt);
        } catch (NoSuchAlgorithmException e) {
            LoggingUtil.logError("PasswordUtils", "Error generating random salt: " + e.getMessage());
            new SecureRandom().nextBytes(salt);
        }
        LoggingUtil.logInfo("PasswordUtils", "Random salt generation completed.");
        return salt;
    }
}