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
 * Additional tests for FolderManager to improve code coverage.
 */
public class FolderManagerAdditionalTest {
    
    private FolderManager folderManager;
    private Path testDataDir;
    
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
     * Test the createDataDirectory method by checking if the directory exists.
     */
    @Test
    void testCreateDataDirectory() {
        // The createDataDirectory method is called during initialize()
        // We just need to verify the directory exists
        Path dataDir = Paths.get(System.getProperty("user.home"), ".filevault", "data");
        assertTrue(Files.exists(dataDir), "Data directory should exist after initialization");
    }
    
    /**
     * Test the getDataDirectoryPath method.
     */
    @Test
    void testGetDataDirectoryPath() {
        String dataPath = folderManager.getDataDirectoryPath();
        assertNotNull(dataPath, "Data directory path should not be null");
        assertTrue(dataPath.contains(".filevault"), "Data directory path should contain .filevault");
        assertTrue(dataPath.contains("data"), "Data directory path should contain data subdirectory");
    }
    
    /**
     * Test the reloadFromDatabase method.
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
            fail("Failed to create external folder: " + e.getMessage());
        }
        
        // Reload from database
        folderManager.reloadFromDatabase();
        
        // Check that the new folder was loaded
        int newCount = folderManager.getAllFolders().size();
        assertEquals(initialCount + 1, newCount, "Should have one more folder after reload");
        
        // Verify the folder exists by name
        VirtualFolder externalFolder = folderManager.getFolderByName("ExternalFolder");
        assertNotNull(externalFolder, "Should find the externally created folder");
        assertEquals("Created externally", externalFolder.getDescription());
    }
    
    /**
     * Test the getFolderByName method.
     */
    @Test
    void testGetFolderByName() {
        // Create a folder with a unique name
        String uniqueName = "UniqueFolderName_" + System.currentTimeMillis();
        folderManager.createFolder(uniqueName, null);
        
        // Try to find it by name
        VirtualFolder found = folderManager.getFolderByName(uniqueName);
        assertNotNull(found, "Should find folder by name");
        assertEquals(uniqueName, found.getName(), "Found folder should have the correct name");
        
        // Try to find a non-existent folder
        VirtualFolder notFound = folderManager.getFolderByName("NonExistentFolder");
        assertNull(notFound, "Should return null for non-existent folder name");
    }
    
    /**
     * Test the getSubfolders method.
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
        assertEquals(3, subfolders.size(), "Should have 3 subfolders");
        
        // Verify names
        boolean foundChild1 = false;
        boolean foundChild2 = false;
        boolean foundChild3 = false;
        
        for (VirtualFolder subfolder : subfolders) {
            if ("SubChild1".equals(subfolder.getName())) foundChild1 = true;
            if ("SubChild2".equals(subfolder.getName())) foundChild2 = true;
            if ("SubChild3".equals(subfolder.getName())) foundChild3 = true;
        }
        
        assertTrue(foundChild1 && foundChild2 && foundChild3, "All children should be found");
    }
} 