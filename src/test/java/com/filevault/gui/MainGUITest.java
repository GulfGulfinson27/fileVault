package com.filevault.gui;

import com.filevault.core.Vault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für die MainGUI-Klasse.
 * Diese Klasse testet die Funktionalität der grafischen Benutzeroberfläche,
 * ohne JavaFX-Komponenten zu initialisieren, um Headless-Tests zu ermöglichen.
 */
public class MainGUITest {
    
    private MainGUI mainGUI;
    
    /**
     * Initialisiert die Testumgebung vor jedem Test.
     */
    @BeforeEach
    public void setUp() {
        // Nur die Klasse instanziieren, aber keine JavaFX-Komponenten initialisieren
        mainGUI = new MainGUI();
    }
    
    /**
     * Bereinigt nach jedem Test.
     */
    @AfterEach
    public void tearDown() {
        mainGUI = null;
    }
    
    /**
     * Testet die Existenz der erwarteten Methoden in der MainGUI-Klasse.
     * Dies ist ein struktureller Test, der keine JavaFX-Initialisierung erfordert.
     */
    @Test
    public void testMethodsExist() {
        // Überprüfe, ob die erforderlichen Methoden in der MainGUI-Klasse vorhanden sind
        assertDoesNotThrow(() -> MainGUI.class.getDeclaredMethod("initializeComponents"),
                "Die initializeComponents-Methode sollte existieren");
        
        assertDoesNotThrow(() -> MainGUI.class.getDeclaredMethod("applyStyles"),
                "Die applyStyles-Methode sollte existieren");
        
        assertDoesNotThrow(() -> MainGUI.class.getDeclaredMethod("setupEventHandlers"),
                "Die setupEventHandlers-Methode sollte existieren");
        
        assertDoesNotThrow(() -> MainGUI.class.getDeclaredMethod("showError", String.class, String.class),
                "Die showError-Methode sollte existieren");
        
        assertDoesNotThrow(() -> MainGUI.class.getDeclaredMethod("start", javafx.stage.Stage.class),
                "Die start-Methode sollte existieren");
    }
    
    /**
     * Testet die Existenz der erforderlichen Felder in der MainGUI-Klasse.
     * Dies ist ein struktureller Test, der keine JavaFX-Initialisierung erfordert.
     */
    @Test
    public void testFieldsExist() {
        // Überprüfe, ob die erforderlichen Felder in der MainGUI-Klasse vorhanden sind
        assertDoesNotThrow(() -> MainGUI.class.getDeclaredField("mainContainer"),
                "Das mainContainer-Feld sollte existieren");
        
        assertDoesNotThrow(() -> MainGUI.class.getDeclaredField("importButton"),
                "Das importButton-Feld sollte existieren");
        
        assertDoesNotThrow(() -> MainGUI.class.getDeclaredField("exportButton"),
                "Das exportButton-Feld sollte existieren");
        
        assertDoesNotThrow(() -> MainGUI.class.getDeclaredField("deleteButton"),
                "Das deleteButton-Feld sollte existieren");
        
        assertDoesNotThrow(() -> MainGUI.class.getDeclaredField("fileTable"),
                "Das fileTable-Feld sollte existieren");
        
        assertDoesNotThrow(() -> MainGUI.class.getDeclaredField("statusBar"),
                "Das statusBar-Feld sollte existieren");
        
        assertDoesNotThrow(() -> MainGUI.class.getDeclaredField("vault"),
                "Das vault-Feld sollte existieren");
    }
    
    /**
     * Testet, ob die MainGUI mit der Singleton-Instanz von Vault arbeiten kann.
     * Dies ist ein funktionaler Test, der keine JavaFX-Initialisierung erfordert.
     */
    @Test
    public void testVaultAssignment() throws Exception {
        Field vaultField = MainGUI.class.getDeclaredField("vault");
        vaultField.setAccessible(true);
        
        // Vault-Instanz zuweisen und überprüfen
        vaultField.set(mainGUI, Vault.getInstance());
        
        assertEquals(Vault.getInstance(), vaultField.get(mainGUI),
                "MainGUI sollte die Singleton-Instanz von Vault verwenden können");
    }
    
    /**
     * Dieser Test überprüft, ob die MainGUI-Klasse von Application erbt.
     * Dies ist ein struktureller Test, der keine JavaFX-Initialisierung erfordert.
     */
    @Test
    public void testExtendsApplication() {
        assertTrue(javafx.application.Application.class.isAssignableFrom(MainGUI.class),
                "MainGUI sollte von Application erben");
    }
    
    /**
     * Dieser Test überprüft den Konstruktor der MainGUI-Klasse.
     * Dies ist ein struktureller Test, der keine JavaFX-Initialisierung erfordert.
     */
    @Test
    public void testConstructor() {
        assertNotNull(mainGUI, "Der MainGUI-Konstruktor sollte ein gültiges Objekt erstellen");
    }
}