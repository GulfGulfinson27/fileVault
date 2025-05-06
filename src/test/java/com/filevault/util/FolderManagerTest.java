package com.filevault.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.filevault.model.VirtualFolder;
import com.filevault.storage.DatabaseManager;

/**
 * Testklasse für den FolderManager.
 * Testet die Funktionalität zum Erstellen, Umbenennen und Löschen von Ordnern.
 */
class FolderManagerTest {
    private FolderManager folderManager;
    private Path testDataDir;

    /**
     * Initialisiert die Datenbank und den FolderManager vor jedem Test.
     * Erstellt ein temporäres Testverzeichnis.
     */
    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager.initDatabase(true);  // Use test database
        
        testDataDir = Paths.get(System.getProperty("user.home"), ".filevault", "test_data");
        Files.createDirectories(testDataDir);
        
        folderManager = FolderManager.getInstance();
        folderManager.initialize();
    }

    /**
     * Bereinigt nach jedem Test.
     * Schließt Datenbankverbindungen und löscht das Testverzeichnis.
     */
    @AfterEach
    void tearDown() throws Exception {
        DatabaseManager.closeConnections();
        
        if (Files.exists(testDataDir)) {
            Files.walk(testDataDir)
                .map(Path::toFile)
                .forEach(File::delete);
            Files.deleteIfExists(testDataDir);
        }

        // Clean up all test folders
        List<VirtualFolder> folders = folderManager.getFolders();
        for (VirtualFolder folder : folders) {
            if (folder.getName().equals("TestFolder") || 
                folder.getName().equals("CurrentFolder") || 
                folder.getName().equals("OldName") || 
                folder.getName().equals("NewName") || 
                folder.getName().equals("ToDelete")) {
                folderManager.deleteFolder(folder);
            }
        }
        
        // Delete test database
        DatabaseManager.deleteTestDatabase();
    }

    /**
     * Testet die Erstellung eines neuen Ordners.
     * Überprüft, ob der Ordner korrekt erstellt wurde und in der Liste vorhanden ist.
     */
    @Test
    public void testOrdnerErstellen() {
        // Teste Erstellung eines Root-Ordners
        VirtualFolder rootFolder = folderManager.createFolder("Root", null);
        assertNotNull(rootFolder);
        assertEquals("Root", rootFolder.getName());
        assertNull(rootFolder.getParentId());
        
        // Teste Erstellung eines Unterordners
        VirtualFolder subFolder = folderManager.createFolder("Sub", rootFolder.getId());
        assertNotNull(subFolder);
        assertEquals("Sub", subFolder.getName());
        assertEquals(rootFolder.getId(), subFolder.getParentId());
        
        // Teste Erstellung eines Ordners mit ungültigem Namen
        assertThrows(IllegalArgumentException.class, () -> {
            folderManager.createFolder("", null);
        });
    }

    /**
     * Testet das Umbenennen eines Ordners.
     * Überprüft, ob der Name erfolgreich geändert wurde.
     */
    @Test
    void testOrdnerUmbenennen() {
        VirtualFolder folder = folderManager.createFolder("OldName", null);
        assertNotNull(folder);
        
        boolean success = folderManager.renameFolder(folder, "NewName");
        
        assertTrue(success);
        assertEquals("NewName", folder.getName());
    }

    /**
     * Testet das Löschen eines Ordners.
     * Überprüft, ob der Ordner erfolgreich gelöscht wurde und nicht mehr in der Liste ist.
     */
    @Test
    void testOrdnerLoeschen() {
        VirtualFolder folder = folderManager.createFolder("ToDelete", null);
        assertNotNull(folder);
        
        folderManager.deleteFolder(folder);
        assertFalse(folderManager.getFolders().contains(folder));
    }

    /**
     * Testet die Verwaltung des aktuellen Ordners.
     * Überprüft, ob der aktuelle Ordner korrekt gesetzt und abgerufen werden kann.
     */
    @Test
    void testAktuellerOrdner() {
        VirtualFolder folder = folderManager.createFolder("CurrentFolder", null);
        
        folderManager.setCurrentFolder(folder);
        
        assertEquals(folder, folderManager.getCurrentFolder());
    }

    @Test
    public void testVerschachtelteOrdner() {
        // Erstelle eine verschachtelte Ordnerstruktur
        VirtualFolder root = folderManager.createFolder("Root", null);
        assertNotNull(root);
        VirtualFolder level1 = folderManager.createFolder("Level1", root.getId());
        assertNotNull(level1);
        VirtualFolder level2 = folderManager.createFolder("Level2", level1.getId());
        assertNotNull(level2);
        
        // Teste die Hierarchie
        assertEquals(root.getId(), level1.getParentId());
        assertEquals(level1.getId(), level2.getParentId());
        
        // Teste das Löschen eines Ordners mit Unterordnern
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            folderManager.deleteFolder(root);
        });
        assertEquals("Ordner enthält Unterordner und kann nicht gelöscht werden", exception.getMessage());
        
        // Lösche zuerst die Unterordner
        folderManager.deleteFolder(level2);
        folderManager.deleteFolder(level1);
        folderManager.deleteFolder(root);
    }

    /**
     * Testet die Erstellung eines Ordners mit einem doppelten Namen im gleichen Verzeichnis.
     * Überprüft, ob eine IllegalArgumentException ausgelöst wird.
     */
    @Test
    public void testDuplicateFolderNameInSameParent() {
        VirtualFolder rootFolder = folderManager.createFolder("Root", null);
        assertNotNull(rootFolder);

        // Erstelle einen Ordner mit dem gleichen Namen im gleichen Verzeichnis
        folderManager.createFolder("Test", rootFolder.getId());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            folderManager.createFolder("Test", rootFolder.getId());
        });
        assertEquals("Ein Ordner mit diesem Namen existiert bereits im gleichen Verzeichnis", exception.getMessage());
    }

    /**
     * Testet die Erstellung eines Ordners mit einem doppelten Namen in unterschiedlichen Verzeichnissen.
     * Überprüft, ob die Erstellung erfolgreich ist.
     */
    @Test
    public void testDuplicateFolderNameInDifferentParents() {
        VirtualFolder rootFolder1 = folderManager.createFolder("Root1", null);
        VirtualFolder rootFolder2 = folderManager.createFolder("Root2", null);
        assertNotNull(rootFolder1);
        assertNotNull(rootFolder2);

        // Erstelle Ordner mit gleichem Namen in unterschiedlichen Verzeichnissen
        VirtualFolder folder1 = folderManager.createFolder("Test", rootFolder1.getId());
        VirtualFolder folder2 = folderManager.createFolder("Test", rootFolder2.getId());

        assertNotNull(folder1);
        assertNotNull(folder2);
        assertEquals("Test", folder1.getName());
        assertEquals("Test", folder2.getName());
    }
}