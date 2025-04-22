package com.filevault.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testklasse für die VirtualFolder-Klasse.
 * Diese Klasse testet die Funktionalität der virtuellen Ordner im Tresor.
 */
public class VirtualFolderTest {
    
    private VirtualFolder testFolder;
    
    /**
     * Initialisiert die Testumgebung vor jedem Test.
     * Erstellt einen neuen virtuellen Ordner mit Testdaten.
     */
    @BeforeEach
    public void setUp() {
        testFolder = new VirtualFolder(
            1,                      // id
            "Testordner",           // name
            "Ein Testordner",       // description
            null                    // parentId
        );
    }
    
    /**
     * Testet die Getter-Methoden der VirtualFolder-Klasse.
     * Überprüft, ob alle Werte korrekt gesetzt und abgerufen werden können.
     */
    @Test
    public void testGetters() {
        assertEquals(1, testFolder.getId());
        assertEquals("Testordner", testFolder.getName());
        assertEquals("Ein Testordner", testFolder.getDescription());
    }
    
    /**
     * Testet die Setter-Methoden der VirtualFolder-Klasse.
     * Überprüft, ob Werte korrekt geändert werden können.
     */
    @Test
    public void testSetters() {
        testFolder.setName("Neuer Name");
        assertEquals("Neuer Name", testFolder.getName());
        
        testFolder.setDescription("Neue Beschreibung");
        assertEquals("Neue Beschreibung", testFolder.getDescription());
    }
    
    /**
     * Testet die equals- und hashCode-Methoden.
     * Überprüft die korrekte Implementierung der Objektgleichheit.
     */
    @Test
    public void testEqualsAndHashCode() {
        VirtualFolder sameId = new VirtualFolder(1, "Anderer Name", "Andere Beschreibung", null);
        VirtualFolder differentId = new VirtualFolder(2, "Testordner", "Ein Testordner", null);
        VirtualFolder differentParent = new VirtualFolder(1, "Testordner", "Ein Testordner", 5);
        
        assertTrue(testFolder.equals(sameId));
        assertFalse(testFolder.equals(differentId));
        assertFalse(testFolder.equals(differentParent));
        assertEquals(testFolder.hashCode(), sameId.hashCode());
        assertNotEquals(testFolder.hashCode(), differentId.hashCode());
        assertNotEquals(testFolder.hashCode(), differentParent.hashCode());
    }
    
    /**
     * Testet die toString-Methode.
     * Überprüft, ob der Ordnername korrekt zurückgegeben wird.
     */
    @Test
    public void testToString() {
        assertEquals("Testordner", testFolder.toString());
    }
    
    /**
     * Testet die Grenzfälle für die Setter-Methoden.
     * Überprüft das Verhalten bei leeren Strings und null-Werten.
     */
    @Test
    public void testEdgeCases() {
        // Test mit leerem Namen
        testFolder.setName("");
        assertEquals("", testFolder.getName());
        
        // Test mit leerer Beschreibung
        testFolder.setDescription("");
        assertEquals("", testFolder.getDescription());
        
        // Test mit null-Werten
        testFolder.setName(null);
        assertNull(testFolder.getName());
        
        testFolder.setDescription(null);
        assertNull(testFolder.getDescription());
    }
} 