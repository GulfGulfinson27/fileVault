package com.filevault.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.filevault.model.EncryptedFile;
import com.filevault.model.VirtualFolder;
import com.filevault.storage.DatabaseManager;
import com.filevault.storage.FileStorage;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Einfache API-Server-Klasse, die GET-Anfragen verarbeitet.
 */
public class ApiServer {

    private HttpServer server;

    /**
     * Startet den API-Server auf dem angegebenen Port.
     *
     * @param port Der Port, auf dem der Server gestartet wird.
     * @throws IOException Wenn ein Fehler beim Starten des Servers auftritt.
     */
    public void start(int port) throws IOException {
        Logger logger = Logger.getLogger(ApiServer.class.getName());

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/auth", new AuthHandler());
        logger.info("Kontext /api/auth registriert.");

        server.createContext("/api/info", new AuthMiddleware(new InfoHandler()));
        logger.info("Kontext /api/info mit Authentifizierung registriert.");

        server.createContext("/api/folders", new AuthMiddleware(new FoldersHandler()));
        logger.info("Kontext /api/folders mit Authentifizierung registriert.");

        server.createContext("/api/files", new AuthMiddleware(new FileHandler()));
        logger.info("Kontext /api/files mit Authentifizierung registriert.");

        server.createContext("/", new WebInterfaceHandler());
        logger.info("Kontext / für Web-Interface registriert.");

        server.setExecutor(null); // Standard-Executor
        server.start();
        logger.log(Level.INFO, "API-Server gestartet auf Port {0}", port);
    }

    /**
     * Stoppt den API-Server.
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * Ein einfacher In-Memory-Token-Manager zur Verwaltung von Authentifizierungs-Token.
     */
    static class TokenManager {
        private static final Map<String, String> tokenStore = new HashMap<>();

        public static String generateToken(String username) {
            String token = UUID.randomUUID().toString();
            tokenStore.put(token, username);
            return token;
        }

        public static boolean isValidToken(String token) {
            return tokenStore.containsKey(token);
        }

        public static void invalidateToken(String token) {
            tokenStore.remove(token);
        }
    }

    /**
     * Handler für Authentifizierungsanfragen.
     */
    static class AuthHandler implements HttpHandler {
        private static final String MASTER_PASSWORD = "11111111";

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Logger logger = Logger.getLogger(AuthHandler.class.getName());
            logger.info("Verarbeite Authentifizierungsanfrage...");

            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                logger.info("Anfrageinhalt: " + requestBody);

                if (requestBody.contains(MASTER_PASSWORD)) {
                    String token = TokenManager.generateToken("user");
                    String response = String.format("{\"token\":\"%s\"}", token);
                    logger.info("Token generiert: " + token);

                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } else {
                    String response = "{\"error\":\"Ungültiges Passwort. Zugriff verweigert.\"}";
                    logger.warning("Authentifizierung fehlgeschlagen: Ungültiges Passwort.");
                    exchange.sendResponseHeaders(401, response.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            } else {
                logger.warning("HTTP-Methode nicht erlaubt: " + exchange.getRequestMethod());
                exchange.sendResponseHeaders(405, -1); // Methode nicht erlaubt
            }
        }
    }

    /**
     * Middleware zur Überprüfung von gültigen Authentifizierungs-Token.
     */
    static class AuthMiddleware implements HttpHandler {
        private final HttpHandler next;

        public AuthMiddleware(HttpHandler next) {
            this.next = next;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Logger logger = Logger.getLogger(AuthMiddleware.class.getName());
            logger.info("Überprüfe Authentifizierungs-Token...");

            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader != null && TokenManager.isValidToken(authHeader)) {
                logger.info("Token gültig. Anfrage wird weitergeleitet.");
                next.handle(exchange);
            } else {
                logger.warning("Ungültiges oder fehlendes Token.");
                exchange.sendResponseHeaders(401, -1); // Nicht autorisiert
            }
        }
    }

    /**
     * Handler für die Verarbeitung von GET-Anfragen an /api/info.
     */
    static class InfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Logger logger = Logger.getLogger(InfoHandler.class.getName());
            logger.info("Verarbeite Anfrage an /api/info...");

            if ("GET".equals(exchange.getRequestMethod())) {
                String response = "Willkommen bei der FileVault API!";
                logger.info("Antwort: " + response);

                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                logger.warning("HTTP-Methode nicht erlaubt: " + exchange.getRequestMethod());
                exchange.sendResponseHeaders(405, -1); // Methode nicht erlaubt
            }
        }
    }

    /**
     * Handler für die Verarbeitung von Anfragen an /api/folders.
     */
    static class FoldersHandler implements HttpHandler {
        private static final Logger logger = Logger.getLogger(FoldersHandler.class.getName());

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            logger.info("Verarbeite Anfrage an /api/folders mit Methode: " + method);

            String response;

            switch (method) {
                case "GET" -> {
                    response = listFolders();
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                }
                case "POST" -> {
                    response = createFolder(exchange);
                    exchange.sendResponseHeaders(201, response.getBytes().length);
                }
                case "PUT" -> {
                    response = updateFolder(exchange);
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                }
                case "DELETE" -> {
                    response = deleteFolder(exchange);
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                }
                default -> {
                    response = "Methode nicht erlaubt.";
                    logger.warning("Methode nicht erlaubt: " + method);
                    exchange.sendResponseHeaders(405, response.getBytes().length);
                }
            }

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }

        private String listFolders() {
            logger.info("Liste alle Ordner auf...");
            StringBuilder response = new StringBuilder("[");
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM folders");
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    if (response.length() > 1) {
                        response.append(",");
                    }
                    response.append(String.format("{\"id\":%d,\"name\":\"%s\"}", rs.getInt("id"), rs.getString("name")));
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Datenbankfehler: {0}", e.getMessage());
                return "Datenbankfehler: " + e.getMessage();
            }
            response.append("]");
            logger.info("Ordnerliste erstellt: " + response);
            return response.toString();
        }

        private String createFolder(HttpExchange exchange) {
            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                logger.info("Empfangene Anfrage zum Erstellen eines Ordners: " + requestBody);

                // Parse JSON to extract folder details
                String folderName = parseJson(requestBody, "name");

                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("INSERT INTO folders (name) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS)) {

                    stmt.setString(1, folderName);
                    stmt.executeUpdate();

                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int folderId = generatedKeys.getInt(1);
                            logger.info("Ordner erstellt mit ID: " + folderId);
                            return String.format("{\"id\":%d,\"name\":\"%s\"}", folderId, folderName);
                        } else {
                            throw new SQLException("Erstellen des Ordners fehlgeschlagen, keine ID erhalten.");
                        }
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Fehler beim Erstellen des Ordners: {0}", e.getMessage());
                return "Fehler beim Erstellen des Ordners: " + e.getMessage();
            }
        }

        private String updateFolder(HttpExchange exchange) {
            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                logger.info("Empfangene Anfrage zum Aktualisieren eines Ordners: " + requestBody);

                // Parse JSON to extract folder details
                int folderId = Integer.parseInt(parseJson(requestBody, "id"));
                String folderName = parseJson(requestBody, "name");

                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("UPDATE folders SET name = ? WHERE id = ?")) {

                    stmt.setString(1, folderName);
                    stmt.setInt(2, folderId);

                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        logger.info("Ordner erfolgreich aktualisiert: ID=" + folderId);
                        return String.format("{\"id\":%d,\"name\":\"%s\"}", folderId, folderName);
                    } else {
                        return "Ordner nicht gefunden.";
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Fehler beim Aktualisieren des Ordners: {0}", e.getMessage());
                return "Fehler beim Aktualisieren des Ordners: " + e.getMessage();
            }
        }

        private String deleteFolder(HttpExchange exchange) {
            try {
                String query = exchange.getRequestURI().getQuery();
                int folderId = Integer.parseInt(query.split("=")[1]);
                logger.info("Empfangene Anfrage zum Löschen des Ordners mit ID: " + folderId);

                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM folders WHERE id = ?")) {

                    stmt.setInt(1, folderId);

                    int rowsDeleted = stmt.executeUpdate();
                    if (rowsDeleted > 0) {
                        logger.info("Ordner erfolgreich gelöscht: ID=" + folderId);
                        return "Ordner erfolgreich gelöscht.";
                    } else {
                        return "Ordner nicht gefunden.";
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Fehler beim Löschen des Ordners: {0}", e.getMessage());
                return "Fehler beim Löschen des Ordners: " + e.getMessage();
            }
        }

        private String parseJson(String json, String key) {
            // Simple JSON parsing logic (replace with a proper library like Jackson or Gson in production)
            String searchKey = "\"" + key + "\":";
            int startIndex = json.indexOf(searchKey) + searchKey.length();
            int endIndex = json.indexOf(",", startIndex);
            if (endIndex == -1) {
                endIndex = json.indexOf("}", startIndex);
            }
            return json.substring(startIndex, endIndex).replaceAll("\"", "").trim();
        }
    }

    /**
     * Handler für Dateioperationen.
     */
    static class FileHandler implements HttpHandler {
        private static final Logger logger = Logger.getLogger(FileHandler.class.getName());

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            logger.info("Verarbeite Anfrage an /api/files mit Methode: " + method);

            String response;

            switch (method) {
                case "GET" -> {
                    response = listFiles();
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                }
                case "POST" -> {
                    response = uploadFile(exchange);
                    exchange.sendResponseHeaders(201, response.getBytes().length);
                }
                case "PUT" -> {
                    response = updateFile(exchange);
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                }
                case "DELETE" -> {
                    response = deleteFile(exchange);
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                }
                default -> {
                    response = "Methode nicht erlaubt.";
                    logger.warning("Methode nicht erlaubt: " + method);
                    exchange.sendResponseHeaders(405, response.getBytes().length);
                }
            }

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }

        private String listFiles() {
            try {
                FileStorage fileStorage = FileStorage.getInstance();
                logger.info("Rufe alle Dateien ab...");

                // Fetch all files instead of limiting to the root folder
                List<EncryptedFile> files = fileStorage.getAllFiles();

                if (files.isEmpty()) {
                    logger.info("Keine Dateien gefunden.");
                    return "[]";
                }

                logger.info("Anzahl der gefundenen Dateien: " + files.size());
                StringBuilder response = new StringBuilder("[");

                for (EncryptedFile file : files) {
                    logger.info("Datei gefunden: ID=" + file.getId() + ", Name=" + file.getOriginalName());
                    if (response.length() > 1) {
                        response.append(",");
                    }
                    response.append(String.format("{\"id\":%d,\"name\":\"%s\"}",
                            file.getId(), file.getOriginalName()));
                }

                response.append("]");
                return response.toString();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Fehler beim Auflisten der Dateien: {0}", e.getMessage());
                return "Fehler beim Auflisten der Dateien: " + e.getMessage();
            }
        }

        private String uploadFile(HttpExchange exchange) {
            try {
                // Parse the request body to get file data (placeholder logic)
                byte[] fileData = exchange.getRequestBody().readAllBytes();

                File tempFile = File.createTempFile("upload", null);
                Files.write(tempFile.toPath(), fileData);

                FileStorage fileStorage = FileStorage.getInstance();
                // Updated to use the correct VirtualFolder constructor
                EncryptedFile encryptedFile = fileStorage.importFile(tempFile, new VirtualFolder(0, "Root", "Root folder", null));

                String response = String.format("Datei '%s' erfolgreich hochgeladen mit ID %d.",
                        encryptedFile.getOriginalName(), encryptedFile.getId());
                logger.info(response);
                return response;
            } catch (Exception e) {
                String errorMessage = "Fehler beim Hochladen der Datei: " + e.getMessage();
                logger.log(Level.SEVERE, errorMessage);
                return errorMessage;
            }
        }

        private String updateFile(HttpExchange exchange) {
            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                logger.info("Empfangene Anfrage zum Aktualisieren einer Datei: " + requestBody);
                // Parse JSON and update file logic here
                return "Datei erfolgreich aktualisiert.";
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Fehler beim Aktualisieren der Datei: {0}", e.getMessage());
                return "Fehler beim Aktualisieren der Datei: " + e.getMessage();
            }
        }

        private String deleteFile(HttpExchange exchange) {
            try {
                // Extract file ID from the query parameters (placeholder logic)
                String query = exchange.getRequestURI().getQuery();
                int fileId = Integer.parseInt(query.split("=")[1]);
                logger.info("Empfangene Anfrage zum Löschen der Datei mit ID: " + fileId);

                FileStorage fileStorage = FileStorage.getInstance();
                EncryptedFile fileToDelete = fileStorage.getFileById(fileId);

                if (fileToDelete == null) {
                    String message = String.format("Datei mit ID %d nicht gefunden.", fileId);
                    logger.warning(message);
                    return message;
                }

                boolean success = fileStorage.deleteFile(fileToDelete);

                if (success) {
                    String message = String.format("Datei mit ID %d erfolgreich gelöscht.", fileId);
                    logger.info(message);
                    return message;
                } else {
                    String message = String.format("Löschen der Datei mit ID %d fehlgeschlagen.", fileId);
                    logger.warning(message);
                    return message;
                }
            } catch (Exception e) {
                String errorMessage = "Fehler beim Löschen der Datei: " + e.getMessage();
                logger.log(Level.SEVERE, errorMessage);
                return errorMessage;
            }
        }
    }

    /**
     * Handler für das Web-Interface.
     */
    static class WebInterfaceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String htmlResponse = """
                    <!DOCTYPE html>
                    <html lang=\"en\">
                    <head>
                        <meta charset=\"UTF-8\">
                        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">
                        <title>FileVault Web Interface</title>
                        <script>
                            let token = null;

                            async function authenticate() {
                                const password = document.getElementById('password').value;
                                const response = await fetch('/api/auth', {
                                    method: 'POST',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify({ password })
                                });

                                if (response.ok) {
                                    const data = await response.json();
                                    token = data.token;
                                    alert('Authentication successful!');
                                } else {
                                    alert('Authentication failed!');
                                }
                            }

                            async function callEndpoint(endpoint) {
                                if (!token) {
                                    alert('Please authenticate first!');
                                    return;
                                }

                                const response = await fetch(endpoint, {
                                    method: 'GET',
                                    headers: { 'Authorization': token }
                                });

                                if (response.ok) {
                                    const data = await response.text();
                                    alert(`Response from ${endpoint}:\n${data}`);
                                } else {
                                    alert(`Failed to access ${endpoint}.`);
                                }
                            }
                        </script>
                    </head>
                    <body>
                        <h1>FileVault Web Interface</h1>
                        <div>
                            <label for=\"password\">Password:</label>
                            <input type=\"password\" id=\"password\" />
                            <button onclick=\"authenticate()\">Authenticate</button>
                        </div>
                        <div>
                            <button onclick=\"callEndpoint('/api/info')\">Get Info</button>
                            <button onclick=\"callEndpoint('/api/folders')\">List Folders</button>
                            <button onclick=\"callEndpoint('/api/files')\">List Files</button>
                        </div>
                    </body>
                    </html>
                """;

                exchange.sendResponseHeaders(200, htmlResponse.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(htmlResponse.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }
}