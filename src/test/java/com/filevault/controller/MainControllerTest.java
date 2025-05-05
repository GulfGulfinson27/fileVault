package com.filevault.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.filevault.model.EncryptedFile;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

class MainControllerTest {

    @BeforeAll
    static void initToolkit() {
        Platform.startup(() -> {
            // Initialize JavaFX toolkit
            new Stage();
        });
    }

    private MainController mainController;

    @BeforeEach
    void setUp() {
        mainController = new MainController();
        mainController.setFolderTreeView(new TreeView<>());
        mainController.setFileTableView(new TableView<>());
        mainController.setCurrentFolderLabel(new Label());
        mainController.setStatusLabel(new Label());
        mainController.setFileNameColumn(new TableColumn<>());
        mainController.setFileSizeColumn(new TableColumn<>());
        mainController.setFileDateColumn(new TableColumn<>());
        mainController.setThemeToggleButton(new Button());
    }

    @Test
    void testInitialize() {
        assertDoesNotThrow(() -> mainController.initialize());
    }

    @Test
    void testHandleNewFolder() {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> mainController.handleNewFolder());
        });
    }

    @Test
    void testHandleImportFile() {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> mainController.handleImportFile());
        });
    }

    @Test
    void testHandleExportFile() {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> mainController.handleExportFile());
        });
    }

    @Test
    void testHandleRenameFile() {
        Platform.runLater(() -> {
            EncryptedFile mockFile = new EncryptedFile(1, 1, "mockFile", "mockPath", 1024L, "mockKey", null, null); // Create a valid mock EncryptedFile instance
            assertDoesNotThrow(() -> mainController.handleRenameFile(mockFile));
        });
    }

    @Test
    void testHandleDeleteFile() {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> mainController.handleDeleteFile());
        });
    }

    @Test
    void testHandleChangePassword() {
        assertDoesNotThrow(() -> mainController.handleChangePassword());
    }

    @Test
    void testHandleSettings() {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> mainController.handleSettings());
        });
    }

    @Test
    void testHandleAbout() {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> mainController.handleAbout());
        });
    }

    @Test
    void testHandleExit() {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> mainController.handleExit());
        });
    }
}