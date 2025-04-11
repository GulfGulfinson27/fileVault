package com.filevault.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.filevault.core.Vault;
import java.io.File;
import java.util.Optional;
import javafx.geometry.Insets;

public class MainGUI extends Application {
    private Vault vault;
    private TableView<File> fileTable;
    private TreeView<String> folderTree;
    private Label statusBar;
    private Button importButton, exportButton, createFolderButton, deleteButton;
    private VBox mainContainer;
    private SplitPane splitPane;

    @Override
    public void start(Stage primaryStage) {
        // Initialize vault
        vault = Vault.getInstance();
        
        // Initialize components
        initializeComponents();
        
        // Create scene
        Scene scene = new Scene(mainContainer, 800, 600);
        
        // Apply styles directly
        applyStyles();
        
        // Set up event handlers
        setupEventHandlers();
        
        // Set up the stage
        primaryStage.setTitle("FileVault");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void initializeComponents() {
        // Create main container
        mainContainer = new VBox();
        mainContainer.setSpacing(10);
        mainContainer.setPadding(new Insets(10));

        // Create buttons
        importButton = new Button("Import");
        exportButton = new Button("Export");
        createFolderButton = new Button("Create Folder");
        deleteButton = new Button("Delete");

        // Create status bar
        statusBar = new Label("Ready");

        // Create folder tree
        folderTree = new TreeView<>();
        TreeItem<String> rootItem = new TreeItem<>("Root");
        folderTree.setRoot(rootItem);
        folderTree.setShowRoot(true);

        // Create file table
        fileTable = new TableView<>();
        TableColumn<File, String> nameColumn = new TableColumn<>("Name");
        TableColumn<File, String> sizeColumn = new TableColumn<>("Size");
        TableColumn<File, String> dateColumn = new TableColumn<>("Date");
        fileTable.getColumns().addAll(nameColumn, sizeColumn, dateColumn);

        // Create split pane
        splitPane = new SplitPane();
        VBox leftPane = new VBox(folderTree);
        VBox rightPane = new VBox(fileTable);
        splitPane.getItems().addAll(leftPane, rightPane);
        splitPane.setDividerPositions(0.3);

        // Add components to main container
        HBox buttonBar = new HBox(10, importButton, exportButton, createFolderButton, deleteButton);
        mainContainer.getChildren().addAll(buttonBar, splitPane, statusBar);
    }

    private void applyStyles() {
        // Table styles
        fileTable.setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #404040; -fx-border-width: 1;");
        
        // Table header styles
        fileTable.lookup(".column-header").setStyle("-fx-background-color: #2e7d32;");
        fileTable.lookup(".column-header-background").setStyle("-fx-background-color: #2e7d32;");
        fileTable.lookup(".column-header .label").setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
        
        // Table row styles
        fileTable.setStyle("-fx-background-color: #2d2d2d;");
        fileTable.lookup(".table-row-cell").setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff;");
        fileTable.lookup(".table-row-cell:odd").setStyle("-fx-background-color: #252525;");
        fileTable.lookup(".table-row-cell:selected").setStyle("-fx-background-color: #4a90e2; -fx-text-fill: #ffffff;");
        
        // Button styles
        importButton.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
        exportButton.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
        createFolderButton.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
        deleteButton.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
        
        // Tree view styles
        folderTree.setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #404040; -fx-border-width: 1;");
        folderTree.lookup(".tree-cell").setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #ffffff;");
        
        // Split pane styles
        splitPane.setStyle("-fx-background-color: #1a1a1a;");
        splitPane.lookup(".split-pane-divider").setStyle("-fx-background-color: #404040;");
        
        // Status bar styles
        statusBar.setStyle("-fx-background-color: #252525; -fx-text-fill: #ffffff; -fx-padding: 5 10;");
    }

    private void setupEventHandlers() {
        // Delete button handler
        deleteButton.setOnAction(e -> {
            TreeItem<String> selectedFolder = folderTree.getSelectionModel().getSelectedItem();
            if (selectedFolder != null && !selectedFolder.getValue().equals("Root")) {
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Ordner löschen");
                confirmDialog.setHeaderText("Ordner löschen");
                confirmDialog.setContentText("Möchten Sie den Ordner '" + selectedFolder.getValue() + "' wirklich löschen?");
                
                Optional<ButtonType> result = confirmDialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        // Delete the folder from the vault
                        vault.deleteFolder(selectedFolder.getValue());
                        
                        // Remove from tree view
                        selectedFolder.getParent().getChildren().remove(selectedFolder);
                        
                        // Update status
                        statusBar.setText("Ordner erfolgreich gelöscht");
                    } catch (Exception ex) {
                        showError("Fehler beim Löschen des Ordners", ex.getMessage());
                    }
                }
            } else {
                showError("Kein Ordner ausgewählt", "Bitte wählen Sie einen Ordner zum Löschen aus.");
            }
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 