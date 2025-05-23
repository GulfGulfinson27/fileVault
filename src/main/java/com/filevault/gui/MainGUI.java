package com.filevault.gui;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.filevault.core.Vault;
import com.filevault.model.VirtualFolder;
import com.filevault.util.FolderManager;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Hauptklasse für die grafische Benutzeroberfläche (GUI) der Anwendung.
 * Diese Klasse verwaltet die Anzeige und Interaktion mit Ordnern und Dateien.
 */
public class MainGUI extends Application {
    private Vault vault;
    private TableView<File> fileTable;
    private TreeView<String> folderTree;
    private Label statusBar;
    private Button importButton, exportButton, createFolderButton, deleteButton;
    private VBox mainContainer;
    private SplitPane splitPane;

    /**
     * Startet die GUI-Anwendung.
     *
     * @param primaryStage Die Hauptbühne der Anwendung.
     */
    @Override
    public void start(Stage primaryStage) {
        // Initialisiere den Tresor
        vault = Vault.getInstance();
        
        // Initialisiere die GUI-Komponenten
        initializeComponents();
        
        // Erstelle die Szene
        Scene scene = new Scene(mainContainer, 800, 600);
        
        // Wende Stile direkt an
        applyStyles();
        
        // Richte Event-Handler ein
        setupEventHandlers();
        
        // Konfiguriere die Bühne
        primaryStage.setTitle("FileVault");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Hauptmethode der Anwendung.
     *
     * @param args Die Befehlszeilenargumente.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initialisiert die GUI-Komponenten.
     */
    private void initializeComponents() {
        // Erstelle den Hauptcontainer
        mainContainer = new VBox();
        mainContainer.setSpacing(10);
        mainContainer.setPadding(new Insets(10));

        // Erstelle die Buttons
        importButton = new Button("Import");
        exportButton = new Button("Export");
        createFolderButton = new Button("Create Folder");
        deleteButton = new Button("Delete");

        // Erstelle die Statusleiste
        statusBar = new Label("Ready");

        // Erstelle den Ordnerbaum
        folderTree = new TreeView<>();
        TreeItem<String> rootItem = new TreeItem<>("Root");
        folderTree.setRoot(rootItem);
        folderTree.setShowRoot(true);

        // Erstelle die Dateitabelle
        fileTable = new TableView<>();
        TableColumn<File, String> nameColumn = new TableColumn<>("Name");
        TableColumn<File, String> sizeColumn = new TableColumn<>("Size");
        TableColumn<File, String> dateColumn = new TableColumn<>("Date");
        // Suppress type safety warning for adding columns
        @SuppressWarnings("unchecked")
        TableColumn<File, String>[] columns = new TableColumn[] { nameColumn, sizeColumn, dateColumn };
        fileTable.getColumns().addAll(columns);

        // Erstelle das SplitPane
        splitPane = new SplitPane();
        VBox leftPane = new VBox(folderTree);
        VBox rightPane = new VBox(fileTable);
        splitPane.getItems().addAll(leftPane, rightPane);
        splitPane.setDividerPositions(0.3);

        // Füge Komponenten zum Hauptcontainer hinzu
        HBox buttonBar = new HBox(10, importButton, exportButton, createFolderButton, deleteButton);
        mainContainer.getChildren().addAll(buttonBar, splitPane, statusBar);
    }

    /**
     * Wendet Stile auf die GUI-Komponenten an.
     */
    private void applyStyles() {
        // Stile für die Dateitabelle
        fileTable.setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #404040; -fx-border-width: 1;");
        
        // Stile für die Statusleiste
        statusBar.setStyle("-fx-background-color: #252525; -fx-text-fill: #ffffff; -fx-padding: 5 10;");
    }

    /**
     * Richtet die Event-Handler für die GUI-Komponenten ein.
     */
    private void setupEventHandlers() {
        // Event-Handler für den Löschen-Button
        deleteButton.setOnAction(e -> {
            TreeItem<String> selectedFolder = folderTree.getSelectionModel().getSelectedItem();
            if (selectedFolder != null && !selectedFolder.getValue().equals("Root")) {
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Ordner löschen");
                confirmDialog.setHeaderText("Ordner löschen");
                
                // Prüfen, ob es Unterordner gibt
                boolean hasSubfolders = false;
                try {
                    VirtualFolder folder = FolderManager.getInstance().getFolderByName(selectedFolder.getValue());
                    if (folder != null) {
                        List<VirtualFolder> subfolders = FolderManager.getInstance().getSubfolders(folder.getId());
                        hasSubfolders = !subfolders.isEmpty();
                    }
                } catch (Exception ex) {
                    showError("Fehler", "Fehler beim Prüfen der Unterordner: " + ex.getMessage());
                    return;
                }
                
                String confirmMessage;
                if (hasSubfolders) {
                    confirmMessage = "Der Ordner '" + selectedFolder.getValue() + "' enthält Unterordner.\n" +
                            "Möchten Sie diesen Ordner und ALLE darin enthaltenen Unterordner und Dateien löschen?\n" +
                            "WARNUNG: Diese Aktion kann nicht rückgängig gemacht werden!";
                } else {
                    confirmMessage = "Möchten Sie den Ordner '" + selectedFolder.getValue() + "' wirklich löschen?";
                }
                confirmDialog.setContentText(confirmMessage);
                
                Optional<ButtonType> result = confirmDialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        // Lösche den Ordner aus dem Tresor
                        if (hasSubfolders) {
                            vault.deleteFolderRecursive(selectedFolder.getValue());
                        } else {
                            vault.deleteFolder(selectedFolder.getValue());
                        }
                        
                        // Entferne den Ordner aus der Baumansicht
                        selectedFolder.getParent().getChildren().remove(selectedFolder);
                        
                        // Aktualisiere die Statusleiste
                        statusBar.setText("Ordner" + (hasSubfolders ? " und alle Unterordner" : "") + " erfolgreich gelöscht");
                    } catch (Exception ex) {
                        showError("Fehler beim Löschen des Ordners", ex.getMessage());
                    }
                }
            } else {
                showError("Fehler", "Bitte wählen Sie einen Ordner zum Löschen aus.");
            }
        });

        // Füge einen Refresh-Button hinzu
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            try {
                // Aktualisiere die Ordner- und Dateiansicht
                folderTree.setRoot(vault.getFolderTree());
                refreshFileTable();
                statusBar.setText("Ansicht erfolgreich aktualisiert");
            } catch (Exception ex) {
                showError("Fehler beim Aktualisieren", ex.getMessage());
            }
        });

        // Füge den Refresh-Button zur Button-Leiste hinzu
        HBox buttonBar = new HBox(10, importButton, exportButton, createFolderButton, deleteButton, refreshButton);
        mainContainer.getChildren().set(0, buttonBar);
    }

    /**
     * Aktualisiert die Dateitabelle nach einer Änderung.
     */
    private void refreshFileTable() {
        fileTable.getItems().clear();
        // Hier können Sie die aktualisierten Dateien aus dem Tresor abrufen und hinzufügen
        // Beispiel: fileTable.getItems().addAll(vault.getFilesForCurrentFolder());
    }

    /**
     * Zeigt eine Fehlermeldung an.
     *
     * @param title   Der Titel der Fehlermeldung.
     * @param message Die Nachricht der Fehlermeldung.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}