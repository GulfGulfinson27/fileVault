package com.filevault.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

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
        // Wir verwenden hier einen festen Salt für die Einfachheit,
        // idealerweise sollte dieser für jeden Benutzer separat gespeichert werden
        byte[] salt = new byte[SALT_LENGTH];
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            random.nextBytes(salt);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Fehler beim Generieren des Salts: " + e.getMessage());
            // Fallback auf einen weniger sicheren, aber noch verwendbaren Zufallsgenerator
            new SecureRandom().nextBytes(salt);
        }
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
        return generateKeyFromPassword(password, DEFAULT_SALT);
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
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("Fehler beim Generieren des Schlüssels aus dem Passwort: " + e.getMessage());
            throw new RuntimeException("Fehler beim Generieren des Schlüssels aus dem Passwort", e);
        }
    }

    /**
     * Generiert einen zufälligen Salt für die Schlüsselableitung.
     * 
     * @return Ein zufälliger Salt als Byte-Array
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            random.nextBytes(salt);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Fehler beim Generieren des Salts: " + e.getMessage());
            // Fallback auf einen weniger sicheren, aber noch verwendbaren Zufallsgenerator
            new SecureRandom().nextBytes(salt);
        }
        return salt;
    }
} 