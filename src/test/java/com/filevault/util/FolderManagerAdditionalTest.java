package com.filevault.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.filevault.model.VirtualFolder;
import com.filevault.storage.DatabaseManager;

/**
 * Zusätzliche Tests für den FolderManager zur Verbesserung der Codeabdeckung.
 * Diese Klasse ergänzt die Haupttestklasse mit weiteren Testfällen.
 */
public class FolderManagerAdditionalTest {
    
    private FolderManager folderManager;
    private Path testDataDir;
    
    /**
     * Initialisiert die Testumgebung vor jedem Test.
     * Erstellt eine Testdatenbank und ein Testdatenverzeichnis.
     */
    @BeforeEach
    void setUp() throws Exception {
        // Initialize test database
        DatabaseManager.initDatabase(true);
        
        // Create test data directory
        testDataDir = Paths.get(System.getProperty("user.home"), ".filevault", "test_data");
        Files.createDirectories(testDataDir);
        
        // Get FolderManager instance
        folderManager = FolderManager.getInstance();
        folderManager.initialize();
    }
    
    /**
     * Bereinigt die Testumgebung nach jedem Test.
     * Schließt Datenbankverbindungen und löscht Testdateien.
     */
    @AfterEach
    void tearDown() throws Exception {
        // Close database connections
        DatabaseManager.closeConnections();
        
        // Clean up test data directory
        if (Files.exists(testDataDir)) {
            Files.walk(testDataDir)
                .map(Path::toFile)
                .forEach(File::delete);
            Files.deleteIfExists(testDataDir);
        }
        
        // Delete test database
        DatabaseManager.deleteTestDatabase();
    }
    
    /**
     * Testet die createDataDirectory-Methode, indem überprüft wird, ob das Verzeichnis existiert.
     * Die Methode wird während der Initialisierung aufgerufen.
     */
    @Test
    void testCreateDataDirectory() {
        // The createDataDirectory method is called during initialize()
        // We just need to verify the directory exists
        Path dataDir = Paths.get(System.getProperty("user.home"), ".filevault", "data");
        assertTrue(Files.exists(dataDir), "Datenverzeichnis sollte nach der Initialisierung existieren");
    }
    
    /**
     * Testet die getDataDirectoryPath-Methode.
     * Überprüft, ob der zurückgegebene Pfad korrekt ist.
     */
    @Test
    void testGetDataDirectoryPath() {
        String dataPath = folderManager.getDataDirectoryPath();
        assertNotNull(dataPath, "Datenverzeichnispfad sollte nicht null sein");
        assertTrue(dataPath.contains(".filevault"), "Datenverzeichnispfad sollte .filevault enthalten");
        assertTrue(dataPath.contains("data"), "Datenverzeichnispfad sollte das Unterverzeichnis data enthalten");
    }
    
    /**
     * Testet die reloadFromDatabase-Methode.
     * Überprüft, ob Änderungen in der Datenbank korrekt geladen werden.
     */
    @Test
    void testReloadFromDatabase() {
        // Create some test folders
        VirtualFolder rootFolder = folderManager.createFolder("ReloadRoot", null);
        folderManager.createFolder("ReloadChild", rootFolder.getId());
        
        // Get the initial count
        int initialCount = folderManager.getAllFolders().size();
        
        // Create another folder directly in the database to simulate external changes
        try {
            var conn = DatabaseManager.getConnection();
            var stmt = conn.prepareStatement(
                "INSERT INTO folders (name, description, parent_id) VALUES (?, ?, ?)");
            stmt.setString(1, "ExternalFolder");
            stmt.setString(2, "Created externally");
            stmt.setObject(3, rootFolder.getId());
            stmt.executeUpdate();
            conn.close();
        } catch (Exception e) {
            fail("Fehler beim Erstellen des externen Ordners: " + e.getMessage());
        }
        
        // Reload from database
        folderManager.reloadFromDatabase();
        
        // Check that the new folder was loaded
        int newCount = folderManager.getAllFolders().size();
        assertEquals(initialCount + 1, newCount, "Nach dem Neuladen sollte ein zusätzlicher Ordner vorhanden sein");
        
        // Verify the folder exists by name
        VirtualFolder externalFolder = folderManager.getFolderByName("ExternalFolder");
        assertNotNull(externalFolder, "Der extern erstellte Ordner sollte gefunden werden");
        assertEquals("Created externally", externalFolder.getDescription());
    }
    
    /**
     * Testet die getFolderByName-Methode.
     * Überprüft, ob Ordner korrekt nach Namen gefunden werden können.
     */
    @Test
    void testGetFolderByName() {
        // Create a folder with a unique name
        String uniqueName = "UniqueFolderName_" + System.currentTimeMillis();
        folderManager.createFolder(uniqueName, null);
        
        // Try to find it by name
        VirtualFolder found = folderManager.getFolderByName(uniqueName);
        assertNotNull(found, "Ordner sollte anhand des Namens gefunden werden");
        assertEquals(uniqueName, found.getName(), "Der gefundene Ordner sollte den korrekten Namen haben");
        
        // Try to find a non-existent folder
        VirtualFolder notFound = folderManager.getFolderByName("NonExistentFolder");
        assertNull(notFound, "Bei nicht existierendem Ordnernamen sollte null zurückgegeben werden");
    }
    
    /**
     * Testet die getSubfolders-Methode.
     * Überprüft, ob Unterordner korrekt zurückgegeben werden.
     */
    @Test
    void testGetSubfolders() {
        // Create a root folder
        VirtualFolder root = folderManager.createFolder("SubfolderRoot", null);
        
        // Create some subfolders
        folderManager.createFolder("SubChild1", root.getId());
        folderManager.createFolder("SubChild2", root.getId());
        folderManager.createFolder("SubChild3", root.getId());
        
        // Get subfolders
        List<VirtualFolder> subfolders = folderManager.getSubfolders(root.getId());
        
        // Verify
        assertEquals(3, subfolders.size(), "Es sollten 3 Unterordner vorhanden sein");
        
        // Verify names
        boolean foundChild1 = false;
        boolean foundChild2 = false;
        boolean foundChild3 = false;
        
        for (VirtualFolder subfolder : subfolders) {
            if ("SubChild1".equals(subfolder.getName())) foundChild1 = true;
            if ("SubChild2".equals(subfolder.getName())) foundChild2 = true;
            if ("SubChild3".equals(subfolder.getName())) foundChild3 = true;
        }
        
        assertTrue(foundChild1 && foundChild2 && foundChild3, "Alle Unterordner sollten gefunden werden");
    }
} 