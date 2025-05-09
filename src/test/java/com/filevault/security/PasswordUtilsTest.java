package com.filevault.security;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for PasswordUtils class.
 */
public class PasswordUtilsTest {

    @Test
    void testGenerateKeyFromPassword() {
        // Test mit einfachem Passwort
        String password = "testPassword";
        byte[] key = PasswordUtils.generateKeyFromPassword(password);
        
        assertNotNull(key);
        assertEquals(32, key.length); // 256 Bit = 32 Byte
    }
    
    @Test
    void testGenerateKeyFromPasswordConsistency() {
        // Test, ob die gleiche Eingabe den gleichen Schlüssel erzeugt
        String password = "testPassword";
        byte[] key1 = PasswordUtils.generateKeyFromPassword(password);
        byte[] key2 = PasswordUtils.generateKeyFromPassword(password);
        
        assertArrayEquals(key1, key2, "Derselbe Passwort-Input sollte denselben Schlüssel erzeugen");
    }
    
    @Test
    void testGenerateKeyFromPasswordWithDifferentInputs() {
        // Test, ob unterschiedliche Eingaben unterschiedliche Schlüssel erzeugen
        String password1 = "testPassword1";
        String password2 = "testPassword2";
        
        byte[] key1 = PasswordUtils.generateKeyFromPassword(password1);
        byte[] key2 = PasswordUtils.generateKeyFromPassword(password2);
        
        assertNotEquals(Arrays.hashCode(key1), Arrays.hashCode(key2), 
                "Verschiedene Passwörter sollten verschiedene Schlüssel erzeugen");
    }
    
    @Test
    void testGenerateKeyFromPasswordWithCustomSalt() {
        // Test mit benutzerdefiniertem Salt
        String password = "testPassword";
        byte[] salt = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        
        byte[] key = PasswordUtils.generateKeyFromPassword(password, salt);
        
        assertNotNull(key);
        assertEquals(32, key.length); // 256 Bit = 32 Byte
    }
    
    @Test
    void testSaltChangesOutput() {
        // Test, ob verschiedene Salts bei gleichem Passwort unterschiedliche Schlüssel erzeugen
        String password = "testPassword";
        byte[] salt1 = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        byte[] salt2 = new byte[] { 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
        
        byte[] key1 = PasswordUtils.generateKeyFromPassword(password, salt1);
        byte[] key2 = PasswordUtils.generateKeyFromPassword(password, salt2);
        
        assertNotEquals(Arrays.hashCode(key1), Arrays.hashCode(key2), 
                "Derselbe Passwort mit unterschiedlichen Salts sollte unterschiedliche Schlüssel erzeugen");
    }
    
    @Test
    void testGenerateSalt() {
        // Test der Salt-Generierung
        byte[] salt1 = PasswordUtils.generateSalt();
        byte[] salt2 = PasswordUtils.generateSalt();
        
        assertNotNull(salt1);
        assertNotNull(salt2);
        assertEquals(16, salt1.length); // Erwartete Länge für den Salt
        assertEquals(16, salt2.length);
        
        // Die Salts sollten zufällig und damit unterschiedlich sein
        assertNotEquals(Arrays.hashCode(salt1), Arrays.hashCode(salt2), 
                "Zufällig generierte Salts sollten unterschiedlich sein");
    }
    
    @Test
    void testPasswordStrength() {
        // Test der Schlüsselqualität - einfacher Test, der überprüft, ob die Bits gut verteilt sind
        String password = "veryStrongPassword123!@#";
        byte[] key = PasswordUtils.generateKeyFromPassword(password);
        
        // Überprüfe die Entropie durch Zählen der gesetzten Bits
        int setBits = 0;
        for (byte b : key) {
            setBits += Integer.bitCount(b & 0xFF);
        }
        
        // Bei einem 256-Bit-Schlüssel erwarten wir ungefähr 128 gesetzte Bits (50%)
        // Ein guter Schlüssel sollte nahe an diesem Wert liegen
        double percentageOfSetBits = (double) setBits / (key.length * 8);
        
        // Wir erwarten, dass zwischen 40% und 60% der Bits gesetzt sind
        assertTrue(percentageOfSetBits > 0.4 && percentageOfSetBits < 0.6,
                "Der generierte Schlüssel sollte eine gute Bitverteilung haben");
    }
}