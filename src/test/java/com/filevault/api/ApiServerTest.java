package com.filevault.api;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testklasse für den ApiServer.
 * Diese Klasse testet die grundlegende Funktionalität des API-Servers,
 * einschließlich Start, Stop, Change-Listener und Authentifizierungs-Endpunkt.
 */
class ApiServerTest {

    /** Die zu testende ApiServer-Instanz */
    private ApiServer apiServer;
    
    /** Der Port für den Test-Server */
    private final int TEST_PORT = 8765;
    
    /** ExecutorService für das Ausführen des Servers in einem separaten Thread */
    private ExecutorService executorService;
    
    /**
     * Initialisiert die Testumgebung vor jedem Test.
     * Erstellt eine neue ApiServer-Instanz und einen ExecutorService.
     */
    @BeforeEach
    void setUp() {
        apiServer = new ApiServer();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Bereinigt die Testumgebung nach jedem Test.
     * Stoppt den Server und beendet den ExecutorService.
     */
    @AfterEach
    void tearDown() throws InterruptedException {
        if (apiServer != null) {
            apiServer.stop();
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }
    
    /**
     * Testet das Starten und Stoppen des Servers.
     * Überprüft, ob der Server erfolgreich gestartet werden kann und auf Anfragen reagiert.
     */
    @Test
    void testStartAndStop() throws IOException {
        // Start the server in a separate thread
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
        
        // Test that the server is running by making a request
        try {
            URL url = new URL("http://localhost:" + TEST_PORT + "/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            
            // We expect a response (even if it's an error)
            assertTrue(responseCode > 0);
            
        } catch (IOException e) {
            fail("Failed to connect to server: " + e.getMessage());
        }
        
        // Stop the server
        apiServer.stop();
    }
    
    /**
     * Testet das Hinzufügen und Entfernen von Change-Listenern.
     * Überprüft, ob Listener korrekt registriert, benachrichtigt und entfernt werden können.
     */
    @Test
    void testAddAndRemoveChangeListener() {
        // Use a CountDownLatch to verify that the listener was called
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean listenerCalled = new AtomicBoolean(false);
        
        // Create a real listener
        Consumer<String> listener = action -> {
            listenerCalled.set(true);
            latch.countDown();
        };
        
        // Add the listener
        ApiServer.addChangeListener(listener);
        
        // Call the notifyChangeListeners method using reflection
        try {
            java.lang.reflect.Method notifyMethod = ApiServer.class.getDeclaredMethod("notifyChangeListeners", String.class);
            notifyMethod.setAccessible(true);
            notifyMethod.invoke(null, "test_action");
            
            // Wait for the listener to be called
            boolean called = latch.await(2, TimeUnit.SECONDS);
            assertTrue(called, "Listener should have been called");
            assertTrue(listenerCalled.get(), "Listener should have been called with the action");
            
            // Reset for next test
            listenerCalled.set(false);
            
            // Remove the listener
            ApiServer.removeChangeListener(listener);
            
            // Call notify again - this time the listener should not be called
            notifyMethod.invoke(null, "another_action");
            
            // Wait a bit to make sure the listener is not called
            Thread.sleep(500);
            assertFalse(listenerCalled.get(), "Listener should not have been called after removal");
            
        } catch (Exception e) {
            fail("Failed to test listeners: " + e.getMessage());
        }
    }
    
    /**
     * Testet den Authentifizierungs-Endpunkt.
     * Überprüft, ob der Server auf POST-Anfragen an den Auth-Endpunkt reagiert.
     */
    @Test
    void testAuthEndpoint() throws IOException {
        // Start the server
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
        
        // Create a POST request to the auth endpoint
        URL url = new URL("http://localhost:" + TEST_PORT + "/api/auth");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        
        // Send a password in the request body
        // Note: This is a test password, in a real test we would need to ensure
        // the UserManager is properly initialized with this password
        String jsonInput = "{\"password\":\"test_password\"}";
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // Read the response
        int responseCode = connection.getResponseCode();
        
        // We're just testing that the endpoint responds, not necessarily with success
        // since we don't have a way to set up the UserManager with a valid password in this test
        assertTrue(responseCode == 200 || responseCode == 401, 
                "Response code should be either 200 (success) or 401 (unauthorized)");
    }
    
    /**
     * Testet den Authentifizierungs-Endpunkt mit einer ungültigen HTTP-Methode.
     * Überprüft, ob der Server GET-Anfragen an den Auth-Endpunkt mit einem 405-Fehler ablehnt.
     */
    @Test
    void testAuthEndpointInvalidMethod() throws IOException {
        // Start the server
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
        
        // Create a GET request to the auth endpoint (should be rejected as only POST is allowed)
        URL url = new URL("http://localhost:" + TEST_PORT + "/api/auth");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        // Read the response
        int responseCode = connection.getResponseCode();
        
        // We expect a 405 Method Not Allowed response
        assertEquals(405, responseCode, "Response code should be 405 (Method Not Allowed)");
    }
} 