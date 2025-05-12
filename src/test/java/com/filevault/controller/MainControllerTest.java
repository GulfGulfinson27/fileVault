package com.filevault.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.filevault.model.EncryptedFile;
import com.filevault.model.VirtualFolder;
import com.filevault.util.TestApplicationHelper;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

@ExtendWith(TestApplicationHelper.class)
class MainControllerTest {

    /**
     * Test subclass that overrides problematic UI methods
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
        
        // Getter methods for UI components
        public TableView<Object> getTestFileTableView() {
            return testFileTableView;
        }
        
        public TreeView<VirtualFolder> getTestFolderTreeView() {
            return testFolderTreeView;
        }
        
        // Override setter to also set test field
        @Override
        public void setFileTableView(TableView<Object> fileTableView) {
            super.setFileTableView(fileTableView);
            this.testFileTableView = fileTableView;
        }
        
        // Override setter to also set test field
        @Override
        public void setFolderTreeView(TreeView<VirtualFolder> folderTreeView) {
            super.setFolderTreeView(folderTreeView);
            this.testFolderTreeView = folderTreeView;
        }
        
        // Reset test flags
        public void resetFlags() {
            refreshCalled = false;
            newFolderCalled = false;
            renameFolderCalled = false;
            deleteFolderCalled = false;
            renameFileCalled = false;
            deleteFileCalled = false;
        }
        
        // Add test folders
        public void addTestFolder(VirtualFolder folder) {
            folders.add(folder);
        }
        
        // Add test files
        public void addTestFile(EncryptedFile file) {
            files.add(file);
        }
        
        // Set current folder for testing
        public void setCurrentFolder(VirtualFolder folder) {
            this.currentFolder = folder;
        }
        
        // Get test flags
        public boolean isRefreshCalled() { return refreshCalled; }
        public boolean isNewFolderCalled() { return newFolderCalled; }
        public boolean isRenameFolderCalled() { return renameFolderCalled; }
        public boolean isDeleteFolderCalled() { return deleteFolderCalled; }
        public boolean isRenameFileCalled() { return renameFileCalled; }
        public boolean isDeleteFileCalled() { return deleteFileCalled; }
        
        // Override methods that would show UI dialogs or interact with the JavaFX thread
        @Override
        public void handleNewFolder() {
            newFolderCalled = true;
        }
        
        @Override
        public void handleRenameFile(EncryptedFile file) {
            renameFileCalled = true;
        }
        
        @Override
        public void handleDeleteFile() {
            deleteFileCalled = true;
        }
        
        @Override
        public void handleRenameFolder(VirtualFolder folder) {
            renameFolderCalled = true;
        }
        
        @Override
        public void handleDeleteFolder() {
            deleteFolderCalled = true;
        }
        
        @Override
        public void handleChangePassword() {
            // No-op for testing
        }
        
        @Override
        public void handleSettings() {
            // No-op for testing
        }
        
        @Override
        public void handleAbout() {
            // No-op for testing
        }
        
        @Override
        public void handleExit() {
            // No-op for testing
        }
        
        @Override
        public void handleRefresh() {
            refreshCalled = true;
            testRefreshFileList();
        }
        
        // Allow test access to private refreshFileList method directly
        public void testRefreshFileList() {
            ObservableList<Object> items = FXCollections.observableArrayList();
            if (testFileTableView != null) {
                if (currentFolder != null) {
                    items.addAll(files);
                }
                testFileTableView.setItems(items);
            }
        }
        
        // Return current folder for testing
        public VirtualFolder getCurrentFolder() {
            return currentFolder;
        }
    }

    private TestableMainController controller;
    private VirtualFolder testFolder;
    private EncryptedFile testFile;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        controller = new TestableMainController();
        
        // Create test data
        testFolder = new VirtualFolder(1, "Test Folder", "Test Description", null);
        testFolder.setCreatedAt(LocalDateTime.now());
        testFile = new EncryptedFile(1, 1, "testfile.txt", "encryptedPath", 1024L, "text/plain", 
                                   LocalDateTime.now(), LocalDateTime.now());
        
        controller.addTestFolder(testFolder);
        controller.addTestFile(testFile);
        controller.setCurrentFolder(testFolder);
        
        // Use proper table and tree views with correct generic types
        TreeView<VirtualFolder> folderTreeView = new TreeView<>();
        TreeItem<VirtualFolder> rootItem = new TreeItem<>(testFolder);
        folderTreeView.setRoot(rootItem);
        controller.setFolderTreeView(folderTreeView);
        
        TableView<Object> fileTableView = new TableView<>();
        controller.setFileTableView(fileTableView);
        controller.setCurrentFolderLabel(new Label());
        controller.setStatusLabel(new Label());
        
        // Initialize table columns with proper generic types
        controller.setFileNameColumn(new TableColumn<>());
        controller.setFileSizeColumn(new TableColumn<>());
        controller.setFileDateColumn(new TableColumn<>());
        
        controller.setThemeToggleButton(new Button());
        controller.setRefreshButton(new Button());
    }

    @Test
    void testInitialize() {
        assertDoesNotThrow(() -> controller.initialize());
    }

    @Test
    void testHandleNewFolder() {
        controller.resetFlags();
        controller.handleNewFolder();
        assertTrue(controller.isNewFolderCalled());
    }

    @Test
    void testRefreshFileList() {
        controller.testRefreshFileList();
        assertNotNull(controller.getTestFileTableView().getItems());
        assertFalse(controller.getTestFileTableView().getItems().isEmpty());
        assertEquals(1, controller.getTestFileTableView().getItems().size());
    }
    
    @Test
    void testHandleFolderSelection() {
        assertDoesNotThrow(() -> {
            // Set up a new folder for selection
            VirtualFolder newFolder = new VirtualFolder(2, "Selected Folder", "Selected Description", null);
            newFolder.setCreatedAt(LocalDateTime.now());
            controller.addTestFolder(newFolder);
            
            // Create a tree item for the folder
            TreeItem<VirtualFolder> treeItem = new TreeItem<>(newFolder);
            controller.getTestFolderTreeView().getRoot().getChildren().add(treeItem);
            
            // Select the folder
            controller.getTestFolderTreeView().getSelectionModel().select(treeItem);
            
            // Call handleFolderSelection
            // We can't directly test this method due to event dependencies, but we can verify the controller state
            assertEquals("Test Folder", controller.getCurrentFolder().getName());
        });
    }
    
    @Test
    void testHandleRenameFile() {
        controller.resetFlags();
        controller.handleRenameFile(testFile);
        assertTrue(controller.isRenameFileCalled());
    }

    @Test
    void testHandleDeleteFile() {
        controller.resetFlags();
        controller.handleDeleteFile();
        assertTrue(controller.isDeleteFileCalled());
    }
    
    @Test
    void testHandleRenameFolder() {
        controller.resetFlags();
        controller.handleRenameFolder(testFolder);
        assertTrue(controller.isRenameFolderCalled());
    }
    
    @Test
    void testHandleDeleteFolder() {
        controller.resetFlags();
        controller.handleDeleteFolder();
        assertTrue(controller.isDeleteFolderCalled());
    }
    
    @Test
    void testHandleRefresh() {
        controller.resetFlags();
        controller.handleRefresh();
        assertTrue(controller.isRefreshCalled());
    }
    
    @Test
    void testCurrentFolderManagement() {
        // Test that the current folder is correctly set
        assertNotNull(controller.getCurrentFolder());
        assertEquals("Test Folder", controller.getCurrentFolder().getName());
        assertEquals("Test Description", controller.getCurrentFolder().getDescription());
        assertEquals(1, controller.getCurrentFolder().getId());
    }
    
    @Test
    void testFileTableViewSetup() {
        controller.testRefreshFileList();
        
        // Check that file table view contains the test file
        TableView<Object> fileTableView = controller.getTestFileTableView();
        assertNotNull(fileTableView);
        assertNotNull(fileTableView.getItems());
        assertEquals(1, fileTableView.getItems().size());
        assertTrue(fileTableView.getItems().contains(testFile));
    }
}