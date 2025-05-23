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
 * Testklasse für die AuthMiddleware-Funktionalität mit echten HTTP-Anfragen.
 * Diese Tests überprüfen die Token-basierte Authentifizierung für geschützte Endpunkte.
 */
class AuthMiddlewareTest {

    /** Die zu testende ApiServer-Instanz */
    private ApiServer apiServer;
    
    /** Der Port für den Test-Server (unterschiedlich zu ApiServerTest) */
    private final int TEST_PORT = 8766;
    
    /** ExecutorService für das Ausführen des Servers in einem separaten Thread */
    private ExecutorService executorService;
    
    /** Ein gültiges Token für Testzwecke */
    private String validToken;

    /**
     * Initialisiert die Testumgebung vor jedem Test.
     * Erstellt eine neue ApiServer-Instanz und generiert ein gültiges Token.
     */
    @BeforeEach
    void setUp() throws Exception {
        apiServer = new ApiServer();
        executorService = Executors.newSingleThreadExecutor();
        
        // Generiere ein gültiges Token für Tests
        validToken = ApiServer.TokenManager.generateToken("testuser");
    }

    /**
     * Bereinigt die Testumgebung nach jedem Test.
     * Stoppt den Server, beendet den ExecutorService und invalidiert das Testtoken.
     */
    @AfterEach
    void tearDown() throws InterruptedException {
        if (apiServer != null) {
            apiServer.stop();
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        
        // Bereinige das Token
        ApiServer.TokenManager.invalidateToken(validToken);
    }

    /**
     * Testet die Funktionen des TokenManagers.
     * Überprüft die Generierung und Invalidierung von Tokens.
     */
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
    
    /**
     * Testet einen geschützten Endpunkt mit einem gültigen Token.
     * Überprüft, ob der Zugriff mit einem gültigen Token erlaubt wird.
     */
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
    
    /**
     * Testet einen geschützten Endpunkt mit einem ungültigen Token.
     * Überprüft, ob der Zugriff mit einem ungültigen Token verweigert wird.
     */
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
    
    /**
     * Testet einen geschützten Endpunkt ohne Token.
     * Überprüft, ob der Zugriff ohne Token verweigert wird.
     */
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
     * Hilfsmethode zum Starten des Servers und Warten, bis er bereit ist.
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