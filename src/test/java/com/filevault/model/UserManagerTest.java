package com.filevault.model;

import com.filevault.storage.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für den UserManager.
 * Testet die Benutzerverwaltung, Authentifizierung und Passwortänderung.
 */
public class UserManagerTest {
    private static final String TEST_PASSWORD = "TestPasswort123!";
    private UserManager userManager;

    /**
     * Initialisiert die Testdatenbank und den UserManager vor jedem Test.
     * Stellt sicher, dass kein Benutzer existiert.
     */
    @BeforeEach
    void setUp() {
        // Testdatenbank initialisieren
        DatabaseManager.initDatabase(true);
        userManager = UserManager.getInstance();
    }

    /**
     * Bereinigt nach jedem Test.
     * Schließt Datenbankverbindungen und löscht die Testdatenbank.
     */
    @AfterEach
    void tearDown() {
        DatabaseManager.deleteTestDatabase();
    }

    /**
     * Testet die Erstellung eines neuen Benutzers.
     * Überprüft, ob der Benutzer erfolgreich erstellt wurde und in der Datenbank existiert.
     */
    @Test
    void testBenutzerErstellen() {
        assertFalse(userManager.userExists(), "Es sollte noch kein Benutzer existieren");
        
        boolean created = userManager.createUser(TEST_PASSWORD);
        assertTrue(created, "Benutzer sollte erfolgreich erstellt werden");
        assertTrue(userManager.userExists(), "Benutzer sollte jetzt existieren");
    }

    /**
     * Testet die Benutzerauthentifizierung.
     * Überprüft die erfolgreiche Anmeldung mit korrektem Passwort
     * und das Scheitern mit falschem Passwort.
     */
    @Test
    void testAuthentifizierung() {
        userManager.createUser(TEST_PASSWORD);
        
        assertTrue(userManager.authenticate(TEST_PASSWORD), "Authentifizierung mit korrektem Passwort sollte erfolgreich sein");
        assertFalse(userManager.authenticate("FalschesPasswort"), "Authentifizierung mit falschem Passwort sollte fehlschlagen");
    }

    /**
     * Testet die Änderung des Benutzerpassworts.
     * Überprüft, ob die Passwortänderung erfolgreich ist
     * und die Authentifizierung mit dem neuen Passwort funktioniert.
     */
    @Test
    void testPasswortAendern() {
        userManager.createUser(TEST_PASSWORD);
        
        String neuesPasswort = "NeuesTestPasswort123!";
        assertTrue(userManager.changePassword(TEST_PASSWORD, neuesPasswort), "Passwortänderung sollte erfolgreich sein");
        assertTrue(userManager.authenticate(neuesPasswort), "Authentifizierung mit neuem Passwort sollte erfolgreich sein");
        assertFalse(userManager.authenticate(TEST_PASSWORD), "Authentifizierung mit altem Passwort sollte fehlschlagen");
    }

    /**
     * Testet die Passwortänderung mit falschem aktuellem Passwort.
     * Überprüft, ob die Passwortänderung fehlschlägt, wenn das aktuelle Passwort falsch ist.
     */
    @Test
    void testPasswortAendernFalschesAktuellesPasswort() {
        userManager.createUser(TEST_PASSWORD);
        
        assertFalse(userManager.changePassword("FalschesPasswort", "NeuesPasswort123!"), 
                "Passwortänderung mit falschem aktuellem Passwort sollte fehlschlagen");
    }
} 