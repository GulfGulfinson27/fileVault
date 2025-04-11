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
        
        // Load CSS after scene is created
        try {
            String cssPath = getClass().getResource("/com/filevault/css/styles.css").toExternalForm();
            System.out.println("Loading CSS from: " + cssPath);
            scene.getStylesheets().add(cssPath);
            System.out.println("CSS loaded successfully");
        } catch (Exception e) {
            System.out.println("Error loading CSS: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Set up styles
        setupStyles();
        
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

    private void setupStyles() {
        // Set dark theme colors
        String darkBackground = "#1a1a1a";
        String darkSurface = "#2d2d2d";
        String darkText = "#e0e0e0";
        String darkAccent = "#4a90e2";
        String darkHover = "#3a3a3a";
        String darkSelected = "#4a4a4a";
        String darkHeader = "#252525";
        String darkBorder = "#404040";

        // Apply styles to the main container
        mainContainer.setStyle("-fx-background-color: " + darkBackground + ";");
        mainContainer.setSpacing(10);
        mainContainer.setPadding(new Insets(10));

        // Style buttons with CSS classes
        importButton.getStyleClass().add("button");
        exportButton.getStyleClass().add("button");
        createFolderButton.getStyleClass().add("button");
        deleteButton.getStyleClass().add("button");

        // Style the folder tree
        folderTree.getStyleClass().add("tree-view");

        // Style the file table
        fileTable.getStyleClass().add("table-view");

        // Style the split pane
        splitPane.getStyleClass().add("split-pane");

        // Style the status bar
        statusBar.getStyleClass().add("status-bar");
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