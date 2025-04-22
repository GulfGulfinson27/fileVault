package com.filevault.core;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.filevault.model.EncryptedFile;
import com.filevault.model.UserManager;
import com.filevault.model.VirtualFolder;
import com.filevault.storage.DatabaseManager;
import com.filevault.storage.FileStorage;
import com.filevault.util.FolderManager;

/**
 * Testklasse für die Vault-Klasse.
 * Diese Klasse testet die Kernfunktionalität zum Importieren, Exportieren und Löschen von Dateien und Ordnern.
 */
class VaultTest {
    private Vault vault;
    private FolderManager folderManager;
    private Path testDataDir;
    private Path testFilesDir;
    private VirtualFolder testFolder;
    private UserManager userManager;
    private static final String TEST_PASSWORD = "TestPasswort123!";

    /**
     * Initialisiert die Testumgebung vor jedem Test.
     * Erstellt eine Testdatenbank, Testverzeichnisse und einen Testordner.
     */
    @BeforeEach
    void setUp() throws Exception {
        // Initialisiere die Testdatenbank
        DatabaseManager.initDatabase(true);
        
        // Erstelle Testverzeichnisse
        testDataDir = Paths.get(System.getProperty("user.home"), ".filevault", "test_data");
        testFilesDir = Paths.get(System.getProperty("java.io.tmpdir"), "filevault_test_files");
        Files.createDirectories(testDataDir);
        Files.createDirectories(testFilesDir);
        
        // Initialisiere den UserManager und erstelle einen Testbenutzer
        userManager = UserManager.getInstance();
        userManager.logout(); // Sicherstellen, dass kein Benutzer angemeldet ist
        userManager.createUser(TEST_PASSWORD);
        assertTrue(userManager.authenticate(TEST_PASSWORD), "Benutzer sollte erfolgreich authentifiziert sein");
        
        // Initialisiere den FolderManager
        folderManager = FolderManager.getInstance();
        folderManager.initialize();
        
        // Erstelle einen Testordner
        testFolder = folderManager.createFolder("TestFolder", null);
        
        // Hole die Vault-Instanz
        vault = Vault.getInstance();
    }

    /**
     * Bereinigt nach jedem Test.
     * Löscht Testdateien und Testverzeichnisse, schließt die Datenbankverbindung.
     */
    @AfterEach
    void tearDown() throws Exception {
        // Benutzer abmelden
        userManager.logout();
        
        DatabaseManager.closeConnections();
        
        // Lösche den Testordner
        if (testFolder != null) {
            try {
                folderManager.deleteFolder(testFolder);
            } catch (Exception e) {
                // Ignoriere Fehler beim Löschen des Testordners
            }
        }
        
        // Lösche Testdateien
        if (Files.exists(testFilesDir)) {
            Files.walk(testFilesDir)
                .map(Path::toFile)
                .forEach(File::delete);
            Files.deleteIfExists(testFilesDir);
        }
        
        // Lösche Testdatenverzeichnis
        if (Files.exists(testDataDir)) {
            Files.walk(testDataDir)
                .map(Path::toFile)
                .forEach(File::delete);
            Files.deleteIfExists(testDataDir);
        }
        
        // Lösche Testdatenbank
        DatabaseManager.deleteTestDatabase();
    }

    /**
     * Testet das Importieren einer Datei.
     * Überprüft, ob die Datei erfolgreich importiert wurde.
     */
    @Test
    void testImportFile() throws Exception {
        // Erstelle eine Testdatei
        File testFile = createTestFile("testImport.txt", "Das ist ein Testinhalt");
        
        // Importiere die Datei
        EncryptedFile encryptedFile = vault.importFile(testFile, "TestFolder");
        
        // Überprüfe, ob die Datei korrekt importiert wurde
        assertNotNull(encryptedFile);
        assertEquals("testImport.txt", encryptedFile.getOriginalName());
        assertEquals(testFolder.getId(), encryptedFile.getFolderId());
        assertTrue(new File(encryptedFile.getEncryptedPath()).exists());
        
        // Überprüfe, ob die Datei im Ordner ist
        List<EncryptedFile> files = FileStorage.getInstance().getFilesInFolder(testFolder);
        assertEquals(1, files.size());
        assertEquals("testImport.txt", files.get(0).getOriginalName());
    }

    /**
     * Testet das Exportieren einer Datei.
     * Überprüft, ob die Datei erfolgreich exportiert wurde.
     */
    @Test
    void testExportFile() throws Exception {
        // Erstelle und importiere eine Testdatei
        File testFile = createTestFile("testExport.txt", "Das ist ein Testinhalt zum Exportieren");
        EncryptedFile encryptedFile = vault.importFile(testFile, "TestFolder");
        
        // Erstelle Zieldatei für den Export
        File exportFile = new File(testFilesDir.toString(), "exported.txt");
        
        // Exportiere die Datei
        boolean success = vault.exportFile(encryptedFile, exportFile);
        
        // Überprüfe, ob der Export erfolgreich war
        assertTrue(success);
        assertTrue(exportFile.exists());
        
        // Überprüfe den Inhalt der exportierten Datei (sollte dem Original entsprechen)
        String exportedContent = new String(Files.readAllBytes(exportFile.toPath()));
        assertEquals("Das ist ein Testinhalt zum Exportieren", exportedContent);
    }

    /**
     * Testet das Löschen eines Ordners mit Dateien.
     * Überprüft, ob der Ordner und alle enthaltenen Dateien gelöscht wurden.
     */
    @Test
    void testDeleteFolder() throws Exception {
        // Erstelle und importiere mehrere Testdateien
        File testFile1 = createTestFile("file1.txt", "Inhalt von Datei 1");
        File testFile2 = createTestFile("file2.txt", "Inhalt von Datei 2");
        
        vault.importFile(testFile1, "TestFolder");
        vault.importFile(testFile2, "TestFolder");
        
        // Überprüfe, ob die Dateien im Ordner sind
        List<EncryptedFile> filesBeforeDelete = FileStorage.getInstance().getFilesInFolder(testFolder);
        assertEquals(2, filesBeforeDelete.size());
        
        // Lösche den Ordner
        vault.deleteFolder("TestFolder");
        
        // Überprüfe, ob der Ordner nicht mehr existiert
        assertNull(folderManager.getFolderByName("TestFolder"));
        
        // Stelle sicher, dass testFolder auf null gesetzt ist, damit tearDown() nicht versucht, ihn erneut zu löschen
        testFolder = null;
    }

    /**
     * Testet das Löschen eines Ordners, der nicht existiert.
     * Erwartet eine Exception.
     */
    @Test
    void testDeleteNonExistentFolder() {
        // Versuche, einen nicht existierenden Ordner zu löschen
        Exception exception = assertThrows(Exception.class, () -> {
            vault.deleteFolder("NonExistentFolder");
        });
        
        // Überprüfe die Fehlermeldung
        assertEquals("Ordner nicht gefunden: NonExistentFolder", exception.getMessage());
    }

    /**
     * Testet das Importieren einer Datei in einen nicht existierenden Ordner.
     * Erwartet eine Exception.
     */
    @Test
    void testImportFileToNonExistentFolder() throws Exception {
        // Erstelle eine Testdatei
        File testFile = createTestFile("testImport.txt", "Das ist ein Testinhalt");
        
        // Versuche, die Datei in einen nicht existierenden Ordner zu importieren
        Exception exception = assertThrows(Exception.class, () -> {
            vault.importFile(testFile, "NonExistentFolder");
        });
        
        // Überprüfe die Fehlermeldung
        assertEquals("Ordner nicht gefunden: NonExistentFolder", exception.getMessage());
    }

    /**
     * Testet das Importieren einer nicht existierenden Datei.
     * Erwartet eine Exception.
     */
    @Test
    void testImportNonExistentFile() {
        // Erstelle einen nicht existierenden Verweis auf eine Datei
        File nonExistentFile = new File(testFilesDir.toString(), "nonexistent.txt");
        
        // Versuche, die nicht existierende Datei zu importieren
        assertThrows(IOException.class, () -> {
            vault.importFile(nonExistentFile, "TestFolder");
        });
    }

    /**
     * Testet den Singleton-Aspekt der Vault-Klasse.
     * Überprüft, ob immer dieselbe Instanz zurückgegeben wird.
     */
    @Test
    void testSingletonPattern() {
        // Hole mehrere Instanzen
        Vault firstInstance = Vault.getInstance();
        Vault secondInstance = Vault.getInstance();
        
        // Überprüfe, ob es sich um dieselbe Instanz handelt
        assertSame(firstInstance, secondInstance);
    }

    /**
     * Hilfsmethode zum Erstellen einer Testdatei mit dem angegebenen Inhalt.
     * 
     * @param fileName Der Name der Testdatei
     * @param content Der Inhalt der Testdatei
     * @return Die erstellte Datei
     * @throws IOException Wenn die Datei nicht erstellt werden kann
     */
    private File createTestFile(String fileName, String content) throws IOException {
        Path filePath = testFilesDir.resolve(fileName);
        Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE);
        return filePath.toFile();
    }
}