package com.filevault.util;

import com.filevault.model.VirtualFolder;
import com.filevault.storage.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

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
        DatabaseManager.initDatabase();
        
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
    }

    /**
     * Testet die Erstellung eines neuen Ordners.
     * Überprüft, ob der Ordner korrekt erstellt wurde und in der Liste vorhanden ist.
     */
    @Test
    void testOrdnerErstellen() {
        VirtualFolder folder = folderManager.createFolder("TestFolder", "Test Description");
        
        assertNotNull(folder);
        assertEquals("TestFolder", folder.getName());
        assertEquals("Test Description", folder.getDescription());
        
        assertTrue(folderManager.getFolders().contains(folder));
    }

    /**
     * Testet das Umbenennen eines Ordners.
     * Überprüft, ob der Name erfolgreich geändert wurde.
     */
    @Test
    void testOrdnerUmbenennen() {
        VirtualFolder folder = folderManager.createFolder("OldName", "Test Description");
        
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
        VirtualFolder folder = folderManager.createFolder("ToDelete", "Test Description");
        
        boolean success = folderManager.deleteFolder(folder);
        
        assertTrue(success);
        assertFalse(folderManager.getFolders().contains(folder));
    }

    /**
     * Testet die Verwaltung des aktuellen Ordners.
     * Überprüft, ob der aktuelle Ordner korrekt gesetzt und abgerufen werden kann.
     */
    @Test
    void testAktuellerOrdner() {
        VirtualFolder folder = folderManager.createFolder("CurrentFolder", "Test Description");
        
        folderManager.setCurrentFolder(folder);
        
        assertEquals(folder, folderManager.getCurrentFolder());
    }
} 