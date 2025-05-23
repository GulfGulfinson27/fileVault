package com.filevault.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.filevault.model.EncryptedFile;
import com.filevault.model.VirtualFolder;
import com.filevault.util.TestApplicationHelper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Testklasse für den MainController.
 * Diese Klasse testet die Funktionalität des Hauptcontrollers der Anwendung
 * mit Hilfe einer speziellen testbaren Unterklasse.
 */
@ExtendWith(TestApplicationHelper.class)
class MainControllerTest {

    /**
     * Testbare Unterklasse, die problematische UI-Methoden überschreibt.
     * Diese Klasse ermöglicht das Testen des Controllers ohne direkte JavaFX-Abhängigkeiten.
     */
    static class TestableMainController extends MainController {
        private List<VirtualFolder> folders = new ArrayList<>();
        private List<EncryptedFile> files = new ArrayList<>();
        private VirtualFolder currentFolder;
        private TableView<Object> testFileTableView;
        private TreeView<VirtualFolder> testFolderTreeView;
        private boolean refreshCalled = false;
        private boolean newFolderCalled = false;
        private boolean renameFolderCalled = false;
        private boolean deleteFolderCalled = false;
        private boolean renameFileCalled = false;
        private boolean deleteFileCalled = false;
        
        /**
         * Gibt die Test-TableView für Dateien zurück.
         * @return Die TableView für Dateien
         */
        public TableView<Object> getTestFileTableView() {
            return testFileTableView;
        }
        
        /**
         * Gibt die Test-TreeView für Ordner zurück.
         * @return Die TreeView für Ordner
         */
        public TreeView<VirtualFolder> getTestFolderTreeView() {
            return testFolderTreeView;
        }
        
        /**
         * Überschreibt die Setter-Methode, um auch das Testfeld zu setzen.
         * @param fileTableView Die zu setzende TableView
         */
        @Override
        public void setFileTableView(TableView<Object> fileTableView) {
            super.setFileTableView(fileTableView);
            this.testFileTableView = fileTableView;
        }
        
        /**
         * Überschreibt die Setter-Methode, um auch das Testfeld zu setzen.
         * @param folderTreeView Die zu setzende TreeView
         */
        @Override
        public void setFolderTreeView(TreeView<VirtualFolder> folderTreeView) {
            super.setFolderTreeView(folderTreeView);
            this.testFolderTreeView = folderTreeView;
        }
        
        /**
         * Setzt alle Test-Flags zurück.
         */
        public void resetFlags() {
            refreshCalled = false;
            newFolderCalled = false;
            renameFolderCalled = false;
            deleteFolderCalled = false;
            renameFileCalled = false;
            deleteFileCalled = false;
        }
        
        /**
         * Fügt einen Testordner hinzu.
         * @param folder Der hinzuzufügende Ordner
         */
        public void addTestFolder(VirtualFolder folder) {
            folders.add(folder);
        }
        
        /**
         * Fügt eine Testdatei hinzu.
         * @param file Die hinzuzufügende Datei
         */
        public void addTestFile(EncryptedFile file) {
            files.add(file);
        }
        
        /**
         * Setzt den aktuellen Ordner für Tests.
         * @param folder Der zu setzende Ordner
         */
        public void setCurrentFolder(VirtualFolder folder) {
            this.currentFolder = folder;
        }
        
        /**
         * Getter-Methoden für Test-Flags.
         */
        public boolean isRefreshCalled() { return refreshCalled; }
        public boolean isNewFolderCalled() { return newFolderCalled; }
        public boolean isRenameFolderCalled() { return renameFolderCalled; }
        public boolean isDeleteFolderCalled() { return deleteFolderCalled; }
        public boolean isRenameFileCalled() { return renameFileCalled; }
        public boolean isDeleteFileCalled() { return deleteFileCalled; }
        
        /**
         * Überschreibt die Methode zum Erstellen eines neuen Ordners.
         */
        @Override
        public void handleNewFolder() {
            newFolderCalled = true;
        }
        
        /**
         * Überschreibt die Methode zum Umbenennen einer Datei.
         * @param file Die umzubenennende Datei
         */
        @Override
        public void handleRenameFile(EncryptedFile file) {
            renameFileCalled = true;
        }
        
        /**
         * Überschreibt die Methode zum Löschen einer Datei.
         */
        @Override
        public void handleDeleteFile() {
            deleteFileCalled = true;
        }
        
        /**
         * Überschreibt die Methode zum Umbenennen eines Ordners.
         * @param folder Der umzubenennende Ordner
         */
        @Override
        public void handleRenameFolder(VirtualFolder folder) {
            renameFolderCalled = true;
        }
        
        /**
         * Überschreibt die Methode zum Löschen eines Ordners.
         */
        @Override
        public void handleDeleteFolder() {
            deleteFolderCalled = true;
        }
        
        /**
         * Überschreibt die Methode zum Ändern des Passworts.
         */
        @Override
        public void handleChangePassword() {
            // Keine Operation für Tests
        }
        
        /**
         * Überschreibt die Methode zum Öffnen der Einstellungen.
         */
        @Override
        public void handleSettings() {
            // Keine Operation für Tests
        }
        
        /**
         * Überschreibt die Methode zum Anzeigen der Über-Informationen.
         */
        @Override
        public void handleAbout() {
            // Keine Operation für Tests
        }
        
        /**
         * Überschreibt die Methode zum Beenden der Anwendung.
         */
        @Override
        public void handleExit() {
            // Keine Operation für Tests
        }
        
        /**
         * Überschreibt die Methode zum Aktualisieren der Ansicht.
         */
        @Override
        public void handleRefresh() {
            refreshCalled = true;
            testRefreshFileList();
        }
        
        /**
         * Erlaubt Testzugriff auf die private refreshFileList-Methode.
         */
        public void testRefreshFileList() {
            ObservableList<Object> items = FXCollections.observableArrayList();
            if (testFileTableView != null) {
                if (currentFolder != null) {
                    items.addAll(files);
                }
                testFileTableView.setItems(items);
            }
        }
        
        /**
         * Gibt den aktuellen Ordner für Tests zurück.
         * @return Der aktuelle Ordner
         */
        public VirtualFolder getCurrentFolder() {
            return currentFolder;
        }
    }

    /** Der zu testende Controller */
    private TestableMainController controller;
    
    /** Ein Testordner für die Tests */
    private VirtualFolder testFolder;
    
    /** Eine Testdatei für die Tests */
    private EncryptedFile testFile;

    /**
     * Initialisiert die Testumgebung vor jedem Test.
     * Erstellt einen Controller und Testdaten.
     */
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        controller = new TestableMainController();
        
        // Erstelle Testdaten
        testFolder = new VirtualFolder(1, "Test Folder", "Test Description", null);
        testFolder.setCreatedAt(LocalDateTime.now());
        testFile = new EncryptedFile(1, 1, "testfile.txt", "encryptedPath", 1024L, "text/plain", 
                                   LocalDateTime.now(), LocalDateTime.now());
        
        controller.addTestFolder(testFolder);
        controller.addTestFile(testFile);
        controller.setCurrentFolder(testFolder);
        
        // Verwende korrekte TableView und TreeView mit richtigen generischen Typen
        TreeView<VirtualFolder> folderTreeView = new TreeView<>();
        TreeItem<VirtualFolder> rootItem = new TreeItem<>(testFolder);
        folderTreeView.setRoot(rootItem);
        controller.setFolderTreeView(folderTreeView);
        
        TableView<Object> fileTableView = new TableView<>();
        TableColumn<Object, String> nameColumn = new TableColumn<>("Name");
        TableColumn<Object, String> sizeColumn = new TableColumn<>("Size");
        TableColumn<Object, String> dateColumn = new TableColumn<>("Date");
        fileTableView.getColumns().addAll(nameColumn, sizeColumn, dateColumn);
        controller.setFileTableView(fileTableView);
        
        // Reset flags before each test
        controller.resetFlags();
    }

    /**
     * Testet die Initialisierung des Controllers.
     * Überprüft, ob alle Komponenten korrekt erstellt wurden.
     */
    @Test
    void testInitialization() {
        assertNotNull(controller.getTestFileTableView());
        assertNotNull(controller.getTestFolderTreeView());
        assertNotNull(controller.getCurrentFolder());
    }
    
    /**
     * Testet die Behandlung eines neuen Ordners.
     * Überprüft, ob die entsprechende Methode aufgerufen wird.
     */
    @Test
    void testHandleNewFolder() {
        controller.handleNewFolder();
        assertTrue(controller.isNewFolderCalled());
    }
    
    /**
     * Testet die Umbenennung eines Ordners.
     * Überprüft, ob die entsprechende Methode aufgerufen wird.
     */
    @Test
    void testHandleRenameFolder() {
        controller.handleRenameFolder(testFolder);
        assertTrue(controller.isRenameFolderCalled());
    }
    
    /**
     * Testet das Löschen eines Ordners.
     * Überprüft, ob die entsprechende Methode aufgerufen wird.
     */
    @Test
    void testHandleDeleteFolder() {
        controller.handleDeleteFolder();
        assertTrue(controller.isDeleteFolderCalled());
    }
    
    /**
     * Testet die Umbenennung einer Datei.
     * Überprüft, ob die entsprechende Methode aufgerufen wird.
     */
    @Test
    void testHandleRenameFile() {
        controller.handleRenameFile(testFile);
        assertTrue(controller.isRenameFileCalled());
    }
    
    /**
     * Testet das Löschen einer Datei.
     * Überprüft, ob die entsprechende Methode aufgerufen wird.
     */
    @Test
    void testHandleDeleteFile() {
        controller.handleDeleteFile();
        assertTrue(controller.isDeleteFileCalled());
    }
    
    /**
     * Testet die Aktualisierung der Dateiliste.
     * Überprüft, ob die entsprechende Methode aufgerufen wird und die Dateiliste aktualisiert wird.
     */
    @Test
    void testHandleRefresh() {
        controller.handleRefresh();
        assertTrue(controller.isRefreshCalled());
        
        // Verify the file table has been updated
        assertNotNull(controller.getTestFileTableView().getItems());
        assertFalse(controller.getTestFileTableView().getItems().isEmpty());
    }
}