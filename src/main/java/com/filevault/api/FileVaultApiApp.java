package com.filevault.api;

import java.io.IOException;

import com.filevault.storage.DatabaseManager;

/**
 * Hauptklasse zum Starten des FileVault API-Servers.
 */
public class FileVaultApiApp {

    /**
     * Startpunkt der Anwendung.
     *
     * @param args Argumente, wobei das erste Argument der Port ist (optional).
     */
    public static void main(String[] args) {
        int port = 8081; // Standardport

        // Überprüfen, ob ein Port als Argument übergeben wurde
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Ungültiger Port angegeben. Standardport 8081 wird verwendet.");
            }
        }

        // Initialisiere die Datenbank
        DatabaseManager.initDatabase();

        ApiServer server = new ApiServer();
        try {
            server.start(port);

            // Shutdown-Hook registrieren, um den Server beim Beenden zu stoppen
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        } catch (IOException e) {
            System.err.println("Fehler beim Starten des API-Servers: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Laufzeitfehler beim Starten des API-Servers: " + e.getMessage());
        }
    }
}
