package com.filevault.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für die EncryptedFile-Klasse.
 * Diese Klasse testet die Funktionalität der verschlüsselten Dateien im Tresor.
 */
@DisplayName("Tests für EncryptedFile")
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
    
    @Nested
    @DisplayName("Getter und Setter Tests")
    class GetterSetterTests {
        /**
         * Testet die Getter-Methoden der EncryptedFile-Klasse.
         * Überprüft, ob alle Werte korrekt gesetzt und abgerufen werden können.
         */
        @Test
        @DisplayName("Getter sollten korrekte Werte zurückgeben")
        public void testGetters() {
            assertEquals(1, testFile.getId(), "getId() sollte die korrekte ID zurückgeben");
            assertEquals(2, testFile.getFolderId(), "getFolderId() sollte die korrekte Ordner-ID zurückgeben");
            assertEquals("test.txt", testFile.getOriginalName(), "getOriginalName() sollte den korrekten Dateinamen zurückgeben");
            assertEquals("/path/to/encrypted", testFile.getEncryptedPath(), "getEncryptedPath() sollte den korrekten Pfad zurückgeben");
            assertEquals(1024, testFile.getSizeBytes(), "getSizeBytes() sollte die korrekte Größe zurückgeben");
            assertEquals("text/plain", testFile.getMimeType(), "getMimeType() sollte den korrekten MIME-Typ zurückgeben");
            assertEquals(testTime, testFile.getCreatedAt(), "getCreatedAt() sollte die korrekte Erstellungszeit zurückgeben");
            assertEquals(testTime, testFile.getLastAccess(), "getLastAccess() sollte die korrekte Zugriffszeit zurückgeben");
        }
        
        /**
         * Testet die Setter-Methoden der EncryptedFile-Klasse.
         * Überprüft, ob Werte korrekt geändert werden können.
         */
        @Test
        @DisplayName("Setter sollten Werte korrekt ändern")
        public void testSetters() {
            testFile.setFolderId(3);
            assertEquals(3, testFile.getFolderId(), "setFolderId() sollte die Ordner-ID korrekt ändern");
            
            testFile.setOriginalName("neuerName.txt");
            assertEquals("neuerName.txt", testFile.getOriginalName(), "setOriginalName() sollte den Dateinamen korrekt ändern");
            
            testFile.setMimeType("application/pdf");
            assertEquals("application/pdf", testFile.getMimeType(), "setMimeType() sollte den MIME-Typ korrekt ändern");
            
            LocalDateTime newTime = LocalDateTime.now().plusHours(1);
            testFile.setLastAccess(newTime);
            assertEquals(newTime, testFile.getLastAccess(), "setLastAccess() sollte die Zugriffszeit korrekt ändern");
        }
        
        /**
         * Testet die Setter mit Null-Werten.
         */
        @ParameterizedTest
        @DisplayName("Setter sollten mit Null-Werten umgehen können")
        @NullAndEmptySource
        public void testSettersWithNullValues(String value) {
            testFile.setOriginalName(value);
            assertEquals(value, testFile.getOriginalName(), "setOriginalName() sollte Null-Werte akzeptieren");
            
            testFile.setMimeType(value);
            assertEquals(value, testFile.getMimeType(), "setMimeType() sollte Null-Werte akzeptieren");
            
            testFile.setLastAccess(null);
            assertNull(testFile.getLastAccess(), "setLastAccess() sollte NULL akzeptieren");
        }
    }
    
    @Nested
    @DisplayName("Formatierung Tests")
    class FormattingTests {
        /**
         * Testet die Formatierung der Dateigröße mit verschiedenen Größen.
         */
        @ParameterizedTest
        @DisplayName("Dateigröße sollte korrekt formatiert werden")
        @CsvSource({
            "500, '500 B'",
            "1024, '1.0 KB'",
            "1536, '1.5 KB'",
            "2097152, '2.0 MB'",
            "3221225472, '3.0 GB'"
        })
        public void testFormattedSize(long size, String expected) {
            EncryptedFile file = new EncryptedFile(1, 1, "test.txt", "/path", size, "text/plain", testTime, testTime);
            assertEquals(expected, file.getFormattedSize(), "Die Formatierung der Dateigröße sollte korrekt sein");
        }
    }
    
    @Nested
    @DisplayName("Dateierweiterung Tests")
    class FileExtensionTests {
        /**
         * Testet die Extraktion der Dateierweiterung mit verschiedenen Dateinamen.
         */
        @ParameterizedTest
        @DisplayName("Dateierweiterung sollte korrekt extrahiert werden")
        @CsvSource({
            "test.txt, txt",
            "noextension, ''",
            "multiple.dots.pdf, pdf",
            ".hiddenfile, hiddenfile",
            "file.with.many.dots.zip, zip"
        })
        public void testFileExtension(String filename, String expected) {
            EncryptedFile file = new EncryptedFile(1, 1, filename, "/path", 1024, "text/plain", testTime, testTime);
            assertEquals(expected, file.getFileExtension(), "Die Dateierweiterung sollte korrekt extrahiert werden");
        }
    }
    
    @Nested
    @DisplayName("Objektvergleich Tests")
    class ObjectComparisonTests {
        /**
         * Testet die equals- und hashCode-Methoden.
         * Überprüft die korrekte Implementierung der Objektgleichheit.
         */
        @Test
        @DisplayName("equals() und hashCode() sollten korrekt implementiert sein")
        public void testEqualsAndHashCode() {
            EncryptedFile sameId = new EncryptedFile(1, 3, "different.txt", "/other/path", 2048, "text/html", testTime, testTime);
            EncryptedFile differentId = new EncryptedFile(2, 2, "test.txt", "/path/to/encrypted", 1024, "text/plain", testTime, testTime);
            
            // Gleichheit basiert nur auf der ID
            assertTrue(testFile.equals(testFile), "Ein Objekt sollte sich selbst gleichen");
            assertTrue(testFile.equals(sameId), "Dateien mit gleicher ID sollten gleich sein");
            assertFalse(testFile.equals(differentId), "Dateien mit unterschiedlicher ID sollten ungleich sein");
            assertFalse(testFile.equals(null), "equals() sollte null-sicher sein");
            assertFalse(testFile.equals("nicht eine Datei"), "equals() sollte typsicher sein");
            
            // HashCode-Konsistenz mit equals
            assertEquals(testFile.hashCode(), sameId.hashCode(), "hashCode() sollte für gleiche Objekte gleich sein");
            assertNotEquals(testFile.hashCode(), differentId.hashCode(), "hashCode() sollte für ungleiche Objekte unterschiedlich sein");
        }
    }
    
    @Test
    @DisplayName("toString() sollte den Originalnamen zurückgeben")
    public void testToString() {
        assertEquals("test.txt", testFile.toString(), "toString() sollte den Originalnamen zurückgeben");
    }
    
    @Test
    @DisplayName("Konstruktor sollte Objekte korrekt initialisieren")
    public void testConstructor() {
        EncryptedFile file = new EncryptedFile(10, 20, "file.txt", "/path", 2048, "text/plain", testTime, null);
        assertEquals(10, file.getId());
        assertEquals(20, file.getFolderId());
        assertEquals("file.txt", file.getOriginalName());
        assertEquals("/path", file.getEncryptedPath());
        assertEquals(2048, file.getSizeBytes());
        assertEquals("text/plain", file.getMimeType());
        assertEquals(testTime, file.getCreatedAt());
        assertNull(file.getLastAccess());
    }
} 