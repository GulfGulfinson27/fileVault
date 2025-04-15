package com.filevault.model;

import com.filevault.storage.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für die UserManager-Klasse.
 * Diese Klasse testet die Benutzerverwaltung und Authentifizierung.
 */
public class UserManagerTest {
    
    private static final String TEST_PASSWORD = "TestPasswort123!";
    private UserManager userManager;
    private String testDbPath;
    
    /**
     * Initialisiert die Testumgebung vor jedem Test.
     * Erstellt eine neue Testdatenbank und initialisiert den UserManager.
     */
    @BeforeEach
    public void setUp() {
        // Testdatenbank initialisieren
        testDbPath = Paths.get(System.getProperty("user.home"), ".filevault", "test_vault.db").toString();
        DatabaseManager.initDatabase(true);
        userManager = UserManager.getInstance();
        userManager.logout(); // Reset the singleton instance
    }
    
    /**
     * Bereinigt die Testumgebung nach jedem Test.
     * Löscht die Testdatenbank und alle temporären Dateien.
     */
    @AfterEach
    public void tearDown() {
        // Datenbankverbindungen schließen
        DatabaseManager.closeConnections();
        
        // Testdatenbank löschen
        File dbFile = new File(testDbPath);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        
        // Temporäre Dateien löschen
        File tempDir = new File("temp");
        if (tempDir.exists() && tempDir.isDirectory()) {
            deleteDirectory(tempDir);
        }
        
        // Sicherstellen, dass der UserManager zurückgesetzt ist
        userManager.logout();
    }
    
    /**
     * Hilfsmethode zum rekursiven Löschen eines Verzeichnisses.
     * 
     * @param directory Das zu löschende Verzeichnis
     */
    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
    
    /**
     * Testet die Singleton-Implementierung des UserManagers.
     * Überprüft, ob immer die gleiche Instanz zurückgegeben wird.
     */
    @Test
    public void testSingleton() {
        UserManager instance1 = UserManager.getInstance();
        UserManager instance2 = UserManager.getInstance();
        assertSame(instance1, instance2);
    }
    
    /**
     * Testet die Benutzererstellung und Authentifizierung.
     * Überprüft, ob ein neuer Benutzer erstellt und authentifiziert werden kann.
     */
    @Test
    public void testCreateAndAuthenticateUser() {
        // Überprüfen, dass noch kein Benutzer existiert
        assertFalse(userManager.userExists());
        
        // Benutzer erstellen
        assertTrue(userManager.createUser(TEST_PASSWORD));
        assertTrue(userManager.userExists());
        
        // Authentifizierung testen
        assertTrue(userManager.authenticate(TEST_PASSWORD));
        assertEquals("master", userManager.getCurrentUser());
        assertNotNull(userManager.getMasterKey());
        
        // Falsches Passwort testen
        assertFalse(userManager.authenticate("wrongpassword"));
    }
    
    /**
     * Testet die Passwortänderung.
     * Überprüft, ob das Passwort erfolgreich geändert werden kann.
     */
    @Test
    public void testChangePassword() {
        // Benutzer erstellen
        userManager.createUser(TEST_PASSWORD);
        
        // Mit altem Passwort authentifizieren
        assertTrue(userManager.authenticate(TEST_PASSWORD));
        
        // Passwort ändern
        String newPassword = "NeuesPasswort123!";
        assertTrue(userManager.changePassword(TEST_PASSWORD, newPassword));
        
        // Mit neuem Passwort authentifizieren
        assertTrue(userManager.authenticate(newPassword));
        
        // Mit altem Passwort sollte es nicht mehr funktionieren
        assertFalse(userManager.authenticate(TEST_PASSWORD));
    }
    
    /**
     * Testet das Abmelden des Benutzers.
     * Überprüft, ob alle Benutzerdaten zurückgesetzt werden.
     */
    @Test
    public void testLogout() {
        // Benutzer erstellen und anmelden
        userManager.createUser(TEST_PASSWORD);
        userManager.authenticate(TEST_PASSWORD);
        
        // Abmelden
        userManager.logout();
        assertNull(userManager.getCurrentUser());
        assertNull(userManager.getMasterKey());
    }
    
    /**
     * Testet die Benutzerprüfung.
     * Überprüft, ob korrekt erkannt wird, ob ein Benutzer existiert.
     */
    @Test
    public void testUserExists() {
        // Überprüfen, dass noch kein Benutzer existiert
        assertFalse(userManager.userExists());
        
        // Benutzer erstellen
        userManager.createUser(TEST_PASSWORD);
        
        // Überprüfen, dass der Benutzer jetzt existiert
        assertTrue(userManager.userExists());
    }
} 