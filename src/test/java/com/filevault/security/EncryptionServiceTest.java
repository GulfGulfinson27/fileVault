package com.filevault.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.filevault.model.UserManager;
import com.filevault.storage.DatabaseManager;

/**
 * Testklasse für den EncryptionService.
 * Testet die Verschlüsselung und Entschlüsselung von Dateien.
 */
public class EncryptionServiceTest {
    
    private static final String TEST_PASSWORD = "TestPasswort123!";
    private static final String TEST_CONTENT = "Dies ist ein Test für die Verschlüsselung.";
    
    private UserManager userManager;
    private EncryptionService encryptionService;
    private File testDirectory;
    private File plainFile;
    private File encryptedFile;
    private File decryptedFile;
    private String testDbPath;
    
    @TempDir
    File tempDir;
    
    private byte[] testMasterKey;
    
    /**
     * Initialisiert die Testumgebung vor jedem Test.
     * Erstellt temporäre Dateien zum Testen und initialisiert den EncryptionService.
     */
    @BeforeEach
    public void setUp() throws Exception {
        // Testdatenbank initialisieren
        // Stelle sicher, dass alte Test-Datenbank gelöscht wird
        testDbPath = Paths.get(System.getProperty("user.home"), ".filevault", "test_vault.db").toString();
        File dbFile = new File(testDbPath);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        
        // Initialisiere die Datenbank explizit für Tests
        DatabaseManager.closeConnections(); // Schließe alle bestehenden Verbindungen
        DatabaseManager.initDatabase(true);
        
        // Überprüfe, ob die Tabellen erstellt wurden
        try (Connection conn = DatabaseManager.getConnection()) {
            // Wenn wir hier eine Verbindung bekommen, ohne eine Exception zu werfen,
            // sollte die Datenbank initialisiert worden sein
        } catch (SQLException e) {
            fail("Fehler beim Initialisieren der Testdatenbank: " + e.getMessage());
        }
        
        // Testverzeichnis erstellen
        testDirectory = new File("temp_encryption_test");
        if (!testDirectory.exists()) {
            testDirectory.mkdir();
        }
        
        // Testdateien erstellen
        plainFile = new File(testDirectory, "plain.txt");
        encryptedFile = new File(testDirectory, "encrypted.bin");
        decryptedFile = new File(testDirectory, "decrypted.txt");
        
        // Testinhalt in plainFile schreiben
        Files.write(plainFile.toPath(), TEST_CONTENT.getBytes(StandardCharsets.UTF_8));
        
        // UserManager initialisieren und Benutzer erstellen/authentifizieren
        userManager = UserManager.getInstance();
        if (userManager.userExists()) {
            userManager.deleteUser();
        }
        
        boolean userCreated = userManager.createUser(TEST_PASSWORD);
        if (!userCreated) {
            fail("Fehler beim Erstellen des Testbenutzers");
        }
        
        boolean userAuthenticated = userManager.authenticate(TEST_PASSWORD);
        if (!userAuthenticated) {
            fail("Fehler beim Authentifizieren des Testbenutzers");
        }
        
        // EncryptionService holen
        encryptionService = EncryptionService.getInstance();
        
        // Erzeuge einen Test-Masterschlüssel
        testMasterKey = new byte[32]; // 256 bit AES key
        new Random().nextBytes(testMasterKey);
        
        // Setze den Masterschlüssel über Reflection im UserManager
        setMasterKeyViaReflection(userManager, testMasterKey);
    }
    
    private void setMasterKeyViaReflection(UserManager userManager, byte[] masterKey) throws Exception {
        Field masterKeyField = UserManager.class.getDeclaredField("masterKey");
        masterKeyField.setAccessible(true);
        masterKeyField.set(userManager, masterKey);
    }
    
    /**
     * Bereinigt die Testumgebung nach jedem Test.
     * Löscht alle temporären Testdateien.
     */
    @AfterEach
    public void tearDown() throws Exception {
        // Benutzer abmelden und löschen
        if (userManager != null) {
            userManager.logout();
            userManager.deleteUser();
        }
        
        // Datenbank-Verbindungen schließen
        DatabaseManager.closeConnections();
        
        // Testdatenbank löschen
        try {
            File dbFile = new File(testDbPath);
            if (dbFile.exists()) {
                dbFile.delete();
            }
        } catch (Exception e) {
            System.err.println("Warnung: Fehler beim Löschen der Testdatenbank: " + e.getMessage());
        }
        
        // Testdateien löschen
        if (plainFile != null && plainFile.exists()) plainFile.delete();
        if (encryptedFile != null && encryptedFile.exists()) encryptedFile.delete();
        if (decryptedFile != null && decryptedFile.exists()) decryptedFile.delete();
        
        // Testverzeichnis löschen
        if (testDirectory != null && testDirectory.exists()) {
            deleteDirectory(testDirectory);
        }
    }
    
    /**
     * Hilfsmethode zum rekursiven Löschen eines Verzeichnisses.
     * 
     * @param directory Das zu löschende Verzeichnis
     */
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
    
    /**
     * Testet die Singleton-Implementierung des EncryptionService.
     * Überprüft, ob immer dieselbe Instanz zurückgegeben wird.
     */
    @Test
    public void testSingleton() {
        EncryptionService instance1 = EncryptionService.getInstance();
        EncryptionService instance2 = EncryptionService.getInstance();
        assertSame(instance1, instance2, "EncryptionService sollte ein Singleton sein");
    }
    
    /**
     * Testet die Verschlüsselung einer Datei.
     * Überprüft, ob die verschlüsselte Datei erstellt wurde und sich vom Original unterscheidet.
     */
    @Test
    public void testEncryptFile() throws Exception {
        // Führe die Verschlüsselung durch
        boolean result = encryptionService.encryptFile(plainFile, encryptedFile);
        
        // Überprüfe das Ergebnis
        assertTrue(result, "Die Verschlüsselung sollte erfolgreich sein");
        assertTrue(encryptedFile.exists(), "Die verschlüsselte Datei sollte existieren");
        assertTrue(encryptedFile.length() > 0, "Die verschlüsselte Datei sollte Inhalt haben");
        
        // Überprüfe, dass die verschlüsselte Datei nicht den gleichen Inhalt wie die Originaldatei hat
        byte[] originalContent = Files.readAllBytes(plainFile.toPath());
        byte[] encryptedContent = Files.readAllBytes(encryptedFile.toPath());
        assertFalse(Arrays.equals(originalContent, encryptedContent), 
                "Der verschlüsselte Inhalt sollte sich vom Original unterscheiden");
    }
    
    /**
     * Testet die Entschlüsselung einer Datei.
     * Überprüft, ob nach Verschlüsselung und Entschlüsselung der ursprüngliche Inhalt erhalten bleibt.
     */
    @Test
    public void testDecryptFile() throws Exception {
        // Verschlüssele eine Datei
        boolean encryptResult = encryptionService.encryptFile(plainFile, encryptedFile);
        assertTrue(encryptResult, "Die Verschlüsselung sollte erfolgreich sein");
        
        // Entschlüssele die Datei
        boolean decryptResult = encryptionService.decryptFile(encryptedFile, decryptedFile);
        
        // Überprüfe das Ergebnis
        assertTrue(decryptResult, "Die Entschlüsselung sollte erfolgreich sein");
        assertTrue(decryptedFile.exists(), "Die entschlüsselte Datei sollte existieren");
        
        // Überprüfe, dass der entschlüsselte Inhalt dem Original entspricht
        String decryptedContent = new String(Files.readAllBytes(decryptedFile.toPath()), StandardCharsets.UTF_8);
        assertEquals(TEST_CONTENT, decryptedContent, "Der entschlüsselte Inhalt sollte dem Original entsprechen");
    }
    
    /**
     * Testet die Verschlüsselung und Entschlüsselung einer größeren Datei mit Binärdaten.
     */
    @Test
    public void testEncryptDecryptLargeBinaryFile() throws Exception {
        // Erstelle eine größere Binärdatei
        File largeBinaryFile = new File(testDirectory, "large.bin");
        File encryptedLargeFile = new File(testDirectory, "large.encrypted");
        File decryptedLargeFile = new File(testDirectory, "large.decrypted");
        
        // Generiere zufällige Binärdaten (1 MB)
        byte[] randomData = new byte[1024 * 1024];
        new Random().nextBytes(randomData);
        Files.write(largeBinaryFile.toPath(), randomData);
        
        // Verschlüsselung
        boolean encryptResult = encryptionService.encryptFile(largeBinaryFile, encryptedLargeFile);
        assertTrue(encryptResult, "Die Verschlüsselung der großen Datei sollte erfolgreich sein");
        
        // Entschlüsselung
        boolean decryptResult = encryptionService.decryptFile(encryptedLargeFile, decryptedLargeFile);
        assertTrue(decryptResult, "Die Entschlüsselung der großen Datei sollte erfolgreich sein");
        
        // Überprüfe den Inhalt
        byte[] decryptedData = Files.readAllBytes(decryptedLargeFile.toPath());
        assertArrayEquals(randomData, decryptedData, "Die Binärdaten sollten nach Ver- und Entschlüsselung identisch sein");
        
        // Dateien aufräumen
        largeBinaryFile.delete();
        encryptedLargeFile.delete();
        decryptedLargeFile.delete();
    }
    
    /**
     * Testet das Verhalten bei fehlender Authentifizierung.
     * Der EncryptionService sollte eine Exception werfen, wenn kein Master-Key verfügbar ist.
     */
    @Test
    public void testEncryptionWithoutAuthentication() throws Exception {
        // Benutzer abmelden, um den Master-Key zu entfernen
        userManager.logout();
        
        // Versuche, ohne Authentifizierung zu verschlüsseln
        Exception encryptException = assertThrows(IllegalStateException.class, () -> {
            encryptionService.encryptFile(plainFile, encryptedFile);
        });
        assertTrue(encryptException.getMessage().contains("Kein Master-Schlüssel verfügbar"), 
                "Die Exception sollte auf einen fehlenden Master-Schlüssel hinweisen");
        
        // Versuche, ohne Authentifizierung zu entschlüsseln
        Exception decryptException = assertThrows(IllegalStateException.class, () -> {
            encryptionService.decryptFile(encryptedFile, decryptedFile);
        });
        assertTrue(decryptException.getMessage().contains("Kein Master-Schlüssel verfügbar"), 
                "Die Exception sollte auf einen fehlenden Master-Schlüssel hinweisen");
    }
    
    /**
     * Testet das Verhalten bei beschädigten oder falsch formatierten verschlüsselten Dateien.
     */
    @Test
    public void testDecryptInvalidFile() throws Exception {
        // Erzeuge eine ungültige "verschlüsselte" Datei
        File invalidFile = new File(testDirectory, "invalid.enc");
        Files.write(invalidFile.toPath(), "Dies ist keine gültige verschlüsselte Datei.".getBytes(StandardCharsets.UTF_8));
        
        // Versuche, die ungültige Datei zu entschlüsseln
        Exception exception = assertThrows(Exception.class, () -> {
            encryptionService.decryptFile(invalidFile, decryptedFile);
        });
        
        // Die genaue Exception kann variieren, aber es sollte ein Fehler auftreten
        assertNotNull(exception, "Es sollte eine Exception geworfen werden bei ungültigen Dateien");
        
        // Aufräumen
        invalidFile.delete();
    }
    
    @Test
    void testEncryptAndDecryptFile() throws Exception {
        // Erstelle eine Testdatei
        String testContent = "Dies ist ein Test mit einigen speziellen Zeichen: äöüßÄÖÜ!@#$%^&*()";
        File originalFile = new File(tempDir, "original.txt");
        Files.write(originalFile.toPath(), testContent.getBytes(StandardCharsets.UTF_8));
        
        // Erstelle Dateien für die Verschlüsselung und Entschlüsselung
        File encryptedFile = new File(tempDir, "encrypted.bin");
        File decryptedFile = new File(tempDir, "decrypted.txt");
        
        // Verschlüssele die Datei
        boolean encryptResult = encryptionService.encryptFile(originalFile, encryptedFile);
        assertTrue(encryptResult);
        assertTrue(encryptedFile.exists());
        
        // Stelle sicher, dass die verschlüsselte Datei anders ist als die Originaldatei
        byte[] originalBytes = Files.readAllBytes(originalFile.toPath());
        byte[] encryptedBytes = Files.readAllBytes(encryptedFile.toPath());
        assertFalse(Arrays.equals(originalBytes, encryptedBytes));
        
        // Entschlüssele die Datei
        boolean decryptResult = encryptionService.decryptFile(encryptedFile, decryptedFile);
        assertTrue(decryptResult);
        assertTrue(decryptedFile.exists());
        
        // Überprüfe, ob der entschlüsselte Inhalt mit dem Original übereinstimmt
        String decryptedContent = new String(Files.readAllBytes(decryptedFile.toPath()), StandardCharsets.UTF_8);
        assertEquals(testContent, decryptedContent);
    }
    
    @Test
    void testEncryptAndDecryptLargeFile() throws Exception {
        // Erstelle eine größere Testdatei (1 MB)
        File largeFile = new File(tempDir, "large.bin");
        byte[] randomData = new byte[1024 * 1024]; // 1 MB
        new Random().nextBytes(randomData);
        try (FileOutputStream fos = new FileOutputStream(largeFile)) {
            fos.write(randomData);
        }
        
        // Erstelle Dateien für die Verschlüsselung und Entschlüsselung
        File encryptedLargeFile = new File(tempDir, "large_encrypted.bin");
        File decryptedLargeFile = new File(tempDir, "large_decrypted.bin");
        
        // Verschlüssele die Datei
        boolean encryptResult = encryptionService.encryptFile(largeFile, encryptedLargeFile);
        assertTrue(encryptResult);
        
        // Entschlüssele die Datei
        boolean decryptResult = encryptionService.decryptFile(encryptedLargeFile, decryptedLargeFile);
        assertTrue(decryptResult);
        
        // Überprüfe, ob der entschlüsselte Inhalt mit dem Original übereinstimmt
        byte[] decryptedData = new byte[randomData.length];
        try (FileInputStream fis = new FileInputStream(decryptedLargeFile)) {
            assertEquals(randomData.length, fis.read(decryptedData));
        }
        
        assertArrayEquals(randomData, decryptedData);
    }
    
    @Test
    void testEncryptWithoutMasterKey() throws Exception {
        // Lösche den Masterschlüssel über Reflection
        setMasterKeyViaReflection(UserManager.getInstance(), null);
        
        // Erstelle eine Testdatei
        File testFile = new File(tempDir, "test.txt");
        Files.write(testFile.toPath(), "Test".getBytes());
        
        File encryptedFile = new File(tempDir, "encrypted.bin");
        
        // Versuch zu verschlüsseln ohne Masterschlüssel sollte fehlschlagen
        assertThrows(IllegalStateException.class, () -> {
            encryptionService.encryptFile(testFile, encryptedFile);
        });
        
        // Stelle den Masterschlüssel wieder her
        setMasterKeyViaReflection(UserManager.getInstance(), testMasterKey);
    }
    
    @Test
    void testDecryptWithoutMasterKey() throws Exception {
        // Erstelle eine Testdatei und verschlüssele sie mit dem vorhandenen Schlüssel
        File testFile = new File(tempDir, "test.txt");
        Files.write(testFile.toPath(), "Test".getBytes());
        
        File encryptedFile = new File(tempDir, "encrypted.bin");
        encryptionService.encryptFile(testFile, encryptedFile);
        
        // Lösche den Masterschlüssel über Reflection
        setMasterKeyViaReflection(UserManager.getInstance(), null);
        
        File decryptedFile = new File(tempDir, "decrypted.txt");
        
        // Versuch zu entschlüsseln ohne Masterschlüssel sollte fehlschlagen
        assertThrows(IllegalStateException.class, () -> {
            encryptionService.decryptFile(encryptedFile, decryptedFile);
        });
        
        // Stelle den Masterschlüssel wieder her
        setMasterKeyViaReflection(UserManager.getInstance(), testMasterKey);
    }
    
    @Test
    void testDecryptCorruptedFile() throws Exception {
        // Erstelle eine Testdatei
        File testFile = new File(tempDir, "test.txt");
        Files.write(testFile.toPath(), "Test".getBytes());
        
        // Erstelle eine absichtlich beschädigte "verschlüsselte" Datei
        File corruptedFile = new File(tempDir, "corrupted.bin");
        byte[] corruptedData = new byte[20]; // Zu kurz für einen gültigen IV
        new Random().nextBytes(corruptedData);
        Files.write(corruptedFile.toPath(), corruptedData);
        
        File decryptedFile = new File(tempDir, "decrypted.txt");
        
        // Versuch, die beschädigte Datei zu entschlüsseln, sollte fehlschlagen
        assertThrows(IOException.class, () -> {
            encryptionService.decryptFile(corruptedFile, decryptedFile);
        });
    }
}