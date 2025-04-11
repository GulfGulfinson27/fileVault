package com.filevault.model;

import com.filevault.security.PasswordUtils;
import com.filevault.storage.DatabaseManager;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Verwaltet die Benutzerauthentifizierung und benutzerbezogene Operationen.
 */
public class UserManager {

    private static UserManager instance;
    private String currentUser = null;
    private byte[] masterKey = null;
    
    private UserManager() {
        // Privater Konstruktor für Singleton-Pattern
    }
    
    /**
     * Gibt die einzige Instanz des UserManagers zurück.
     * @return Die Singleton-Instanz des UserManagers
     */
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }
    
    /**
     * Überprüft, ob ein Benutzer in der Datenbank existiert.
     * @return true, wenn ein Benutzer registriert ist, sonst false
     */
    public boolean userExists() {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
            return false;
        } catch (SQLException e) {
            System.err.println("Fehler beim Überprüfen des Benutzers: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Erstellt einen neuen Benutzer mit dem angegebenen Master-Passwort.
     * @param masterPassword Das Master-Passwort des Benutzers
     * @return true, wenn der Benutzer erfolgreich erstellt wurde
     */
    public boolean createUser(String masterPassword) {
        if (masterPassword == null || masterPassword.isEmpty()) {
            return false;
        }
        
        // Überprüfen, ob bereits ein Benutzer existiert
        if (userExists()) {
            System.err.println("Ein Benutzer existiert bereits");
            return false;
        }
        
        String passwordHash = BCrypt.hashpw(masterPassword, BCrypt.gensalt());
        masterKey = PasswordUtils.generateKeyFromPassword(masterPassword);
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, password_hash, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)")) {
            
            stmt.setString(1, "master");
            stmt.setString(2, passwordHash);
            int affected = stmt.executeUpdate();
            
            if (affected > 0) {
                currentUser = "master";
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            System.err.println("Fehler beim Erstellen des Benutzers: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Authentifiziert einen Benutzer mit dem angegebenen Passwort.
     * @param password Das zu überprüfende Master-Passwort
     * @return true, wenn die Authentifizierung erfolgreich war
     */
    public boolean authenticate(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT username, password_hash FROM users WHERE username = ?")) {
            
            stmt.setString(1, "master"); // Only one user in this system
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    
                    // Check if the provided password matches the stored hash
                    if (BCrypt.checkpw(password, storedHash)) {
                        currentUser = rs.getString("username");
                        
                        // Generate the master key from the password
                        masterKey = PasswordUtils.generateKeyFromPassword(password);
                        
                        return true;
                    }
                }
            }
            
            return false;
        } catch (SQLException e) {
            System.err.println("Fehler bei der Authentifizierung: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Ändert das Master-Passwort des Benutzers.
     * @param oldPassword Das aktuelle Master-Passwort
     * @param newPassword Das neue Master-Passwort
     * @return true, wenn die Passwortänderung erfolgreich war
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (!authenticate(oldPassword)) {
            return false;
        }
        
        // Generate a new password hash
        String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE users SET password_hash = ? WHERE username = ?")) {
            
            stmt.setString(1, newPasswordHash);
            stmt.setString(2, "master");
            
            int affected = stmt.executeUpdate();
            
            if (affected > 0) {
                // Update the master key with the new password
                masterKey = PasswordUtils.generateKeyFromPassword(newPassword);
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            System.err.println("Fehler beim Ändern des Passworts: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gibt den aktuell authentifizierten Benutzer zurück.
     * @return Der Benutzername des authentifizierten Benutzers oder null, wenn kein Benutzer authentifiziert ist
     */
    public String getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Gibt den Master-Verschlüsselungsschlüssel zurück.
     * @return Der Master-Verschlüsselungsschlüssel oder null, wenn kein Benutzer authentifiziert ist
     */
    public byte[] getMasterKey() {
        return masterKey;
    }
    
    /**
     * Meldet den aktuellen Benutzer ab.
     */
    public void logout() {
        currentUser = null;
        masterKey = null;
    }
    
    /**
     * Löscht den aktuellen Benutzer aus der Datenbank.
     * @return true, wenn der Benutzer erfolgreich gelöscht wurde
     */
    public boolean deleteUser() {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users")) {
            
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                currentUser = null;
                masterKey = null;
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Fehler beim Löschen des Benutzers: " + e.getMessage());
            return false;
        }
    }
} 