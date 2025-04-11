package com.filevault.util;

import com.filevault.model.VirtualFolder;
import com.filevault.storage.DatabaseManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages virtual folders in the application.
 */
public class FolderManager {
    
    private static FolderManager instance;
    private final List<VirtualFolder> folders = new ArrayList<>();
    private VirtualFolder currentFolder = null;
    
    private FolderManager() {
        // Private constructor for singleton pattern
    }
    
    public static synchronized FolderManager getInstance() {
        if (instance == null) {
            instance = new FolderManager();
        }
        return instance;
    }
    
    /**
     * Initializes the folders by loading them from the database
     */
    public void initialize() {
        folders.clear();
        loadFoldersFromDatabase();
        
        // Set the current folder to the first folder if available
        if (!folders.isEmpty()) {
            currentFolder = folders.get(0);
        }
        
        // Ensure the data directory exists
        createDataDirectory();
    }
    
    /**
     * Creates the initial folder structure for a new user
     */
    public void createBaseStructure() {
        folders.clear();
        
        // Create the default folders
        createFolder("Documents");
        createFolder("Images");
        createFolder("Videos");
        createFolder("Music");
        createFolder("Others");
        
        // Set the current folder to the first folder
        if (!folders.isEmpty()) {
            currentFolder = folders.get(0);
        }
        
        // Ensure the data directory exists
        createDataDirectory();
    }
    
    /**
     * Creates the data directory where encrypted files will be stored
     */
    private void createDataDirectory() {
        Path dataDir = Paths.get(System.getProperty("user.home"), ".filevault", "data");
        try {
            Files.createDirectories(dataDir);
        } catch (Exception e) {
            System.err.println("Error creating data directory: " + e.getMessage());
        }
    }
    
    /**
     * Loads folders from the database
     */
    private void loadFoldersFromDatabase() {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM folders ORDER BY name");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                VirtualFolder folder = new VirtualFolder(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                );
                folders.add(folder);
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading folders: " + e.getMessage());
        }
    }
    
    /**
     * Creates a new folder
     * @param name The name of the folder
     * @return The created folder, or null if creation failed
     */
    public VirtualFolder createFolder(String name) {
        return createFolder(name, "");
    }
    
    /**
     * Creates a new folder with a description
     * @param name The name of the folder
     * @param description The description of the folder
     * @return The created folder, or null if creation failed
     */
    public VirtualFolder createFolder(String name, String description) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO folders (name, description) VALUES (?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, name);
            stmt.setString(2, description);
            int affected = stmt.executeUpdate();
            
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        VirtualFolder folder = new VirtualFolder(id, name, description);
                        folders.add(folder);
                        return folder;
                    }
                }
            }
            
            return null;
        } catch (SQLException e) {
            System.err.println("Error creating folder: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Renames a folder
     * @param folder The folder to rename
     * @param newName The new name for the folder
     * @return true if the folder was renamed successfully
     */
    public boolean renameFolder(VirtualFolder folder, String newName) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE folders SET name = ? WHERE id = ?")) {
            
            stmt.setString(1, newName);
            stmt.setInt(2, folder.getId());
            int affected = stmt.executeUpdate();
            
            if (affected > 0) {
                folder.setName(newName);
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            System.err.println("Error renaming folder: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deletes a folder
     * @param folder The folder to delete
     * @return true if the folder was deleted successfully
     */
    public boolean deleteFolder(VirtualFolder folder) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement deleteFilesStmt = conn.prepareStatement(
                     "DELETE FROM files WHERE folder_id = ?");
             PreparedStatement deleteFolderStmt = conn.prepareStatement(
                     "DELETE FROM folders WHERE id = ?")) {
            
            // First delete all files in the folder
            deleteFilesStmt.setInt(1, folder.getId());
            deleteFilesStmt.executeUpdate();
            
            // Then delete the folder
            deleteFolderStmt.setInt(1, folder.getId());
            int affected = deleteFolderStmt.executeUpdate();
            
            if (affected > 0) {
                folders.remove(folder);
                
                // If the current folder was deleted, select another folder
                if (currentFolder != null && currentFolder.getId() == folder.getId()) {
                    currentFolder = folders.isEmpty() ? null : folders.get(0);
                }
                
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            System.err.println("Error deleting folder: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets all folders
     * @return A list of all folders
     */
    public List<VirtualFolder> getFolders() {
        return new ArrayList<>(folders);
    }
    
    /**
     * Gets the current folder
     * @return The current folder, or null if no folder is selected
     */
    public VirtualFolder getCurrentFolder() {
        return currentFolder;
    }
    
    /**
     * Sets the current folder
     * @param folder The folder to set as current
     */
    public void setCurrentFolder(VirtualFolder folder) {
        this.currentFolder = folder;
    }
    
    /**
     * Gets the path to the data directory
     * @return The path to the data directory
     */
    public String getDataDirectoryPath() {
        return Paths.get(System.getProperty("user.home"), ".filevault", "data").toString();
    }
    
    /**
     * Gets a folder by its name.
     * @param name The name of the folder to find
     * @return The folder if found, null otherwise
     */
    public VirtualFolder getFolderByName(String name) {
        for (VirtualFolder folder : folders) {
            if (folder.getName().equals(name)) {
                return folder;
            }
        }
        return null;
    }
} 