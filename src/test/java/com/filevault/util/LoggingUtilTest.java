package com.filevault.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

/**
 * Testklasse für LoggingUtil.
 * Diese Klasse testet die Funktionalität des Logging-Systems, insbesondere das Ringpuffer-Verhalten.
 */
@TestInstance(Lifecycle.PER_CLASS)
public class LoggingUtilTest {

    private static final String TEST_LOG_FILE = "logs/test_log.log";

    /**
     * Initialisiert die Testumgebung vor jedem Test.
     * Setzt den Pfad für die Logdatei auf eine testspezifische Datei.
     */
    @BeforeEach
    void setUp() {
        // Set the log file path to a test-specific file
        LoggingUtil.setLogFilePath(TEST_LOG_FILE);
    }

    /**
     * Bereinigt die Testumgebung nach jedem Test.
     * Deaktiviert das Logging und löscht die Testlogdatei.
     */
    @AfterEach
    void tearDown() {
        LoggingUtil.disableLogging();

        Path logFilePath = Paths.get(TEST_LOG_FILE);
        try {
            if (Files.exists(logFilePath)) {
                Files.delete(logFilePath);
            }
        } catch (IOException e) {
            fail("Fehler beim Löschen der Testlogdatei: " + e.getMessage());
        }
    }

    /**
     * Testet das Ringpuffer-Verhalten des LoggingUtil.
     * Überprüft, ob bei Überschreitung der Kapazität nur die neuesten Nachrichten gespeichert werden.
     */
    @Test
    void testLogRingBufferBehavior() throws IOException {
        this.setUp();

        // Clear the log file to ensure a clean state for the test
        Path logFilePath = Paths.get(TEST_LOG_FILE);
        if (Files.exists(logFilePath)) {
            Files.delete(logFilePath);
        }

        int ringBufferCapacity = LoggingUtil.getRingBufferCapacity();
        // Log more messages than the ring buffer capacity
        for (int i = 1; i <= ringBufferCapacity + 5; i++) {
            LoggingUtil.log("Test log message " + i);
        }

        // Verify the log file contains only the most recent messages
        logFilePath = Paths.get(TEST_LOG_FILE);
        assertTrue(Files.exists(logFilePath), "Logdatei sollte existieren");

        List<String> logLines = Files.readAllLines(logFilePath);
        assertEquals(ringBufferCapacity, logLines.size(), "Logdatei sollte nur die neuesten Nachrichten enthalten");

        // Verify the content of the log file
        for (int i = 0; i < ringBufferCapacity; i++) {
            String expectedMessage = "Test log message " + (i + 6);
            String actualMessage = logLines.get(i);

            // Extract the actual log message after the timestamp
            String actualMessageContent = actualMessage.substring(actualMessage.indexOf(" ") + 1);
            assertEquals(expectedMessage, actualMessageContent, "Inhalt der Logdatei stimmt nicht überein");
        }
        this.tearDown();
    }
}