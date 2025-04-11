package com.filevault.model;

import com.filevault.security.PasswordUtils;
import com.filevault.storage.DatabaseManager;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Manages user authentication and user-related operations.
 */
public class UserManager {

    private static UserManager instance;
    private String currentUser = null;
    private byte[] masterKey = null;
    
    private UserManager() {
        // Private constructor for singleton pattern
    }
    
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }
    
    /**
     * Checks if any user exists in the database
     * @return true if a user is registered, false otherwise
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
            System.err.println("Error checking if user exists: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Creates a new user with the given master password
     * @param masterPassword The user's master password
     * @return true if the user was created successfully
     */
    public boolean createUser(String masterPassword) {
        if (masterPassword == null || masterPassword.isEmpty()) {
            return false;
        }
        
        // Generate a password hash
        String passwordHash = BCrypt.hashpw(masterPassword, BCrypt.gensalt());
        
        // Generate a master key from the password
        masterKey = PasswordUtils.generateKeyFromPassword(masterPassword);
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, password_hash, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)")) {
            
            stmt.setString(1, "master"); // Only one user in this system
            stmt.setString(2, passwordHash);
            int affected = stmt.executeUpdate();
            
            if (affected > 0) {
                currentUser = "master";
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Authenticates a user with the provided password
     * @param password The master password to check
     * @return true if authentication is successful
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
            System.err.println("Error authenticating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Changes the user's master password
     * @param oldPassword The current master password
     * @param newPassword The new master password
     * @return true if the password was changed successfully
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
            System.err.println("Error changing password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get the current authenticated user
     * @return The username of the authenticated user, or null if no user is authenticated
     */
    public String getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Get the master encryption key
     * @return The master encryption key, or null if no user is authenticated
     */
    public byte[] getMasterKey() {
        return masterKey;
    }
    
    /**
     * Logs out the current user
     */
    public void logout() {
        currentUser = null;
        masterKey = null;
    }
} 