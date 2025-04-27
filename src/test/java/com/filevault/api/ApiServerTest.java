package com.filevault.api;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.filevault.storage.DatabaseManager;

/**
 * Testklasse für die API-Server-Klasse.
 * Diese Klasse testet die verschiedenen Endpunkte des API-Servers.
 */
public class ApiServerTest {

    private ApiServer apiServer;
    private static final int TEST_PORT = 8082;

    @BeforeEach
    public void setUp() throws IOException {
        DatabaseManager.initDatabase(true);
        apiServer = new ApiServer();
        apiServer.start(TEST_PORT);

        // Add test token to TokenManager
        ApiServer.TokenManager.generateToken("valid-token");
    }

    @AfterEach
    public void tearDown() {
        apiServer.stop();
        DatabaseManager.deleteTestDatabase();
    }
}