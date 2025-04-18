package com.filevault.controller;

import com.filevault.FileVaultApp;
import com.filevault.model.EncryptedFile;
import com.filevault.model.UserManager;
import com.filevault.model.VirtualFolder;
import com.filevault.storage.FileStorage;
import com.filevault.util.FolderManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
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
 * Verwaltet die Interaktion mit Ordnern und Dateien im Tresor.
 */
public class MainController {

    /** ListView für die Anzeige der Ordner */
    @FXML
    private ListView<VirtualFolder> folderListView;

    /** TableView für die Anzeige der Dateien */
    @FXML
    private TableView<EncryptedFile> fileTableView;

    /** Spalte für den Dateinamen */
    @FXML
    private TableColumn<EncryptedFile, String> fileNameColumn;

    /** Spalte für die Dateigröße */
    @FXML
    private TableColumn<EncryptedFile, String> fileSizeColumn;

    /** Spalte für das Erstellungsdatum */
    @FXML
    private TableColumn<EncryptedFile, String> fileDateColumn;

    /** Label für den aktuellen Ordner */
    @FXML
    private Label currentFolderLabel;

    /** Label für Statusmeldungen */
    @FXML
    private Label statusLabel;

    /** Formatierer für Datumsangaben */
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Initialisiert den Controller und die Benutzeroberfläche.
     * Richtet die Ordnerliste und Dateitabelle ein.
     */
    @FXML
    public void initialize() {
        // Initialize folder list
        refreshFolderList();

        // Set up file table columns
        fileNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOriginalName()));
        fileSizeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedSize()));
        fileDateColumn.setCellValueFactory(data -> {
            if (data.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(data.getValue().getCreatedAt().format(dateFormatter));
            } else {
                return new SimpleStringProperty("");
            }
        });

        // Select the first folder if available
        Platform.runLater(() -> {
            if (!folderListView.getItems().isEmpty()) {
                folderListView.getSelectionModel().select(0);
                handleFolderSelection(null);
            }
        });
    }

    /**
     * Aktualisiert die Liste der Ordner.
     */
    private void refreshFolderList() {
        List<VirtualFolder> folders = FolderManager.getInstance().getFolders();
        folderListView.setItems(FXCollections.observableArrayList(folders));
    }

    /**
     * Aktualisiert die Liste der Dateien im aktuellen Ordner.
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
     * @param event Das auslösende Mausereignis
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
     * Bei Doppelklick wird die Datei exportiert.
     * 
     * @param event Das auslösende Mausereignis
     */
    @FXML
    public void handleFileSelection(MouseEvent event) {
        if (event != null && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            handleExportFile();
        }
    }

    /**
     * Importiert eine Datei in den Tresor.
     * Zeigt einen Dateiauswahldialog und verarbeitet den Import.
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
     * Zeigt einen Ordnerauswahldialog und verarbeitet den Export.
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
     * Benennt eine Datei um.
     * Zeigt einen Dialog zur Eingabe des neuen Namens.
     */
    @FXML
    public void handleRenameFile() {
        EncryptedFile selectedFile = fileTableView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "Keine Datei ausgewählt", "Bitte wählen Sie eine Datei zum Umbenennen aus.");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(selectedFile.getOriginalName());
        dialog.setTitle("Datei umbenennen");
        dialog.setHeaderText("Geben Sie einen neuen Namen für die Datei ein");
        dialog.setContentText("Neuer Name:");
        
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.isEmpty()) {
                try {
                    boolean success = FileStorage.getInstance().renameFile(selectedFile, newName);
                    
                    if (success) {
                        refreshFileList();
                        statusLabel.setText("Datei erfolgreich umbenannt.");
                    } else {
                        statusLabel.setText("Umbenennen der Datei fehlgeschlagen.");
                    }
                } catch (Exception e) {
                    statusLabel.setText("Fehler beim Umbenennen: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Umbenennungsfehler", "Fehler beim Umbenennen der Datei: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Löscht eine Datei aus dem Tresor.
     * Zeigt einen Bestätigungsdialog vor dem Löschen.
     */
    @FXML
    public void handleDeleteFile() {
        EncryptedFile selectedFile = fileTableView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "Keine Datei ausgewählt", "Bitte wählen Sie eine Datei zum Löschen aus.");
            return;
        }
        
        boolean confirm = showConfirmationDialog("Datei löschen", 
                "Bist du sicher, dass du \"" + selectedFile.getOriginalName() + "\" loeschen moechtest?");
        
        if (confirm) {
            try {
                boolean success = FileStorage.getInstance().deleteFile(selectedFile);
                
                if (success) {
                    refreshFileList();
                    statusLabel.setText("Datei erfolgreich gelöscht.");
                } else {
                    statusLabel.setText("Failed to delete file.");
                }
            } catch (Exception e) {
                statusLabel.setText("Error deleting file: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Delete Error", "Failed to delete file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Erstellt einen neuen Ordner.
     * Zeigt einen Dialog zur Eingabe des Ordnernamens.
     */
    @FXML
    public void handleNewFolder() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Neuer Ordner");
        dialog.setHeaderText("Geben Sie einen Namen für den neuen Ordner ein");
        dialog.setContentText("Ordnername:");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(folderName -> {
            if (!folderName.isEmpty()) {
                try {
                    VirtualFolder folder = FolderManager.getInstance().createFolder(folderName);
                    
                    if (folder != null) {
                        refreshFolderList();
                        folderListView.getSelectionModel().select(folder);
                        handleFolderSelection(null);
                        statusLabel.setText("Ordner erfolgreich erstellt.");
                    } else {
                        statusLabel.setText("Erstellen des Ordners fehlgeschlagen.");
                    }
                } catch (Exception e) {
                    statusLabel.setText("Fehler beim Erstellen des Ordners: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Erstellungsfehler", "Fehler beim Erstellen des Ordners: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Benennt einen Ordner um.
     * Zeigt einen Dialog zur Eingabe des neuen Ordnernamens.
     */
    @FXML
    public void handleRenameFolder() {
        VirtualFolder selectedFolder = folderListView.getSelectionModel().getSelectedItem();
        if (selectedFolder == null) {
            showAlert(Alert.AlertType.WARNING, "Kein Ordner ausgewählt", "Bitte wählen Sie einen Ordner zum Umbenennen aus.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedFolder.getName());
        dialog.setTitle("Ordner umbenennen");
        dialog.setHeaderText("Geben Sie einen neuen Namen für den Ordner ein");
        dialog.setContentText("Neuer Name:");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.isEmpty()) {
                try {
                    boolean success = FolderManager.getInstance().renameFolder(selectedFolder, newName);
                    
                    if (success) {
                        refreshFolderList();
                        statusLabel.setText("Ordner erfolgreich umbenannt.");
                    } else {
                        statusLabel.setText("Umbenennen des Ordners fehlgeschlagen.");
                    }
                } catch (Exception e) {
                    statusLabel.setText("Fehler beim Umbenennen: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Umbenennungsfehler", "Fehler beim Umbenennen des Ordners: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Löscht einen Ordner.
     * Zeigt einen Bestätigungsdialog vor dem Löschen.
     */
    @FXML
    public void handleDeleteFolder() {
        VirtualFolder selectedFolder = folderListView.getSelectionModel().getSelectedItem();
        if (selectedFolder == null) {
            showAlert(Alert.AlertType.WARNING, "Kein Ordner ausgewählt", "Bitte wählen Sie einen Ordner zum Löschen aus.");
            return;
        }

        boolean confirm = showConfirmationDialog("Ordner löschen", 
                "Bist du sicher, dass du den Ordner \"" + selectedFolder.getName() + "\" löschen möchtest?");

        if (confirm) {
            try {
                boolean success = FolderManager.getInstance().deleteFolder(selectedFolder);
                
                if (success) {
                    refreshFolderList();
                    statusLabel.setText("Ordner erfolgreich gelöscht.");
                } else {
                    statusLabel.setText("Löschen des Ordners fehlgeschlagen.");
                }
            } catch (Exception e) {
                statusLabel.setText("Fehler beim Löschen: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Löschfehler", "Fehler beim Löschen des Ordners: " + e.getMessage());
            }
        }
    }
    
    /**
     * Ändert das Benutzerpasswort.
     * Zeigt einen Dialog zur Eingabe des neuen Passworts.
     */
    @FXML
    public void handleChangePassword() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Change Master Password");
        dialog.setHeaderText("Enter your current password and a new password");
        
        // Set the button types
        ButtonType changeButtonType = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);
        
        // Create the password fields
        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Current password");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");
        
        // Enable/Disable change button depending on whether passwords are entered
        Button changeButton = (Button) dialog.getDialogPane().lookupButton(changeButtonType);
        changeButton.setDisable(true);
        
        currentPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            changeButton.setDisable(
                    newValue.trim().isEmpty() || 
                    newPasswordField.getText().trim().isEmpty() || 
                    confirmPasswordField.getText().trim().isEmpty());
        });
        
        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            changeButton.setDisable(
                    currentPasswordField.getText().trim().isEmpty() || 
                    newValue.trim().isEmpty() || 
                    confirmPasswordField.getText().trim().isEmpty());
        });
        
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            changeButton.setDisable(
                    currentPasswordField.getText().trim().isEmpty() || 
                    newPasswordField.getText().trim().isEmpty() || 
                    newValue.trim().isEmpty());
        });
        
        // Create and add the layout
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Current password:"), 0, 0);
        grid.add(currentPasswordField, 1, 0);
        grid.add(new Label("New password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Confirm new password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        Platform.runLater(currentPasswordField::requestFocus);
        
        // Convert the result to a password when the change button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                return new String[]{
                        currentPasswordField.getText(),
                        newPasswordField.getText(),
                        confirmPasswordField.getText()
                };
            }
            return null;
        });
        
        Optional<String[]> result = dialog.showAndWait();
        
        result.ifPresent(passwords -> {
            String currentPassword = passwords[0];
            String newPassword = passwords[1];
            String confirmPassword = passwords[2];
            
            if (!newPassword.equals(confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Passwort Error", "Passwörter stimmen nicht überein.");
                return;
            }
            
            if (newPassword.length() < 8) {
                showAlert(Alert.AlertType.ERROR, "Passwort Error", "Passwort muss mindestens 8 Zeichen lang sein.");
                return;
            }
            
            try {
                boolean success = UserManager.getInstance().changePassword(currentPassword, newPassword);
                
                if (success) {
                    statusLabel.setText("Passwort erfolgreich geändert.");
                    showAlert(Alert.AlertType.INFORMATION, "Passwort geändert", "Dein Passwort wurde erfolgreich geändert.");
                } else {
                        statusLabel.setText("Fehler beim Ändern des Passworts.");
                    showAlert(Alert.AlertType.ERROR, "Passwort Error", "Fehler beim Ändern des Passworts.");
                }
            } catch (Exception e) {
                statusLabel.setText("Fehler beim Ändern des Passworts: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Passwort Error", "Fehler beim Ändern des Passworts: " + e.getMessage());
            }
        });
    }
    
    /**
     * Öffnet die Einstellungen.
     */
    @FXML
    public void handleSettings() {
        showAlert(Alert.AlertType.INFORMATION, "Einstellungen", "Einstellungen wurden noch nicht implementiert.");
    }
    
    /**
     * Zeigt Informationen über die Anwendung an.
     */
    @FXML
    public void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Über FileVault");
        alert.setHeaderText("FileVault");
        alert.setContentText("Version 1.0\n" +
                "2025 Phillip Schneider - Projekt FileVault- Java II");
        alert.showAndWait();
    }
    
    /**
     * Beendet die Anwendung.
     */
    @FXML
    public void handleExit() {
        boolean confirm = showConfirmationDialog("Beenden", "Möchten Sie FileVault wirklich beenden?");
        if (confirm) {
            Platform.exit();
        }
    }
    
    /**
     * Zeigt eine Warnung oder Fehlermeldung an.
     * 
     * @param type Der Typ der Meldung (WARNUNG oder FEHLER)
     * @param title Der Titel der Meldung
     * @param message Der Inhalt der Meldung
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
    
    /**
     * Zeigt einen Bestätigungsdialog an.
     * 
     * @param title Der Titel des Dialogs
     * @param message Der Inhalt des Dialogs
     * @return true, wenn der Benutzer bestätigt hat, sonst false
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
}