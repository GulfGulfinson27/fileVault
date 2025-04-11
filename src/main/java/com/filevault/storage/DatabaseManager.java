package com.filevault.storage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite database connection and schema.
 */
public class DatabaseManager {

    private static final String DB_PATH = System.getProperty("user.home") + File.separator + ".filevault" + File.separator + "vault.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;
    
    private static Connection connection;
    
    /**
     * Initializes the database, creating tables if they don't exist.
     */
    public static void initDatabase() {
        try {
            // Ensure parent directory exists
            Path parentDir = Paths.get(DB_PATH).getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            
            // Connect to the database (creates it if it doesn't exist)
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            
            // Enable foreign keys
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            
            // Create tables if they don't exist
            createTables();
            
            System.out.println("Database initialized successfully.");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates the necessary database tables
     */
    private static void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                         "username TEXT PRIMARY KEY, " +
                         "password_hash TEXT NOT NULL, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "last_login TIMESTAMP)");
            
            // Folders table
            stmt.execute("CREATE TABLE IF NOT EXISTS folders (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "name TEXT NOT NULL, " +
                         "description TEXT, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // Files table
            stmt.execute("CREATE TABLE IF NOT EXISTS files (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "folder_id INTEGER, " +
                         "original_name TEXT NOT NULL, " +
                         "encrypted_path TEXT NOT NULL, " +
                         "size_bytes INTEGER, " +
                         "mime_type TEXT, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "last_access TIMESTAMP, " +
                         "FOREIGN KEY (folder_id) REFERENCES folders(id))");
            
            // Settings table
            stmt.execute("CREATE TABLE IF NOT EXISTS settings (" +
                         "key TEXT PRIMARY KEY, " +
                         "value TEXT, " +
                         "description TEXT)");
        }
    }
    
    /**
     * Gets a connection to the database
     * @return A connection to the database
     * @throws SQLException if a database error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }
    
    /**
     * Closes all database connections
     */
    public static void closeConnections() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
} 