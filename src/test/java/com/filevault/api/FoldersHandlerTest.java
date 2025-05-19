package com.filevault.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.filevault.storage.DatabaseManager;
import com.filevault.util.FolderManager;

/**
 * Tests for the FoldersHandler functionality using real HTTP requests.
 * These tests focus on verifying that the API endpoints respond with the correct status codes.
 */
class FoldersHandlerTest {

    private ApiServer apiServer;
    private final int TEST_PORT = 8767; // Different port from other tests
    private ExecutorService executorService;
    private String validToken;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize the test database
        DatabaseManager.initDatabase(true);
        
        apiServer = new ApiServer();
        executorService = Executors.newSingleThreadExecutor();
        
        // Generate a valid token for testing
        validToken = ApiServer.TokenManager.generateToken("testuser");
        
        // Initialize the folder manager
        FolderManager.getInstance().initialize();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (apiServer != null) {
            apiServer.stop();
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        
        // Clean up the token
        ApiServer.TokenManager.invalidateToken(validToken);
        
        // Clean up the database
        DatabaseManager.closeConnections();
        DatabaseManager.deleteTestDatabase();
    }

    @Test
    void testListFolders() throws IOException {
        // Start the server
        startServer();
        
        // Create a GET request to the folders endpoint
        URL url = new URL("http://localhost:" + TEST_PORT + "/api/folders");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", validToken);
        
        // Send the request
        int responseCode = connection.getResponseCode();
        
        // We expect a 200 OK response
        assertEquals(200, responseCode, "Should get 200 OK when listing folders");
    }
    
    @Test
    void testFolderEndpointWithInvalidToken() throws IOException {
        // Start the server
        startServer();
        
        // Create a GET request to the folders endpoint
        URL url = new URL("http://localhost:" + TEST_PORT + "/api/folders");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "invalid-token");
        
        // Send the request
        int responseCode = connection.getResponseCode();
        
        // We expect a 401 Unauthorized response
        assertEquals(401, responseCode, "Should get 401 Unauthorized with invalid token");
    }
    
    @Test
    void testFolderEndpointWithNoToken() throws IOException {
        // Start the server
        startServer();
        
        // Create a GET request to the folders endpoint
        URL url = new URL("http://localhost:" + TEST_PORT + "/api/folders");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        // Send the request
        int responseCode = connection.getResponseCode();
        
        // We expect a 401 Unauthorized response
        assertEquals(401, responseCode, "Should get 401 Unauthorized with no token");
    }
    
    /**
     * Helper method to start the server and wait for it to be ready
     */
    private void startServer() {
        executorService.submit(() -> {
            try {
                apiServer.start(TEST_PORT);
            } catch (IOException e) {
                fail("Server failed to start: " + e.getMessage());
            }
        });
        
        // Give the server time to start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
} 