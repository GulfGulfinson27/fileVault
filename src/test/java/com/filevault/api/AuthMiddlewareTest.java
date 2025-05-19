package com.filevault.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the AuthMiddleware functionality using real HTTP requests.
 */
class AuthMiddlewareTest {

    private ApiServer apiServer;
    private final int TEST_PORT = 8766; // Different port from ApiServerTest
    private ExecutorService executorService;
    private String validToken;

    @BeforeEach
    void setUp() throws Exception {
        apiServer = new ApiServer();
        executorService = Executors.newSingleThreadExecutor();
        
        // Generate a valid token for testing
        validToken = ApiServer.TokenManager.generateToken("testuser");
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
    }

    @Test
    void testTokenManagerFunctions() {
        // Test token generation
        String token = ApiServer.TokenManager.generateToken("newuser");
        assertNotNull(token);
        assertTrue(ApiServer.TokenManager.isValidToken(token));
        
        // Test token invalidation
        ApiServer.TokenManager.invalidateToken(token);
        assertFalse(ApiServer.TokenManager.isValidToken(token));
    }
    
    @Test
    void testProtectedEndpointWithValidToken() throws IOException {
        // Start the server
        startServer();
        
        // Create a request to a protected endpoint
        URL url = new URL("http://localhost:" + TEST_PORT + "/api/folders");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        // Add the valid token
        connection.setRequestProperty("Authorization", validToken);
        
        // Send the request
        int responseCode = connection.getResponseCode();
        
        // We expect a response, possibly 200 OK if the endpoint works
        // or possibly another code if the endpoint has other requirements,
        // but definitely not 401 Unauthorized
        assertNotEquals(401, responseCode, "Should not get 401 Unauthorized with valid token");
    }
    
    @Test
    void testProtectedEndpointWithInvalidToken() throws IOException {
        // Start the server
        startServer();
        
        // Create a request to a protected endpoint
        URL url = new URL("http://localhost:" + TEST_PORT + "/api/folders");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        // Add an invalid token
        connection.setRequestProperty("Authorization", "invalid-token");
        
        // Send the request
        int responseCode = connection.getResponseCode();
        
        // We expect a 401 Unauthorized response
        assertEquals(401, responseCode, "Should get 401 Unauthorized with invalid token");
    }
    
    @Test
    void testProtectedEndpointWithNoToken() throws IOException {
        // Start the server
        startServer();
        
        // Create a request to a protected endpoint
        URL url = new URL("http://localhost:" + TEST_PORT + "/api/folders");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        // Don't add any token
        
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