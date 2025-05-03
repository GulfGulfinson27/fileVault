package com.filevault.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import com.filevault.security.PasswordUtils;
import com.filevault.storage.DatabaseManager;
import com.filevault.util.LoggingUtil;

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
        LoggingUtil.logInfo("UserManager", "Checking if user exists.");
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                LoggingUtil.logInfo("UserManager", "User exists: " + exists);
                return exists;
            }

            LoggingUtil.logInfo("UserManager", "No user found.");
            return false;
        } catch (SQLException e) {
            LoggingUtil.logError("UserManager", "Error checking user existence: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Erstellt einen neuen Benutzer mit dem angegebenen Master-Passwort.
     * @param masterPassword Das Master-Passwort des Benutzers
     * @return true, wenn der Benutzer erfolgreich erstellt wurde
     */
    public boolean createUser(String masterPassword) {
        LoggingUtil.logInfo("UserManager", "Attempting to create user.");
        if (masterPassword == null || masterPassword.isEmpty()) {
            LoggingUtil.logError("UserManager", "User creation failed: Password is empty.");
            return false;
        }

        if (userExists()) {
            LoggingUtil.logError("UserManager", "User creation failed: User already exists.");
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
                LoggingUtil.logInfo("UserManager", "User created successfully.");
                return true;
            }

            LoggingUtil.logError("UserManager", "User creation failed: No rows affected.");
            return false;
        } catch (SQLException e) {
            LoggingUtil.logError("UserManager", "Error creating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Authentifiziert einen Benutzer mit dem angegebenen Passwort.
     * @param password Das zu überprüfende Master-Passwort
     * @return true, wenn die Authentifizierung erfolgreich war
     */
    public boolean authenticate(String password) {
        LoggingUtil.logInfo("UserManager", "Attempting to authenticate user.");
        if (password == null || password.isEmpty()) {
            LoggingUtil.logError("UserManager", "Authentication failed: Password is empty.");
            return false;
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT username, password_hash FROM users WHERE username = ?")) {

            stmt.setString(1, "master");

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");

                    if (BCrypt.checkpw(password, storedHash)) {
                        currentUser = rs.getString("username");
                        masterKey = PasswordUtils.generateKeyFromPassword(password);
                        LoggingUtil.logInfo("UserManager", "Authentication successful.");
                        return true;
                    }
                }
            }

            LoggingUtil.logError("UserManager", "Authentication failed: Invalid credentials.");
            return false;
        } catch (SQLException e) {
            LoggingUtil.logError("UserManager", "Error during authentication: " + e.getMessage());
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
        LoggingUtil.logInfo("UserManager", "Attempting to change password.");
        if (!authenticate(oldPassword)) {
            LoggingUtil.logError("UserManager", "Password change failed: Old password is incorrect.");
            return false;
        }

        String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE users SET password_hash = ? WHERE username = ?")) {

            stmt.setString(1, newPasswordHash);
            stmt.setString(2, "master");

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                masterKey = PasswordUtils.generateKeyFromPassword(newPassword);
                LoggingUtil.logInfo("UserManager", "Password changed successfully.");
                return true;
            }

            LoggingUtil.logError("UserManager", "Password change failed: No rows affected.");
            return false;
        } catch (SQLException e) {
            LoggingUtil.logError("UserManager", "Error changing password: " + e.getMessage());
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
        LoggingUtil.logInfo("UserManager", "Attempting to delete user.");
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users")) {

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                currentUser = null;
                masterKey = null;
                LoggingUtil.logInfo("UserManager", "User deleted successfully.");
                return true;
            }

            LoggingUtil.logError("UserManager", "User deletion failed: No rows affected.");
            return false;
        } catch (SQLException e) {
            LoggingUtil.logError("UserManager", "Error deleting user: " + e.getMessage());
            return false;
        }
    }
}