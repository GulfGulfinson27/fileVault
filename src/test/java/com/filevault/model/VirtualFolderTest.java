package com.filevault.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Testklasse für die VirtualFolder-Klasse.
 * Diese Klasse testet die Funktionalität der virtuellen Ordner im Tresor.
 */
@DisplayName("Tests für VirtualFolder")
public class VirtualFolderTest {
    
    private VirtualFolder rootFolder;
    private VirtualFolder childFolder1;
    private VirtualFolder childFolder2;
    private LocalDateTime testTime;
    
    /**
     * Initialisiert die Testumgebung vor jedem Test.
     * Erstellt eine einfache Ordnerstruktur für die Tests.
     */
    @BeforeEach
    public void setUp() {
        testTime = LocalDateTime.now();
        
        // Erstelle eine einfache Ordnerstruktur für die Tests
        rootFolder = new VirtualFolder(1, "Root", "Root-Ordner", null);
        rootFolder.setCreatedAt(testTime);
        
        childFolder1 = new VirtualFolder(2, "Child1", "Unterordner 1", 1);
        childFolder1.setCreatedAt(testTime.plusMinutes(1));
        
        childFolder2 = new VirtualFolder(3, "Child2", "Unterordner 2", 1);
        childFolder2.setCreatedAt(testTime.plusMinutes(2));
        
        rootFolder.addChild(childFolder1);
        rootFolder.addChild(childFolder2);
    }
    
    @Nested
    @DisplayName("Konstruktor Tests")
    class ConstructorTests {
        /**
         * Testet die VirtualFolder-Konstruktor.
         * Überprüft, ob alle Felder korrekt initialisiert werden.
         */
        @Test
        @DisplayName("Konstruktor sollte alle Felder korrekt initialisieren")
        public void testVirtualFolderConstructor() {
            // Teste, ob der Konstruktor alle Felder richtig initialisiert
            VirtualFolder folder = new VirtualFolder(4, "TestFolder", "Test Description", 1);
            
            assertEquals(4, folder.getId());
            assertEquals("TestFolder", folder.getName());
            assertEquals("Test Description", folder.getDescription());
            assertEquals(Integer.valueOf(1), folder.getParentId());
            assertNotNull(folder.getCreatedAt());
            assertTrue(folder.getChildren().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Getter und Setter Tests")
    class GetterSetterTests {
        /**
         * Testet die Setter- und Getter-Methoden für verschiedene Felder.
         */
        @ParameterizedTest
        @DisplayName("Name Setter und Getter sollten funktionieren")
        @ValueSource(strings = {"New Name", "Another Name", "Special!@#$"})
        public void testGetSetName(String name) {
            rootFolder.setName(name);
            assertEquals(name, rootFolder.getName());
        }
        
        /**
         * Testet die Setter- und Getter-Methoden für die Beschreibung.
         */
        @ParameterizedTest
        @DisplayName("Description Setter und Getter sollten funktionieren")
        @ValueSource(strings = {"New Description", "Another Description", "Special!@#$"})
        public void testGetSetDescription(String description) {
            rootFolder.setDescription(description);
            assertEquals(description, rootFolder.getDescription());
        }
        
        /**
         * Testet die Setter- und Getter-Methoden für die ParentId.
         */
        @ParameterizedTest
        @DisplayName("ParentId Setter und Getter sollten funktionieren")
        @ValueSource(ints = {5, 10, 15})
        public void testGetSetParentId(Integer parentId) {
            childFolder1.setParentId(parentId);
            assertEquals(parentId, childFolder1.getParentId());
        }
        
        /**
         * Testet die Getter-Methode für die ID.
         */
        @Test
        @DisplayName("getId sollte die korrekte ID zurückgeben")
        public void testGetId() {
            assertEquals(1, rootFolder.getId());
            assertEquals(2, childFolder1.getId());
            assertEquals(3, childFolder2.getId());
        }
        
        /**
         * Testet createdAt Getter und Setter
         */
        @Test
        @DisplayName("CreatedAt Setter und Getter sollten funktionieren")
        public void testGetSetCreatedAt() {
            assertEquals(testTime, rootFolder.getCreatedAt());
            
            LocalDateTime newTime = LocalDateTime.now().plusDays(1);
            rootFolder.setCreatedAt(newTime);
            assertEquals(newTime, rootFolder.getCreatedAt());
        }
    }
    
    @Nested
    @DisplayName("Kindverwaltung Tests")
    class ChildManagementTests {
        /**
         * Testet das Hinzufügen und Entfernen von Kindern.
         */
        @Test
        @DisplayName("Kind-Verwaltung sollte korrekt funktionieren")
        public void testGetAddRemoveChildren() {
            List<VirtualFolder> children = rootFolder.getChildren();
            assertEquals(2, children.size());
            assertTrue(children.contains(childFolder1));
            assertTrue(children.contains(childFolder2));
            
            // Teste das Hinzufügen eines weiteren Unterordners
            VirtualFolder newChild = new VirtualFolder(4, "Child3", "Unterordner 3", 1);
            rootFolder.addChild(newChild);
            
            assertEquals(3, rootFolder.getChildren().size());
            assertTrue(rootFolder.getChildren().contains(newChild));
            
            // Teste das Entfernen eines Unterordners
            rootFolder.removeChild(childFolder1);
            assertEquals(2, rootFolder.getChildren().size());
            assertFalse(rootFolder.getChildren().contains(childFolder1));
        }
    }
    
    @Nested
    @DisplayName("Pfad Tests")
    class PathTests {
        /**
         * Testet die Pfadermittlung für Ordner.
         */
        @Test
        @DisplayName("getFullPath sollte den korrekten Pfad zurückgeben")
        public void testGetFullPath() {
            assertEquals("Root", rootFolder.getFullPath());
            assertEquals("Child1", childFolder1.getFullPath());
        }
    }
    
    @Nested
    @DisplayName("String Repräsentation Tests")
    class StringRepresentationTests {
        /**
         * Testet die toString-Methode.
         */
        @ParameterizedTest
        @DisplayName("toString sollte den Ordnernamen zurückgeben")
        @CsvSource({
            "TestFolder1, TestFolder1",
            "Special Name!@#, Special Name!@#",
            "'', ''"
        })
        public void testToString(String name, String expected) {
            VirtualFolder folder = new VirtualFolder(5, name, "Test Description", 1);
            assertEquals(expected, folder.toString());
        }
    }
    
    @Nested
    @DisplayName("Gleichheit Tests")
    class EqualityTests {
        /**
         * Stellt Testfälle für Gleichheit und Ungleichheit bereit.
         */
        private static Stream<Arguments> equalityTestCases() {
            return Stream.of(
                // Gleiche IDs, verschiedene Namen und Beschreibungen: sollten gleich sein
                Arguments.of(1, "Different Name", "Different Description", null, true),
                // Verschiedene IDs: sollten ungleich sein
                Arguments.of(5, "Root", "Root-Ordner", null, false)
            );
        }
        
        /**
         * Testet die equals-Methode mit verschiedenen Fällen.
         */
        @ParameterizedTest
        @DisplayName("equals sollte IDs und ParentIDs vergleichen")
        @MethodSource("equalityTestCases")
        public void testEquals(int id, String name, String description, Integer parentId, boolean expectedEquality) {
            VirtualFolder testFolder = new VirtualFolder(id, name, description, parentId);
            assertEquals(expectedEquality, rootFolder.equals(testFolder));
        }
        
        /**
         * Testet Spezialfälle der equals-Methode.
         */
        @Test
        @DisplayName("equals sollte Spezialfälle korrekt behandeln")
        public void testEqualsSpecialCases() {
            // Selbstgleichheit
            assertTrue(rootFolder.equals(rootFolder));
            
            // Ungleichheit mit null
            assertFalse(rootFolder.equals(null));
            
            // Ungleichheit mit anderem Objekttyp
            assertFalse(rootFolder.equals("Not a folder"));
        }
        
        /**
         * Testet die hashCode-Methode.
         */
        @Test
        @DisplayName("hashCode sollte konsistent mit equals sein")
        public void testHashCode() {
            // Gleiche IDs und parentIds sollten gleiche Hashcodes haben
            VirtualFolder sameFolder = new VirtualFolder(1, "Different Name", "Different Description", null);
            assertEquals(rootFolder.hashCode(), sameFolder.hashCode());
            
            // Verschiedene IDs sollten verschiedene Hashcodes haben
            VirtualFolder differentFolder = new VirtualFolder(5, "Root", "Root-Ordner", null);
            assertNotEquals(rootFolder.hashCode(), differentFolder.hashCode());
        }
    }
} 