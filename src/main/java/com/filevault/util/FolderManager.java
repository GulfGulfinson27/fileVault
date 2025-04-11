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
 * Verwaltet virtuelle Ordner in der Anwendung.
 * Bietet Funktionen zum Erstellen, Umbenennen und Löschen von Ordnern.
 */
public class FolderManager {
    
    private static FolderManager instance;
    private final List<VirtualFolder> folders = new ArrayList<>();
    private VirtualFolder currentFolder = null;
    
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
        folders.clear();
        loadFoldersFromDatabase();
        
        if (!folders.isEmpty()) {
            currentFolder = folders.get(0);
        }
        
        createDataDirectory();
    }
    
    /**
     * Erstellt die grundlegende Ordnerstruktur für einen neuen Benutzer.
     * Erstellt Standardordner für verschiedene Dateitypen.
     */
    public void createBaseStructure() {
        folders.clear();
        
        createFolder("Documents");
        createFolder("Images");
        createFolder("Videos");
        createFolder("Music");
        createFolder("Others");
        
        if (!folders.isEmpty()) {
            currentFolder = folders.get(0);
        }
        
        createDataDirectory();
    }
    
    /**
     * Erstellt das Datenverzeichnis, in dem verschlüsselte Dateien gespeichert werden.
     */
    private void createDataDirectory() {
        Path dataDir = Paths.get(System.getProperty("user.home"), ".filevault", "data");
        try {
            Files.createDirectories(dataDir);
        } catch (Exception e) {
            System.err.println("Fehler beim Erstellen des Datenverzeichnisses: " + e.getMessage());
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
            
            while (rs.next()) {
                VirtualFolder folder = new VirtualFolder(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                );
                folders.add(folder);
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Laden der Ordner aus der Datenbank: " + e.getMessage());
        }
    }
    
    /**
     * Erstellt einen neuen Ordner mit dem angegebenen Namen.
     * @param name Der Name des neuen Ordners
     * @return Der erstellte Ordner oder null bei Fehler
     */
    public VirtualFolder createFolder(String name) {
        return createFolder(name, "");
    }
    
    /**
     * Erstellt einen neuen Ordner mit Namen und Beschreibung.
     * @param name Der Name des neuen Ordners
     * @param description Die Beschreibung des Ordners
     * @return Der erstellte Ordner oder null bei Fehler
     */
    public VirtualFolder createFolder(String name, String description) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO folders (name, description) VALUES (?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    VirtualFolder folder = new VirtualFolder(rs.getInt(1), name, description);
                    folders.add(folder);
                    return folder;
                }
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Erstellen des Ordners: " + e.getMessage());
        }
        return null;
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
                     "UPDATE folders SET name = ? WHERE id = ?")) {
            
            stmt.setString(1, newName);
            stmt.setInt(2, folder.getId());
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
     * Löscht einen Ordner.
     * @param folder Der zu löschende Ordner
     * @return true, wenn der Ordner erfolgreich gelöscht wurde
     */
    public boolean deleteFolder(VirtualFolder folder) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM folders WHERE id = ?")) {
            
            stmt.setInt(1, folder.getId());
            int affected = stmt.executeUpdate();
            
            if (affected > 0) {
                folders.remove(folder);
                if (currentFolder == folder) {
                    currentFolder = folders.isEmpty() ? null : folders.get(0);
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Löschen des Ordners: " + e.getMessage());
        }
        return false;
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
} 