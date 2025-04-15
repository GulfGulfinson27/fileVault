package com.filevault.controller;

import com.filevault.FileVaultApp;
import com.filevault.model.EncryptedFile;
import com.filevault.model.UserManager;
import com.filevault.model.VirtualFolder;
import com.filevault.storage.FileStorage;
import com.filevault.util.FolderManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller für die Hauptansicht der Anwendung.
 * Verwaltet die Anzeige und Interaktion mit Ordnern und Dateien.
 */
public class MainController {

    @FXML
    private ListView<VirtualFolder> folderListView;

    @FXML
    private TableView<EncryptedFile> fileTableView;

    @FXML
    private TableColumn<EncryptedFile, String> fileNameColumn;

    @FXML
    private TableColumn<EncryptedFile, String> fileSizeColumn;

    @FXML
    private TableColumn<EncryptedFile, String> fileDateColumn;

    @FXML
    private Label currentFolderLabel;

    @FXML
    private Label statusLabel;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Initialisiert die Hauptansicht.
     * Lädt die Ordnerliste und richtet die Dateiansicht ein.
     */
    @FXML
    public void initialize() {
        // Initialisiere die Ordnerliste
        refreshFolderList();

        // Richte die Spalten der Dateiansicht ein
        fileNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOriginalName()));
        fileSizeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedSize()));
        fileDateColumn.setCellValueFactory(data -> {
            if (data.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(data.getValue().getCreatedAt().format(dateFormatter));
            } else {
                return new SimpleStringProperty("");
            }
        });

        // Wähle den ersten Ordner aus, falls verfügbar
        Platform.runLater(() -> {
            if (!folderListView.getItems().isEmpty()) {
                folderListView.getSelectionModel().select(0);
                handleFolderSelection(null);
            }
        });
    }

    /**
     * Aktualisiert die Ordnerliste.
     */
    private void refreshFolderList() {
        List<VirtualFolder> folders = FolderManager.getInstance().getFolders();
        folderListView.setItems(FXCollections.observableArrayList(folders));
    }

    /**
     * Aktualisiert die Dateiliste für den aktuellen Ordner.
     */
    private void refreshFileList() {
        VirtualFolder selectedFolder = FolderManager.getInstance().getCurrentFolder();
        if (selectedFolder != null) {
            List<EncryptedFile> files = FileStorage.getInstance().getFilesInFolder(selectedFolder);
            fileTableView.setItems(FXCollections.observableArrayList(files));
            currentFolderLabel.setText(selectedFolder.getName());
        } else {
            fileTableView.setItems(FXCollections.emptyObservableList());
            currentFolderLabel.setText("[Kein Ordner ausgewählt]");
        }
    }

    /**
     * Verarbeitet die Auswahl eines Ordners.
     *
     * @param event Das Mausereignis (kann null sein).
     */
    @FXML
    public void handleFolderSelection(MouseEvent event) {
        VirtualFolder selectedFolder = folderListView.getSelectionModel().getSelectedItem();
        if (selectedFolder != null) {
            FolderManager.getInstance().setCurrentFolder(selectedFolder);
            refreshFileList();
        }
    }

    /**
     * Verarbeitet die Auswahl einer Datei.
     * Öffnet die Datei bei einem Doppelklick.
     *
     * @param event Das Mausereignis.
     */
    @FXML
    public void handleFileSelection(MouseEvent event) {
        if (event != null && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            handleExportFile();
        }
    }

    /**
     * Importiert eine Datei in den Tresor.
     */
    @FXML
    public void handleImportFile() {
        VirtualFolder currentFolder = FolderManager.getInstance().getCurrentFolder();
        if (currentFolder == null) {
            showAlert(Alert.AlertType.WARNING, "Kein Ordner ausgewählt", "Bitte wählen Sie zuerst einen Ordner aus.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Datei zum Importieren auswählen");

        File file = fileChooser.showOpenDialog(FileVaultApp.getPrimaryStage());
        if (file != null) {
            try {
                statusLabel.setText("Datei wird importiert...");

                EncryptedFile encryptedFile = FileStorage.getInstance().importFile(file, currentFolder);

                if (encryptedFile != null) {
                    refreshFileList();
                    statusLabel.setText("Datei erfolgreich importiert.");
                } else {
                    statusLabel.setText("Import der Datei fehlgeschlagen.");
                }
            } catch (Exception e) {
                statusLabel.setText("Fehler beim Importieren: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Importfehler", "Fehler beim Importieren der Datei: " + e.getMessage());
            }
        }
    }

    /**
     * Exportiert eine Datei aus dem Tresor.
     */
    @FXML
    public void handleExportFile() {
        EncryptedFile selectedFile = fileTableView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "Keine Datei ausgewählt", "Bitte wählen Sie eine Datei zum Exportieren aus.");
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Exportziel auswählen");

        File directory = directoryChooser.showDialog(FileVaultApp.getPrimaryStage());
        if (directory != null) {
            File outputFile = new File(directory, selectedFile.getOriginalName());

            if (outputFile.exists()) {
                boolean overwrite = showConfirmationDialog("Datei existiert bereits",
                        "Eine Datei mit dem gleichen Namen existiert bereits. Möchten Sie sie überschreiben?");

                if (!overwrite) {
                    return;
                }
            }

            try {
                statusLabel.setText("Datei wird exportiert...");

                boolean success = FileStorage.getInstance().exportFile(selectedFile, outputFile);

                if (success) {
                    statusLabel.setText("Datei erfolgreich exportiert.");
                } else {
                    statusLabel.setText("Export der Datei fehlgeschlagen.");
                }
            } catch (Exception e) {
                statusLabel.setText("Fehler beim Exportieren: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Exportfehler", "Fehler beim Exportieren der Datei: " + e.getMessage());
            }
        }
    }

    /**
     * Zeigt einen Bestätigungsdialog an.
     *
     * @param title   Der Titel des Dialogs.
     * @param message Die Nachricht im Dialog.
     * @return true, wenn der Benutzer bestätigt, sonst false.
     */
    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Zeigt einen Benachrichtigungsdialog an.
     *
     * @param type    Der Typ des Dialogs.
     * @param title   Der Titel des Dialogs.
     * @param message Die Nachricht im Dialog.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        alert.showAndWait();
    }
}