package com.filevault.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Utility-Klasse für das Logging in einem benutzerdefinierten Ringpuffer.
 */
public class LoggingUtil {

    private static final int RING_BUFFER_CAPACITY = 500;
    private static final ArrayBlockingQueue<String> ringBuffer = new ArrayBlockingQueue<>(RING_BUFFER_CAPACITY);
    private static final LinkedList<String> fileRingBuffer = new LinkedList<>();
    private static final String LOG_FILE_PATH = "logs/filevault.log";
    private static String logFilePath = LOG_FILE_PATH;
    private static boolean loggingEnabled = true;
    private static final Logger logger = Logger.getLogger("com.filevault");
    private static Handler fileHandler;
    private static final String LOG_FILE = "filevault.log";

    static {
        try {
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Erstellen des Log-Verzeichnisses: " + e.getMessage());
        }
    }

    /**
     * Sets the log file path for testing purposes.
     *
     * @param path The new log file path.
     */
    public static void setLogFilePath(String path) {
        logFilePath = path;
    }

    /**
     * Disables logging.
     */
    public static void disableLogging() {
        loggingEnabled = false;
    }

    /**
     * Enables logging.
     */
    public static void enableLogging() {
        loggingEnabled = true;
    }

    /**
     * Fügt eine Log-Nachricht zum Ringpuffer hinzu und schreibt sie in die Logdatei.
     *
     * @param message Die zu loggende Nachricht
     */
    public static void log(String message) {
        if (!loggingEnabled) {
            return;
        }

        String currentTime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS"));
        String timestampedMessage = currentTime + " " + message;
        
        if (!ringBuffer.offer(timestampedMessage)) {
            ringBuffer.poll();
            ringBuffer.offer(timestampedMessage);
        }

        synchronized (fileRingBuffer) {
            fileRingBuffer.add(timestampedMessage);
            if (fileRingBuffer.size() > RING_BUFFER_CAPACITY) {
                fileRingBuffer.removeFirst();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, false))) {
                for (String logMessage : fileRingBuffer) {
                    writer.write(logMessage);
                    writer.newLine();
                }
            } catch (IOException e) {
                System.err.println("Error writing to log file: " + e.getMessage());
            }
        }
    }

    /**
     * Logs an informational message.
     *
     * @param className The name of the class where the log is generated.
     * @param message   The message to log.
     */
    public static void logInfo(String className, String message) {
        log("INFO: [" + className + "] " + message);
    }

    /**
     * Logs an error message.
     *
     * @param className The name of the class where the log is generated.
     * @param message   The message to log.
     */
    public static void logError(String className, String message) {
        log("ERROR: [" + className + "] " + message);
    }

    /**
     * Logs a severe error message.
     *
     * @param className The name of the class where the log is generated.
     * @param message   The message to log.
     */
    public static void logSevere(String className, String message) {
        log("SEVERE: [" + className + "] " + message);
    }

    /**
     * Logs a warning message.
     *
     * @param className The name of the class where the log is generated.
     * @param message   The message to log.
     */
    public static void logWarning(String className, String message) {
        log("WARNING: [" + className + "] " + message);
    }

    /**
     * Logs a database-related message.
     *
     * @param operation The database operation (e.g., "Get", "Put").
     * @param target    The target of the operation (e.g., "File", "Folder").
     * @param message   The message to log.
     */
    public static void logDatabase(String operation, String target, String message) {
        log("DATABASE: [" + operation + " " + target + "] " + message);
    }

    /**
     * Gibt alle Log-Nachrichten im Ringpuffer zurück.
     *
     * @return Ein Array mit allen Log-Nachrichten
     */
    public static String[] getLogs() {
        return ringBuffer.toArray(String[]::new);
    }

    /**
     * Returns the maximum capacity of the ring buffer.
     *
     * @return The ring buffer capacity.
     */
    public static int getRingBufferCapacity() {
        return RING_BUFFER_CAPACITY;
    }

    /**
     * Configures the logging system with file and console handlers.
     */
    public static void configureLogger() {
        try {
            logger.setLevel(Level.ALL);
            
            // Create log directory if it doesn't exist
            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdir();
            }
            
            // Create custom formatter
            SimpleFormatter customFormatter = new SimpleFormatter() {
                @Override
                public String format(java.util.logging.LogRecord record) {
                    java.time.LocalDateTime datetime = java.time.LocalDateTime.now();
                    String timestamp = datetime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS"));
                    return timestamp + " " + record.getMessage() + System.lineSeparator();
                }
            };
            
            // Remove any existing handlers
            if (fileHandler != null) {
                logger.removeHandler(fileHandler);
                fileHandler.close();
            }
            
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }
            
            // Configure single file handler without rotation (no limit on file count)
            fileHandler = new FileHandler("logs/" + LOG_FILE, 0, 1, true);
            fileHandler.setFormatter(customFormatter);
            fileHandler.setLevel(Level.ALL);
            logger.addHandler(fileHandler);
            
            // Configure console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(customFormatter);
            consoleHandler.setLevel(Level.INFO);
            logger.addHandler(consoleHandler);
            
            logger.info("Logging system initialized");
        } catch (IOException e) {
            System.err.println("Error setting up logger: " + e.getMessage());
            e.printStackTrace();
        }
    }
}