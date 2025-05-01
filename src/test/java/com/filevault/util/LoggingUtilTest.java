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
 * Test class for LoggingUtil.
 */
@TestInstance(Lifecycle.PER_CLASS)
public class LoggingUtilTest {

    private static final String TEST_LOG_FILE = "logs/test_log.log";

    @BeforeEach
    void setUp() {
        // Set the log file path to a test-specific file
        LoggingUtil.setLogFilePath(TEST_LOG_FILE);
    }

    @AfterEach
    void tearDown() {
        LoggingUtil.disableLogging();

        Path logFilePath = Paths.get(TEST_LOG_FILE);
        try {
            if (Files.exists(logFilePath)) {
                Files.delete(logFilePath);
            }
        } catch (IOException e) {
            fail("Failed to delete test log file: " + e.getMessage());
        }
    }

    @Test
    void testLogRingBufferBehavior() throws IOException {
        this.setUp();
        // Log more messages than the ring buffer capacity
        for (int i = 1; i <= 15; i++) {
            LoggingUtil.log("Test log message " + i);
        }

        // Verify the log file contains only the most recent messages
        Path logFilePath = Paths.get(TEST_LOG_FILE);
        assertTrue(Files.exists(logFilePath), "Log file should exist");

        List<String> logLines = Files.readAllLines(logFilePath);
        assertEquals(10, logLines.size(), "Log file should contain 10 most recent messages");

        // Verify the content of the log file
        for (int i = 0; i < 10; i++) {
            assertEquals("Test log message " + (i + 6), logLines.get(i), "Log file content mismatch");
        }
        this.tearDown();
    }
}