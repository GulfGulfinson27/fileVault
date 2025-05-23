package com.filevault.storage;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.filevault.model.EncryptedFile;
import com.filevault.model.UserManager;
import com.filevault.model.VirtualFolder;

/**
 * Testklasse für die FileStorage-Klasse.
 * Diese Klasse testet die Funktionalität zum Importieren, Exportieren, Umbenennen und Löschen von Dateien.
 */
class FileStorageTest {

    /** Temporäres Verzeichnis für Testdateien */
    @TempDir
    Path tempDir;
    
    /** Die zu testende FileStorage-Instanz */
    private FileStorage fileStorage;
    
    /** Testordner für die Dateien */
    private VirtualFolder testFolder;
    
    /** Testdatei für den Import */
    private File testFile;
    
    /** Test-Masterschlüssel für die Verschlüsselung */
    private byte[] testMasterKey;
    
    /**
     * Initialisiert die Testumgebung vor jedem Test.
     * Erstellt temporäre Testdateien, initialisiert die Datenbank und erstellt einen Testordner.
     */
    @BeforeEach
    void setUp() throws Exception {
        // Erzeuge temporäre Testdateien
        testFile = tempDir.resolve("testfile.txt").toFile();
        Files.writeString(testFile.toPath(), "Dies ist ein Test-Inhalt für die Datei.");
        
        // Erzeuge einen Test-Masterschlüssel
        testMasterKey = new byte[32]; // 256 bit AES key
        for (int i = 0; i < testMasterKey.length; i++) {
            testMasterKey[i] = (byte) i;
        }
        
        // Setze den Masterschlüssel über Reflection im UserManager
        UserManager userManager = UserManager.getInstance();
        setMasterKeyViaReflection(userManager, testMasterKey);
        
        // Initialisiere DatabaseManager mit einer temporären Datenbank
        String testDbPath = tempDir.resolve("test.db").toString();
        Field dbPathField = DatabaseManager.class.getDeclaredField("currentDbPath");
        dbPathField.setAccessible(true);
        dbPathField.set(null, testDbPath);
        
        // Initialisiere die Datenbank
        DatabaseManager.initDatabase(true);
        
        // Initialisiere FileStorage
        fileStorage = FileStorage.getInstance();
        
        // Erstelle einen Testordner für die Dateien
        createTestFolder();
    }
    
    /**
     * Hilfsmethode zum Erstellen eines Testordners direkt in der Datenbank.
     * 
     * @throws Exception Falls ein Fehler beim Erstellen des Ordners auftritt
     */
    private void createTestFolder() throws Exception {
        testFolder = new VirtualFolder(1, "TestFolder", "Test-Ordner für die Tests", null);
        
        // SQL zum Einfügen eines Ordners in die Datenbank ausführen
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(
                 "INSERT INTO folders (id, name, description, parent_id) VALUES (?, ?, ?, ?)")) {
            stmt.setInt(1, testFolder.getId());
            stmt.setString(2, testFolder.getName());
            stmt.setString(3, testFolder.getDescription());
            stmt.setObject(4, testFolder.getParentId());
            stmt.executeUpdate();
        }
    }
    
    /**
     * Bereinigt die Testumgebung nach jedem Test.
     * Schließt Datenbankverbindungen und löscht die Testdatenbank.
     */
    @AfterEach
    void tearDown() throws Exception {
        // Bereinige Datenbank und Dateien
        DatabaseManager.closeConnections();
        DatabaseManager.deleteTestDatabase();
    }
    
    /**
     * Hilfsmethode zum Setzen des Masterschlüssels über Reflection.
     * 
     * @param userManager Die UserManager-Instanz
     * @param masterKey Der zu setzende Masterschlüssel
     * @throws Exception Falls ein Fehler beim Setzen des Schlüssels auftritt
     */
    private void setMasterKeyViaReflection(UserManager userManager, byte[] masterKey) throws Exception {
        Field masterKeyField = UserManager.class.getDeclaredField("masterKey");
        masterKeyField.setAccessible(true);
        masterKeyField.set(userManager, masterKey);
    }
    
    /**
     * Testet, dass FileStorage als Singleton implementiert ist.
     * Überprüft, ob mehrere Aufrufe von getInstance() die gleiche Instanz zurückgeben.
     */
    @Test
    void testSingletonInstance() {
        FileStorage instance1 = FileStorage.getInstance();
        FileStorage instance2 = FileStorage.getInstance();
        
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertEquals(instance1, instance2);
    }
    
    /**
     * Testet das Importieren und Abrufen einer Datei.
     * Überprüft, ob eine importierte Datei korrekt in der Datenbank gespeichert und abgerufen werden kann.
     */
    @Test
    void testImportAndGetFile() throws Exception {
        // Importiere eine Testdatei
        EncryptedFile importedFile = fileStorage.importFile(testFile, testFolder);
        
        // Überprüfe, ob die importierte Datei korrekt erstellt wurde
        assertNotNull(importedFile);
        assertEquals(testFile.getName(), importedFile.getOriginalName());
        assertEquals(testFile.length(), importedFile.getSizeBytes());
        assertNotNull(importedFile.getEncryptedPath());
        assertTrue(new File(importedFile.getEncryptedPath()).exists());
        
        // Hole die Datei aus der Datenbank
        EncryptedFile retrievedFile = fileStorage.getFileById(importedFile.getId());
        assertNotNull(retrievedFile);
        assertEquals(importedFile.getId(), retrievedFile.getId());
        assertEquals(importedFile.getOriginalName(), retrievedFile.getOriginalName());
        assertEquals(importedFile.getSizeBytes(), retrievedFile.getSizeBytes());
    }
    
    /**
     * Testet das Abrufen aller Dateien in einem Ordner.
     * Überprüft, ob alle importierten Dateien in einem Ordner korrekt abgerufen werden können.
     */
    @Test
    void testGetFilesInFolder() throws Exception {
        // Importiere einige Testdateien
        File testFile1 = tempDir.resolve("testfile1.txt").toFile();
        Files.writeString(testFile1.toPath(), "Testdatei 1");
        
        File testFile2 = tempDir.resolve("testfile2.txt").toFile();
        Files.writeString(testFile2.toPath(), "Testdatei 2");
        
        EncryptedFile importedFile1 = fileStorage.importFile(testFile1, testFolder);
        EncryptedFile importedFile2 = fileStorage.importFile(testFile2, testFolder);
        
        // Hole alle Dateien im Ordner
        List<EncryptedFile> files = fileStorage.getFilesInFolder(testFolder);
        
        assertNotNull(files);
        assertEquals(2, files.size());
        assertTrue(files.stream().anyMatch(f -> f.getId() == importedFile1.getId()));
        assertTrue(files.stream().anyMatch(f -> f.getId() == importedFile2.getId()));
    }
    
    /**
     * Testet das Umbenennen einer Datei.
     * Überprüft, ob eine Datei erfolgreich umbenannt werden kann.
     */
    @Test
    void testRenameFile() throws Exception {
        // Importiere eine Testdatei
        EncryptedFile importedFile = fileStorage.importFile(testFile, testFolder);
        
        // Benenne die Datei um
        String newName = "renamed_testfile.txt";
        boolean renamed = fileStorage.renameFile(importedFile, newName);
        
        assertTrue(renamed);
        
        // Überprüfe, ob die Datei umbenannt wurde
        EncryptedFile retrievedFile = fileStorage.getFileById(importedFile.getId());
        assertEquals(newName, retrievedFile.getOriginalName());
    }
    
    /**
     * Testet das Löschen einer Datei.
     * Überprüft, ob eine Datei erfolgreich aus der Datenbank und dem Dateisystem gelöscht werden kann.
     */
    @Test
    void testDeleteFile() throws Exception {
        // Importiere eine Testdatei
        EncryptedFile importedFile = fileStorage.importFile(testFile, testFolder);
        
        // Lösche die Datei
        boolean deleted = fileStorage.deleteFile(importedFile);
        
        assertTrue(deleted);
        
        // Überprüfe, ob die Datei gelöscht wurde
        List<EncryptedFile> files = fileStorage.getFilesInFolder(testFolder);
        assertTrue(files.isEmpty());
        
        // Die verschlüsselte Datei sollte ebenfalls gelöscht sein
        File encryptedFile = new File(importedFile.getEncryptedPath());
        assertFalse(encryptedFile.exists());
    }
    
    /**
     * Testet das Exportieren einer Datei.
     * Überprüft, ob eine importierte Datei erfolgreich exportiert werden kann und der Inhalt korrekt ist.
     */
    @Test
    void testExportFile() throws Exception {
        // Importiere eine Testdatei
        EncryptedFile importedFile = fileStorage.importFile(testFile, testFolder);
        
        // Exportiere die Datei
        File exportedFile = tempDir.resolve("exported_file.txt").toFile();
        boolean exported = fileStorage.exportFile(importedFile, exportedFile);
        
        assertTrue(exported);
        assertTrue(exportedFile.exists());
        
        // Überprüfe den Inhalt
        String originalContent = Files.readString(testFile.toPath());
        String exportedContent = Files.readString(exportedFile.toPath());
        
        assertEquals(originalContent, exportedContent);
    }
    
    @Test
    void testSearchFiles() throws Exception {
        // Importiere einige Testdateien mit unterschiedlichen Namen
        File testFile1 = tempDir.resolve("document.txt").toFile();
        Files.writeString(testFile1.toPath(), "Dokument-Inhalt");
        
        File testFile2 = tempDir.resolve("image.jpg").toFile();
        Files.writeString(testFile2.toPath(), "Bild-Daten");
        
        File testFile3 = tempDir.resolve("document_v2.txt").toFile();
        Files.writeString(testFile3.toPath(), "Dokument-Inhalt Version 2");
        
        fileStorage.importFile(testFile1, testFolder);
        fileStorage.importFile(testFile2, testFolder);
        fileStorage.importFile(testFile3, testFolder);
        
        // Hole alle Dateien und filtere nach Namen
        List<EncryptedFile> allFiles = fileStorage.getAllFiles();
        List<EncryptedFile> searchResults = allFiles.stream()
            .filter(file -> file.getOriginalName().contains("document"))
            .collect(Collectors.toList());
        
        assertNotNull(searchResults);
        assertEquals(2, searchResults.size());
        assertTrue(searchResults.stream().anyMatch(f -> f.getOriginalName().equals("document.txt")));
        assertTrue(searchResults.stream().anyMatch(f -> f.getOriginalName().equals("document_v2.txt")));
    }
}
