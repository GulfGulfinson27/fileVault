package com.filevault.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.filevault.model.VirtualFolder;
import com.filevault.storage.DatabaseManager;

/**
 * Verwaltet virtuelle Ordner in der Anwendung.
 * Bietet Funktionen zum Erstellen, Umbenennen und Löschen von Ordnern.
 */
public class FolderManager {
    
    private static FolderManager instance;
    private final List<VirtualFolder> folders = new ArrayList<>();
    private VirtualFolder currentFolder = null;
    private static final Logger logger = LoggerFactory.getLogger(FolderManager.class);
    
    private FolderManager() {
        // Privater Konstruktor für Singleton-Pattern
    }
    
    /**
     * Gibt die einzige Instanz des FolderManagers zurück.
     * @return Die Singleton-Instanz des FolderManagers
     */
    public static synchronized FolderManager getInstance() {
        if (instance == null) {
            instance = new FolderManager();
        }
        return instance;
    }
    
    /**
     * Initialisiert die Ordner durch Laden aus der Datenbank.
     * Setzt den aktuellen Ordner auf den ersten verfügbaren Ordner.
     * Erstellt das Datenverzeichnis, falls es nicht existiert.
     */
    public void initialize() {
        LoggingUtil.logInfo("FolderManager", "Initializing folders.");
        folders.clear();
        loadFoldersFromDatabase();

        if (folders.isEmpty()) {
            LoggingUtil.logInfo("FolderManager", "No folders found. Creating base structure.");
            createBaseStructure();
        } else {
            currentFolder = folders.get(0);
        }

        createDataDirectory();
        LoggingUtil.logInfo("FolderManager", "Folder initialization completed.");
    }
    
    /**
     * Erstellt die grundlegende Ordnerstruktur für einen neuen Benutzer.
     * Erstellt Standardordner für verschiedene Dateitypen.
     */
    public void createBaseStructure() {
        LoggingUtil.logInfo("FolderManager", "Creating base folder structure.");
        folders.clear();

        // Create a root folder explicitly with parent_id as NULL
        VirtualFolder rootFolder = createFolder("Tresor", null);

        // Create base folders under the root folder
        createFolder("Dokumente", rootFolder.getId());
        createFolder("Bilder", rootFolder.getId());
        createFolder("Videos", rootFolder.getId());
        createFolder("Musik", rootFolder.getId());
        createFolder("Andere", rootFolder.getId());

        if (!folders.isEmpty()) {
            currentFolder = folders.get(0);
        }

        createDataDirectory();
        LoggingUtil.logInfo("FolderManager", "Base folder structure created.");
    }
    
    /**
     * Erstellt das Datenverzeichnis, in dem verschlüsselte Dateien gespeichert werden.
     */
    private void createDataDirectory() {
        Path dataDir = Paths.get(System.getProperty("user.home"), ".filevault", "data");
        try {
            Files.createDirectories(dataDir);
            LoggingUtil.logInfo("FolderManager", "Data directory created at: " + dataDir.toString());
        } catch (Exception e) {
            LoggingUtil.logError("FolderManager", "Error creating data directory: " + e.getMessage());
        }
    }
    
    /**
     * Lädt Ordner aus der Datenbank.
     * Stellt die Ordnerliste wieder her.
     */
    private void loadFoldersFromDatabase() {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM folders ORDER BY name");
             ResultSet rs = stmt.executeQuery()) {
            
            // First, create all folders
            while (rs.next()) {
                Integer parentId = null;
                if (rs.getObject("parent_id") != null) {
                    parentId = rs.getInt("parent_id");
                }
                
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                
                VirtualFolder folder = new VirtualFolder(id, name, description, parentId);
                folder.setCreatedAt(createdAt);
                folders.add(folder);
            }
            
            // Then, build the parent-child relationships
            for (VirtualFolder folder : folders) {
                if (folder.getParentId() != null) {
                    for (VirtualFolder potentialParent : folders) {
                        if (potentialParent.getId() == folder.getParentId()) {
                            potentialParent.addChild(folder);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Fehler beim Laden der Ordner aus der Datenbank", e);
            throw new RuntimeException("Fehler beim Laden der Ordner", e);
        }
    }
    
    /**
     * Erstellt einen neuen Ordner mit dem angegebenen Namen.
     * @param name Der Name des neuen Ordners
     * @param parentId Die ID des übergeordneten Ordners (null für Root-Ordner)
     * @return Der erstellte Ordner oder null bei Fehler
     */
    public VirtualFolder createFolder(String name, Integer parentId) {
        return createFolder(name, "", parentId);
    }
    
    /**
     * Checks if a folder with the same name exists under the same parent.
     * @param name The name of the folder to check.
     * @param parentId The ID of the parent folder.
     * @return true if a folder with the same name exists, false otherwise.
     */
    private boolean isDuplicateFolderName(String name, Integer parentId) {
        for (VirtualFolder folder : folders) {
            if (folder.getName().equalsIgnoreCase(name) && 
                ((folder.getParentId() == null && parentId == null) || 
                 (folder.getParentId() != null && folder.getParentId().equals(parentId)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Erstellt einen neuen Ordner mit Namen und Beschreibung.
     * @param name Der Name des neuen Ordners
     * @param description Die Beschreibung des Ordners
     * @param parentId Die ID des übergeordneten Ordners (null für Root-Ordner)
     * @return Der erstellte Ordner oder null bei Fehler
     * @throws IllegalArgumentException wenn der Name null oder leer ist
     */
    public VirtualFolder createFolder(String name, String description, Integer parentId) {
        LoggingUtil.logInfo("FolderManager", "Creating folder: " + name);
        if (name == null || name.trim().isEmpty()) {
            LoggingUtil.logError("FolderManager", "Folder creation failed: Name is empty.");
            throw new IllegalArgumentException("Ordnername darf nicht leer sein");
        }

        if (isDuplicateFolderName(name, parentId)) {
            LoggingUtil.logError("FolderManager", "Folder creation failed: Duplicate folder name.");
            throw new IllegalArgumentException("Ein Ordner mit diesem Namen existiert bereits im gleichen Verzeichnis");
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO folders (name, description, parent_id, created_at) VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setObject(3, parentId);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                LoggingUtil.logError("FolderManager", "Folder creation failed: No rows affected.");
                throw new SQLException("Creating folder failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    VirtualFolder folder = new VirtualFolder(id, name, description, parentId);
                    folder.setCreatedAt(LocalDateTime.now());
                    folders.add(folder);

                    if (parentId != null) {
                        for (VirtualFolder parent : folders) {
                            if (parent.getId() == parentId) {
                                parent.addChild(folder);
                                break;
                            }
                        }
                    }

                    LoggingUtil.logInfo("FolderManager", "Folder created successfully: " + name);
                    return folder;
                } else {
                    LoggingUtil.logError("FolderManager", "Folder creation failed: No ID obtained.");
                    throw new SQLException("Creating folder failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            LoggingUtil.logError("FolderManager", "Error creating folder: " + e.getMessage());
            throw new RuntimeException("Error creating folder", e);
        }
    }
    
    /**
     * Benennt einen Ordner um.
     * @param folder Der umzubenennende Ordner
     * @param newName Der neue Name des Ordners
     * @return true, wenn die Umbenennung erfolgreich war
     */
    public boolean renameFolder(VirtualFolder folder, String newName) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE folders SET name = ?, description = ? WHERE id = ?")) {
            
            stmt.setString(1, newName);
            stmt.setString(2, folder.getDescription());
            stmt.setInt(3, folder.getId());
            int affected = stmt.executeUpdate();
            
            if (affected > 0) {
                folder.setName(newName);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Umbenennen des Ordners: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Löscht einen Ordner und alle seine Dateien.
     * @param folder Der zu löschende Ordner
     * @throws IllegalStateException wenn der Ordner Unterordner enthält
     */
    public void deleteFolder(VirtualFolder folder) {
        LoggingUtil.logInfo("FolderManager", "Deleting folder: " + folder.getName());
        if (folder == null) {
            LoggingUtil.logError("FolderManager", "Folder deletion failed: Folder is null.");
            throw new IllegalArgumentException("Ordner darf nicht null sein");
        }

        // Prüfe auf Unterordner
        List<VirtualFolder> subfolders = getSubfolders(folder.getId());
        if (!subfolders.isEmpty()) {
            LoggingUtil.logError("FolderManager", "Folder deletion failed: Folder contains subfolders.");
            throw new IllegalStateException("Ordner enthält Unterordner und kann nicht gelöscht werden");
        }

        try {
            // Disable auto-commit mode
            DatabaseManager.getConnection().setAutoCommit(false);
            
            // Lösche alle Dateien im Ordner
            String deleteFilesSql = "DELETE FROM files WHERE folder_id = ?";
            try (PreparedStatement deleteFilesStmt = DatabaseManager.getConnection().prepareStatement(deleteFilesSql)) {
                deleteFilesStmt.setInt(1, folder.getId());
                deleteFilesStmt.executeUpdate();
            }

            // Lösche den Ordner
            String deleteFolderSql = "DELETE FROM folders WHERE id = ?";
            try (PreparedStatement deleteFolderStmt = DatabaseManager.getConnection().prepareStatement(deleteFolderSql)) {
                deleteFolderStmt.setInt(1, folder.getId());
                deleteFolderStmt.executeUpdate();
            }

            // Commit the transaction
            DatabaseManager.getConnection().commit();
            
            // Remove from parent's children list if it has a parent
            if (folder.getParentId() != null) {
                for (VirtualFolder parent : folders) {
                    if (parent.getId() == folder.getParentId()) {
                        parent.removeChild(folder);
                        break;
                    }
                }
            }
            
            // Remove from local list
            folders.remove(folder);
            
            // Reset auto-commit mode
            DatabaseManager.getConnection().setAutoCommit(true);
            LoggingUtil.logInfo("FolderManager", "Folder deleted successfully: " + folder.getName());
        } catch (SQLException e) {
            try {
                // Rollback in case of error
                DatabaseManager.getConnection().rollback();
                DatabaseManager.getConnection().setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                LoggingUtil.logError("FolderManager", "Error during rollback: " + rollbackEx.getMessage());
            }
            LoggingUtil.logError("FolderManager", "Error deleting folder: " + e.getMessage());
            throw new RuntimeException("Fehler beim Löschen des Ordners", e);
        }
    }
    
    /**
     * Gibt die Liste aller Ordner zurück.
     * @return Die Liste der Ordner
     */
    public List<VirtualFolder> getFolders() {
        return new ArrayList<>(folders);
    }
    
    /**
     * Gibt den aktuell ausgewählten Ordner zurück.
     * @return Der aktuelle Ordner oder null, wenn kein Ordner ausgewählt ist
     */
    public VirtualFolder getCurrentFolder() {
        return currentFolder;
    }
    
    /**
     * Setzt den aktuellen Ordner.
     * @param folder Der neue aktuelle Ordner
     */
    public void setCurrentFolder(VirtualFolder folder) {
        if (folders.contains(folder)) {
            currentFolder = folder;
        }
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
    
    /**
     * Gibt alle Unterordner eines Ordners zurück.
     * @param folderId Die ID des übergeordneten Ordners
     * @return Liste der Unterordner
     */
    public List<VirtualFolder> getSubfolders(int folderId) {
        List<VirtualFolder> subfolders = new ArrayList<>();
        for (VirtualFolder folder : folders) {
            if (folder.getParentId() != null && folder.getParentId() == folderId) {
                subfolders.add(folder);
            }
        }
        return subfolders;
    }

    /**
     * Gibt alle Ordner zurück.
     *
     * @return Eine Liste aller Ordner.
     */
    public List<VirtualFolder> getAllFolders() {
        return new ArrayList<>(folders);
    }
}