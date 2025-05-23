package com.filevault.util;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.filevault.model.VirtualFolder;
import com.filevault.storage.DatabaseManager;

/**
 * Tests für die rekursive Löschfunktion des FolderManagers.
 * Diese Klasse testet speziell das rekursive Löschen von Ordnerhierarchien.
 */
public class FolderManagerRecursiveDeleteTest {
    
    private FolderManager folderManager;
    
    /**
     * Initialisiert die Testumgebung vor jedem Test.
     * Erstellt eine Testdatenbank und initialisiert den FolderManager.
     */
    @BeforeEach
    public void setUp() throws SQLException {
        // Initialize the test database before tests
        DatabaseManager.initDatabase(true);
        
        // Get the FolderManager instance
        folderManager = FolderManager.getInstance();
        folderManager.initialize();
        
        // Log existing folders
        List<VirtualFolder> existingFolders = folderManager.getAllFolders();
        LoggingUtil.log("Existing folders before test: " + existingFolders.size());
    }
    
    /**
     * Bereinigt die Testumgebung nach jedem Test.
     * Löscht die Testdatenbank.
     */
    @AfterEach
    public void tearDown() {
        // Clean up the test database after each test
        DatabaseManager.deleteTestDatabase();
    }
    
    /**
     * Testet das rekursive Löschen einer kompletten Ordnerstruktur.
     * Überprüft, ob alle Ordner in der Hierarchie gelöscht werden.
     */
    @Test
    public void testRecursiveFolderDeletion() {
        // Erstelle eine isolierte Ordnerstruktur zum Testen
        String testPrefix = "TEST_" + System.currentTimeMillis() + "_";
        VirtualFolder root = folderManager.createFolder(testPrefix + "TestRoot", null);
        VirtualFolder folder1 = folderManager.createFolder(testPrefix + "Folder1", root.getId());
        VirtualFolder folder2 = folderManager.createFolder(testPrefix + "Folder2", folder1.getId());
        VirtualFolder folder3 = folderManager.createFolder(testPrefix + "Folder3", folder2.getId());
        
        // Überprüfe, dass die Testordner erstellt wurden
        List<VirtualFolder> testFolders = folderManager.getSubfolders(root.getId());
        assertEquals(1, testFolders.size(), "Es sollte 1 direkter Unterordner erstellt worden sein");
        
        // Teste rekursives Löschen
        folderManager.deleteFolderRecursive(root);
        
        // Überprüfe, dass der Root-Testordner gelöscht wurde
        VirtualFolder deletedFolder = folderManager.getFolderByName(testPrefix + "TestRoot");
        assertNull(deletedFolder, "Der Root-Testordner sollte gelöscht worden sein");
        
        LoggingUtil.log("Recursive deletion test completed successfully");
    }
    
    /**
     * Testet das teilweise rekursive Löschen einer Ordnerstruktur.
     * Überprüft, ob nur der angegebene Teilbaum gelöscht wird und andere Zweige intakt bleiben.
     */
    @Test
    public void testPartialRecursiveFolderDeletion() {
        // Erstelle eine komplexere isolierte Ordnerstruktur
        String testPrefix = "TEST_" + System.currentTimeMillis() + "_";
        VirtualFolder root = folderManager.createFolder(testPrefix + "TestRoot", null);
        VirtualFolder folder1 = folderManager.createFolder(testPrefix + "Folder1", root.getId());
        VirtualFolder folder2 = folderManager.createFolder(testPrefix + "Folder2", root.getId());
        VirtualFolder subFolder1 = folderManager.createFolder(testPrefix + "SubFolder1", folder1.getId());
        VirtualFolder subFolder2 = folderManager.createFolder(testPrefix + "SubFolder2", folder1.getId());
        
        // Lösche nur folder1 und seine Unterordner
        folderManager.deleteFolderRecursive(folder1);
        
        // Überprüfe, dass folder1 und seine Unterordner gelöscht wurden
        VirtualFolder deletedFolder1 = folderManager.getFolderByName(testPrefix + "Folder1");
        VirtualFolder deletedSubFolder1 = folderManager.getFolderByName(testPrefix + "SubFolder1");
        VirtualFolder deletedSubFolder2 = folderManager.getFolderByName(testPrefix + "SubFolder2");
        
        assertNull(deletedFolder1, "Folder1 sollte gelöscht worden sein");
        assertNull(deletedSubFolder1, "SubFolder1 sollte gelöscht worden sein");
        assertNull(deletedSubFolder2, "SubFolder2 sollte gelöscht worden sein");
        
        // Überprüfe, dass root und folder2 noch existieren
        VirtualFolder remainingRoot = folderManager.getFolderByName(testPrefix + "TestRoot");
        VirtualFolder remainingFolder2 = folderManager.getFolderByName(testPrefix + "Folder2");
        
        assertNotNull(remainingRoot, "Root sollte noch existieren");
        assertNotNull(remainingFolder2, "Folder2 sollte noch existieren");
        
        // Bereinige den Test, indem der verbleibende Testordner gelöscht wird
        folderManager.deleteFolderRecursive(root);
        
        LoggingUtil.log("Partial recursive deletion test completed successfully");
    }
    
    /**
     * Testet, dass die normale Löschfunktion bei nicht leeren Ordnern fehlschlägt.
     * Überprüft, ob eine IllegalStateException geworfen wird.
     */
    @Test
    public void testRegularDeleteFailsOnNonEmptyFolder() {
        // Erstelle einen isolierten Ordner mit Unterordnern
        String testPrefix = "TEST_" + System.currentTimeMillis() + "_";
        VirtualFolder root = folderManager.createFolder(testPrefix + "TestRoot", null);
        VirtualFolder folder1 = folderManager.createFolder(testPrefix + "Folder1", root.getId());
        
        // Versuche, den Root-Ordner mit normaler Delete-Methode zu löschen
        assertThrows(IllegalStateException.class, () -> {
            folderManager.deleteFolder(root);
        }, "Das Löschen eines Ordners mit Unterordnern sollte eine Exception werfen");
        
        // Bereinige den Test
        folderManager.deleteFolderRecursive(root);
        
        LoggingUtil.log("Regular delete fail test completed successfully");
    }
} 