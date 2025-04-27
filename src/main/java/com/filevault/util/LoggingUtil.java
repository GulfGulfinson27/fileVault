package com.filevault.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Utility-Klasse für das Logging in Dateien mit Ringpuffer-Mechanismus.
 */
public class LoggingUtil {

    private static final int MAX_LOG_FILES = 1;
    private static final int MAX_FILE_SIZE = 100 * 100; // 1 MB
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE_PREFIX = "filevault_log";

    private static final Logger logger = Logger.getLogger("FileVaultLogger");

    static {
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
            setupLogger();
        } catch (IOException e) {
            System.err.println("Fehler beim Initialisieren des Loggers: " + e.getMessage());
        }
    }

    /**
     * Initialisiert den Logger mit einem Rotating-File-Handler.
     */
    private static void setupLogger() throws IOException {
        logger.setUseParentHandlers(false);

        String logFileName = LOG_DIR + "/" + LOG_FILE_PREFIX + ".log";

        // Configure the FileHandler to use a rotating log mechanism
        FileHandler fileHandler = new FileHandler(logFileName, MAX_FILE_SIZE, MAX_LOG_FILES, true) {
            @Override
            public synchronized void publish(java.util.logging.LogRecord record) {
                super.publish(record);
                flush(); // Ensure logs are written immediately
            }
        };

        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);

        // Ensure all loggers write to the same file
        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(fileHandler);
    }

    /**
     * Loggt eine Nachricht auf INFO-Level.
     *
     * @param message Die zu loggende Nachricht
     */
    public static void logInfo(String message) {
        logger.info(message);
    }

    /**
     * Loggt eine Nachricht auf WARN-Level.
     *
     * @param message Die zu loggende Nachricht
     */
    public static void logWarning(String message) {
        logger.warning(message);
    }

    /**
     * Loggt eine Nachricht auf SEVERE-Level.
     *
     * @param message Die zu loggende Nachricht
     */
    public static void logSevere(String message) {
        logger.severe(message);
    }
}