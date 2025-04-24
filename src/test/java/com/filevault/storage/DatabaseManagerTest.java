package com.filevault.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatabaseManager.
 */
public class DatabaseManagerTest {

    @BeforeEach
    void setUp() {
        // Initialize the test database before each test
        DatabaseManager.initDatabase(true);
    }

    @AfterEach
    void tearDown() {
        // Clean up the test database after each test
        DatabaseManager.deleteTestDatabase();
    }

    @Test
    void testInitDatabaseCreatesTables() {
        try (Connection connection = DatabaseManager.getConnection()) {
            // Check if the tables exist by querying their metadata
            assertTrue(connection.getMetaData().getTables(null, null, "users", null).next());
            assertTrue(connection.getMetaData().getTables(null, null, "folders", null).next());
            assertTrue(connection.getMetaData().getTables(null, null, "files", null).next());
            assertTrue(connection.getMetaData().getTables(null, null, "settings", null).next());
        } catch (SQLException e) {
            fail("Database connection or table check failed: " + e.getMessage());
        }
    }

    @Test
    void testGetConnection() {
        try (Connection connection = DatabaseManager.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
        } catch (SQLException e) {
            fail("Failed to get a valid database connection: " + e.getMessage());
        }
    }

    @Test
    void testDeleteTestDatabase() {
        Path testDbPath = Paths.get(System.getProperty("user.home"), ".filevault", "test_vault.db");
        assertTrue(Files.exists(testDbPath));

        DatabaseManager.deleteTestDatabase();

        assertFalse(Files.exists(testDbPath));
    }
}