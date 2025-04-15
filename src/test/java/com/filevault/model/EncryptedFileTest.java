package com.filevault.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für die EncryptedFile-Klasse.
 * Diese Klasse testet die Funktionalität der verschlüsselten Dateien im Tresor.
 */
public class EncryptedFileTest {
    
    private EncryptedFile testFile;
    private LocalDateTime testTime;
    
    /**
     * Initialisiert die Testumgebung vor jedem Test.
     * Erstellt eine neue verschlüsselte Datei mit Testdaten.
     */
    @BeforeEach
    public void setUp() {
        testTime = LocalDateTime.now();
        testFile = new EncryptedFile(
            1,                      // id
            2,                      // folderId
            "test.txt",             // originalName
            "/path/to/encrypted",   // encryptedPath
            1024,                   // sizeBytes
            "text/plain",           // mimeType
            testTime,               // createdAt
            testTime                // lastAccess
        );
    }
    
    /**
     * Testet die Getter-Methoden der EncryptedFile-Klasse.
     * Überprüft, ob alle Werte korrekt gesetzt und abgerufen werden können.
     */
    @Test
    public void testGetters() {
        assertEquals(1, testFile.getId());
        assertEquals(2, testFile.getFolderId());
        assertEquals("test.txt", testFile.getOriginalName());
        assertEquals("/path/to/encrypted", testFile.getEncryptedPath());
        assertEquals(1024, testFile.getSizeBytes());
        assertEquals("text/plain", testFile.getMimeType());
        assertEquals(testTime, testFile.getCreatedAt());
        assertEquals(testTime, testFile.getLastAccess());
    }
    
    /**
     * Testet die Setter-Methoden der EncryptedFile-Klasse.
     * Überprüft, ob Werte korrekt geändert werden können.
     */
    @Test
    public void testSetters() {
        testFile.setFolderId(3);
        assertEquals(3, testFile.getFolderId());
        
        testFile.setOriginalName("neuerName.txt");
        assertEquals("neuerName.txt", testFile.getOriginalName());
        
        testFile.setMimeType("application/pdf");
        assertEquals("application/pdf", testFile.getMimeType());
        
        LocalDateTime newTime = LocalDateTime.now();
        testFile.setLastAccess(newTime);
        assertEquals(newTime, testFile.getLastAccess());
    }
    
    /**
     * Testet die Formatierung der Dateigröße.
     * Überprüft verschiedene Größenangaben in Bytes, KB, MB und GB.
     */
    @Test
    public void testFormattedSize() {
        // Test für Bytes
        EncryptedFile smallFile = new EncryptedFile(2, 1, "small.txt", "/path", 500, "text/plain", testTime, testTime);
        assertEquals("500 B", smallFile.getFormattedSize());
        
        // Test für KB
        EncryptedFile kbFile = new EncryptedFile(3, 1, "kb.txt", "/path", 2048, "text/plain", testTime, testTime);
        assertEquals("2.0 KB", kbFile.getFormattedSize());
        
        // Test für MB
        EncryptedFile mbFile = new EncryptedFile(4, 1, "mb.txt", "/path", 2 * 1024 * 1024, "text/plain", testTime, testTime);
        assertEquals("2.0 MB", mbFile.getFormattedSize());
        
        // Test für GB
        EncryptedFile gbFile = new EncryptedFile(5, 1, "gb.txt", "/path", 3L * 1024 * 1024 * 1024, "text/plain", testTime, testTime);
        assertEquals("3.0 GB", gbFile.getFormattedSize());
    }
    
    /**
     * Testet die Extraktion der Dateierweiterung.
     * Überprüft verschiedene Dateinamen mit und ohne Erweiterung.
     */
    @Test
    public void testFileExtension() {
        assertEquals("txt", testFile.getFileExtension());
        
        EncryptedFile noExtension = new EncryptedFile(6, 1, "noextension", "/path", 1024, "text/plain", testTime, testTime);
        assertEquals("", noExtension.getFileExtension());
        
        EncryptedFile multipleDots = new EncryptedFile(7, 1, "test.file.txt", "/path", 1024, "text/plain", testTime, testTime);
        assertEquals("txt", multipleDots.getFileExtension());
    }
    
    /**
     * Testet die equals- und hashCode-Methoden.
     * Überprüft die korrekte Implementierung der Objektgleichheit.
     */
    @Test
    public void testEqualsAndHashCode() {
        EncryptedFile sameId = new EncryptedFile(1, 3, "different.txt", "/other/path", 2048, "text/html", testTime, testTime);
        EncryptedFile differentId = new EncryptedFile(2, 2, "test.txt", "/path/to/encrypted", 1024, "text/plain", testTime, testTime);
        
        assertTrue(testFile.equals(sameId));
        assertFalse(testFile.equals(differentId));
        assertEquals(testFile.hashCode(), sameId.hashCode());
        assertNotEquals(testFile.hashCode(), differentId.hashCode());
    }
    
    /**
     * Testet die toString-Methode.
     * Überprüft, ob der Originalname korrekt zurückgegeben wird.
     */
    @Test
    public void testToString() {
        assertEquals("test.txt", testFile.toString());
    }
} 