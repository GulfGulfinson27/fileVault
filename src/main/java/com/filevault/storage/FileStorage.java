package com.filevault.storage;

import com.filevault.model.EncryptedFile;
import com.filevault.model.VirtualFolder;
import com.filevault.security.EncryptionService;
import com.filevault.util.FolderManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages storage and retrieval of encrypted files.
 */
public class FileStorage {
    
    private static FileStorage instance;
    
    private FileStorage() {
        // Private constructor for singleton pattern
    }
    
    public static synchronized FileStorage getInstance() {
        if (instance == null) {
            instance = new FileStorage();
        }
        return instance;
    }
    
    /**
     * Imports a file into the vault.
     * 
     * @param sourceFile The file to import
     * @param folder The folder to import the file into
     * @return The imported file, or null if import failed
     * @throws Exception if an error occurs during import
     */
    public EncryptedFile importFile(File sourceFile, VirtualFolder folder) throws Exception {
        if (!sourceFile.exists() || !sourceFile.isFile() || !sourceFile.canRead()) {
            throw new IOException("Cannot read source file: " + sourceFile.getAbsolutePath());
        }
        
        // Generate a unique encrypted file name
        String encryptedFileName = UUID.randomUUID().toString();
        String encryptedFilePath = Paths.get(FolderManager.getInstance().getDataDirectoryPath(), encryptedFileName).toString();
        File encryptedFile = new File(encryptedFilePath);
        
        // Encrypt the file
        EncryptionService.getInstance().encryptFile(sourceFile, encryptedFile);
        
        // Detect MIME type
        String mimeType = Files.probeContentType(sourceFile.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        
        // Save file metadata to database
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO files (folder_id, original_name, encrypted_path, size_bytes, mime_type, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, folder.getId());
            stmt.setString(2, sourceFile.getName());
            stmt.setString(3, encryptedFilePath);
            stmt.setLong(4, sourceFile.length());
            stmt.setString(5, mimeType);
            
            int affected = stmt.executeUpdate();
            
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        
                        // Create and return the encrypted file object
                        return new EncryptedFile(
                                id,
                                folder.getId(),
                                sourceFile.getName(),
                                encryptedFilePath,
                                sourceFile.length(),
                                mimeType,
                                LocalDateTime.now(),
                                null
                        );
                    }
                }
            }
            
            // If we get here, something went wrong with the database
            encryptedFile.delete();
            return null;
        }
    }
    
    /**
     * Exports a file from the vault.
     * 
     * @param encryptedFile The file to export
     * @param destinationFile The destination file
     * @return true if export was successful
     * @throws Exception if an error occurs during export
     */
    public boolean exportFile(EncryptedFile encryptedFile, File destinationFile) throws Exception {
        File sourceFile = new File(encryptedFile.getEncryptedPath());
        if (!sourceFile.exists() || !sourceFile.isFile() || !sourceFile.canRead()) {
            throw new IOException("Cannot read encrypted file: " + sourceFile.getAbsolutePath());
        }
        
        // Decrypt the file
        boolean success = EncryptionService.getInstance().decryptFile(sourceFile, destinationFile);
        
        if (success) {
            // Update last access timestamp
            updateLastAccess(encryptedFile.getId());
        }
        
        return success;
    }
    
    /**
     * Deletes a file from the vault.
     * 
     * @param encryptedFile The file to delete
     * @return true if deletion was successful
     */
    public boolean deleteFile(EncryptedFile encryptedFile) {
        try {
            // First delete the physical file
            File file = new File(encryptedFile.getEncryptedPath());
            if (file.exists()) {
                file.delete();
            }
            
            // Then delete the database record
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM files WHERE id = ?")) {
                
                stmt.setInt(1, encryptedFile.getId());
                int affected = stmt.executeUpdate();
                
                return affected > 0;
            }
        } catch (Exception e) {
            System.err.println("Error deleting file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Renames a file in the vault.
     * 
     * @param encryptedFile The file to rename
     * @param newName The new name for the file
     * @return true if renaming was successful
     */
    public boolean renameFile(EncryptedFile encryptedFile, String newName) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE files SET original_name = ? WHERE id = ?")) {
            
            stmt.setString(1, newName);
            stmt.setInt(2, encryptedFile.getId());
            int affected = stmt.executeUpdate();
            
            if (affected > 0) {
                encryptedFile.setOriginalName(newName);
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            System.err.println("Error renaming file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Moves a file to a different folder.
     * 
     * @param encryptedFile The file to move
     * @param targetFolder The target folder
     * @return true if the file was moved successfully
     */
    public boolean moveFile(EncryptedFile encryptedFile, VirtualFolder targetFolder) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE files SET folder_id = ? WHERE id = ?")) {
            
            stmt.setInt(1, targetFolder.getId());
            stmt.setInt(2, encryptedFile.getId());
            int affected = stmt.executeUpdate();
            
            if (affected > 0) {
                encryptedFile.setFolderId(targetFolder.getId());
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            System.err.println("Error moving file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets all files in a folder.
     * 
     * @param folder The folder to get files from
     * @return A list of files in the folder
     */
    public List<EncryptedFile> getFilesInFolder(VirtualFolder folder) {
        List<EncryptedFile> files = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM files WHERE folder_id = ? ORDER BY original_name")) {
            
            stmt.setInt(1, folder.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp createdTimestamp = rs.getTimestamp("created_at");
                    Timestamp lastAccessTimestamp = rs.getTimestamp("last_access");
                    
                    LocalDateTime createdAt = createdTimestamp != null 
                            ? createdTimestamp.toLocalDateTime() 
                            : null;
                            
                    LocalDateTime lastAccess = lastAccessTimestamp != null 
                            ? lastAccessTimestamp.toLocalDateTime() 
                            : null;
                    
                    EncryptedFile file = new EncryptedFile(
                            rs.getInt("id"),
                            rs.getInt("folder_id"),
                            rs.getString("original_name"),
                            rs.getString("encrypted_path"),
                            rs.getLong("size_bytes"),
                            rs.getString("mime_type"),
                            createdAt,
                            lastAccess
                    );
                    
                    files.add(file);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting files: " + e.getMessage());
        }
        
        return files;
    }
    
    /**
     * Gets a file by its ID.
     * 
     * @param fileId The ID of the file to get
     * @return The file, or null if not found
     */
    public EncryptedFile getFileById(int fileId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM files WHERE id = ?")) {
            
            stmt.setInt(1, fileId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp createdTimestamp = rs.getTimestamp("created_at");
                    Timestamp lastAccessTimestamp = rs.getTimestamp("last_access");
                    
                    LocalDateTime createdAt = createdTimestamp != null 
                            ? createdTimestamp.toLocalDateTime() 
                            : null;
                            
                    LocalDateTime lastAccess = lastAccessTimestamp != null 
                            ? lastAccessTimestamp.toLocalDateTime() 
                            : null;
                    
                    return new EncryptedFile(
                            rs.getInt("id"),
                            rs.getInt("folder_id"),
                            rs.getString("original_name"),
                            rs.getString("encrypted_path"),
                            rs.getLong("size_bytes"),
                            rs.getString("mime_type"),
                            createdAt,
                            lastAccess
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting file: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Updates the last access timestamp for a file.
     * 
     * @param fileId The ID of the file to update
     */
    private void updateLastAccess(int fileId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE files SET last_access = CURRENT_TIMESTAMP WHERE id = ?")) {
            
            stmt.setInt(1, fileId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last access: " + e.getMessage());
        }
    }
} 