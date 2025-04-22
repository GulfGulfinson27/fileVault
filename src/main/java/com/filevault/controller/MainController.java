package com.filevault.controller;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * Controller für die Hauptansicht der Anwendung.
 * Verwaltet die Interaktion mit Ordnern und Dateien im Tresor.
 */
public class MainController {

    /** TreeView für die Anzeige der Ordner */
    @FXML
    private TreeView<VirtualFolder> folderTreeView;

    /** TableView für die Anzeige der Dateien */
    @FXML
    private TableView<Object> fileTableView;

    /** Spalte für den Dateinamen */
    @FXML
    private TableColumn<Object, String> fileNameColumn;

    /** Spalte für die Dateigröße */
    @FXML
    private TableColumn<Object, String> fileSizeColumn;

    /** Spalte für das Erstellungsdatum */
    @FXML
    private TableColumn<Object, String> fileDateColumn;

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
        // Initialize folder tree
        refreshFolderTree();
        
        // Set up custom cell factory for folder tree
        folderTreeView.setCellFactory(tv -> new TreeCell<VirtualFolder>() {
            @Override
            protected void updateItem(VirtualFolder folder, boolean empty) {
                super.updateItem(folder, empty);
                if (empty || folder == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(2);
                    Label nameLabel = new Label(folder.getName());
                    nameLabel.setStyle("-fx-font-weight: bold;");
                    Label descLabel = new Label(folder.getDescription());
                    descLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 0.9em;");
                    vbox.getChildren().addAll(nameLabel, descLabel);
                    setGraphic(vbox);
                }
            }
        });

        // Set up file table columns
        fileNameColumn.setCellValueFactory(data -> {
            if (data.getValue() instanceof VirtualFolder virtualFolder) {
                return new SimpleStringProperty(virtualFolder.getName());
            } else {
                return new SimpleStringProperty(((EncryptedFile) data.getValue()).getOriginalName());
            }
        });
        
        fileSizeColumn.setCellValueFactory(data -> {
            if (data.getValue() instanceof VirtualFolder) {
                return new SimpleStringProperty("[Ordner]");
            } else {
                return new SimpleStringProperty(((EncryptedFile) data.getValue()).getFormattedSize());
            }
        });
        
        fileDateColumn.setCellValueFactory(data -> {
            if (data.getValue() instanceof VirtualFolder folder) {
                return new SimpleStringProperty(folder.getCreatedAt() != null ? 
                    folder.getCreatedAt().format(dateFormatter) : "");
            } else {
                EncryptedFile file = (EncryptedFile) data.getValue();
                if (file.getCreatedAt() != null) {
                    return new SimpleStringProperty(file.getCreatedAt().format(dateFormatter));
                } else {
                    return new SimpleStringProperty("");
                }
            }
        });

        // Set up custom cell factory for file table
        fileTableView.setRowFactory(tv -> new TableRow<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    if (item instanceof VirtualFolder) {
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Select the first folder if available
        Platform.runLater(() -> {
            if (!folderTreeView.getRoot().getChildren().isEmpty()) {
                folderTreeView.getSelectionModel().select(0);
                handleFolderSelection(null);
            }
        });
    }

    /**
     * Aktualisiert die Baumansicht der Ordner.
     */
    private void refreshFolderTree() {
        List<VirtualFolder> folders = FolderManager.getInstance().getFolders();
        TreeItem<VirtualFolder> rootItem = new TreeItem<>(new VirtualFolder(-1, "Root", "", null));
        
        // Add all root folders (folders without parent)
        for (VirtualFolder folder : folders) {
            if (folder.getParentId() == null) {
                TreeItem<VirtualFolder> folderItem = createTreeItem(folder, folders);
                rootItem.getChildren().add(folderItem);
            }
        }
        
        folderTreeView.setRoot(rootItem);
        folderTreeView.setShowRoot(false);
    }
    
    /**
     * Erstellt einen TreeItem für einen Ordner und seine Unterordner.
     * 
     * @param folder Der Ordner
     * @param allFolders Die Liste aller Ordner
     * @return Der erstellte TreeItem
     */
    private TreeItem<VirtualFolder> createTreeItem(VirtualFolder folder, List<VirtualFolder> allFolders) {
        TreeItem<VirtualFolder> item = new TreeItem<>(folder);
        
        // Add all children
        for (VirtualFolder child : folder.getChildren()) {
            TreeItem<VirtualFolder> childItem = createTreeItem(child, allFolders);
            item.getChildren().add(childItem);
        }
        
        return item;
    }

    /**
     * Verarbeitet die Auswahl eines Ordners.
     * 
     * @param event Das auslösende Mausereignis
     */
    @FXML
    public void handleFolderSelection(MouseEvent event) {
        TreeItem<VirtualFolder> selectedItem = folderTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.getValue() != null) {
            VirtualFolder selectedFolder = selectedItem.getValue();
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
        EncryptedFile selectedFile = (EncryptedFile) fileTableView.getSelectionModel().getSelectedItem();
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
        EncryptedFile selectedFile = (EncryptedFile) fileTableView.getSelectionModel().getSelectedItem();
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
        Object selectedItem = fileTableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Kein Element ausgewählt", "Bitte wählen Sie eine Datei oder einen Ordner zum Löschen aus.");
            return;
        }

        String itemName = selectedItem instanceof VirtualFolder ? 
            ((VirtualFolder) selectedItem).getName() : 
            ((EncryptedFile) selectedItem).getOriginalName();

        if (!showConfirmationDialog("Löschen bestätigen", 
                "Möchten Sie '" + itemName + "' wirklich löschen?")) {
            return;
        }

        try {
            if (selectedItem instanceof VirtualFolder virtualFolder) {
                FolderManager.getInstance().deleteFolder(virtualFolder);
                refreshFolderTree();
                statusLabel.setText("Ordner erfolgreich gelöscht.");
            } else {
                FileStorage.getInstance().deleteFile((EncryptedFile) selectedItem);
                statusLabel.setText("Datei erfolgreich gelöscht.");
            }
            refreshFileList();
        } catch (IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Ordner kann nicht gelöscht werden", 
                    "Der Ordner enthält Unterordner und kann daher nicht gelöscht werden.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Löschfehler", 
                    "Fehler beim Löschen: " + e.getMessage());
        }
    }
    
    /**
     * Aktualisiert die Liste der Dateien im aktuellen Ordner.
     */
    private void refreshFileList() {
        VirtualFolder currentFolder = FolderManager.getInstance().getCurrentFolder();
        if (currentFolder != null) {
            currentFolderLabel.setText(currentFolder.getName());
            
            // Get files and subfolders
            List<EncryptedFile> files = FileStorage.getInstance().getFilesInFolder(currentFolder);
            List<VirtualFolder> subfolders = FolderManager.getInstance().getSubfolders(currentFolder.getId());
            
            // Create a list of all items
            List<Object> items = new ArrayList<>();
            items.addAll(subfolders);
            items.addAll(files);
            
            // Update the table
            fileTableView.setItems(FXCollections.observableArrayList(items));
        } else {
            currentFolderLabel.setText("[Kein Ordner ausgewählt]");
            fileTableView.setItems(FXCollections.observableArrayList());
        }
    }

    /**
     * Erstellt einen neuen Ordner.
     * Zeigt einen Dialog zur Eingabe der Details für den neuen Ordner.
     */
    @FXML
    public void handleNewFolder() {
        // Get the selected folder (parent)
        TreeItem<VirtualFolder> selectedItem = folderTreeView.getSelectionModel().getSelectedItem();
        final Integer parentId = (selectedItem != null && selectedItem.getValue() != null) ? 
            selectedItem.getValue().getId() : null;

        // Create a custom dialog
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Neuer Ordner");
        dialog.setHeaderText("Geben Sie die Details für den neuen Ordner ein");

        // Set the button types
        ButtonType createButtonType = new ButtonType("Erstellen", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the name and description fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Ordnername");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Beschreibung");
        descriptionField.setPrefRowCount(3);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Beschreibung:"), 0, 1);
        grid.add(descriptionField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the name field by default
        Platform.runLater(nameField::requestFocus);

        // Convert the result to a name-description pair when the create button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Pair<>(nameField.getText(), descriptionField.getText());
            }
            return null;
        });

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            String folderName = pair.getKey();
            String description = pair.getValue();
            
            if (!folderName.isEmpty()) {
                try {
                    VirtualFolder folder = FolderManager.getInstance().createFolder(folderName, description, parentId);
                    
                    if (folder != null) {
                        refreshFolderTree();
                        // Select the new folder
                        selectFolderInTree(folder);
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
     * Wählt einen Ordner in der Baumansicht aus.
     * 
     * @param folder Der auszuwählende Ordner
     */
    private void selectFolderInTree(VirtualFolder folder) {
        selectFolderInTree(folderTreeView.getRoot(), folder);
    }
    
    /**
     * Rekursive Hilfsmethode zum Auswählen eines Ordners in der Baumansicht.
     * 
     * @param parent Der übergeordnete TreeItem
     * @param folder Der auszuwählende Ordner
     * @return true, wenn der Ordner gefunden und ausgewählt wurde
     */
    private boolean selectFolderInTree(TreeItem<VirtualFolder> parent, VirtualFolder folder) {
        for (TreeItem<VirtualFolder> child : parent.getChildren()) {
            if (child.getValue().equals(folder)) {
                folderTreeView.getSelectionModel().select(child);
                return true;
            }
            if (selectFolderInTree(child, folder)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Benennt einen Ordner um.
     * Zeigt einen Dialog zur Eingabe des neuen Ordnernamens.
     */
    @FXML
    public void handleRenameFolder() {
        VirtualFolder selectedFolder = folderTreeView.getSelectionModel().getSelectedItem().getValue();
        if (selectedFolder == null) {
            showAlert(Alert.AlertType.WARNING, "Kein Ordner ausgewählt", "Bitte wählen Sie einen Ordner zum Umbenennen aus.");
            return;
        }

        // Create a custom dialog
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Ordner bearbeiten");
        dialog.setHeaderText("Bearbeiten Sie die Details des Ordners");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the name and description fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selectedFolder.getName());
        nameField.setPromptText("Ordnername");
        TextArea descriptionField = new TextArea(selectedFolder.getDescription());
        descriptionField.setPromptText("Beschreibung");
        descriptionField.setPrefRowCount(3);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Beschreibung:"), 0, 1);
        grid.add(descriptionField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the name field by default
        Platform.runLater(nameField::requestFocus);

        // Convert the result to a name-description pair when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Pair<>(nameField.getText(), descriptionField.getText());
            }
            return null;
        });

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            String newName = pair.getKey();
            String newDescription = pair.getValue();
            
            if (!newName.isEmpty()) {
                try {
                    boolean success = FolderManager.getInstance().renameFolder(selectedFolder, newName);
                    selectedFolder.setDescription(newDescription);
                    
                    if (success) {
                        refreshFolderTree();
                        statusLabel.setText("Ordner erfolgreich bearbeitet.");
                    } else {
                        statusLabel.setText("Bearbeiten des Ordners fehlgeschlagen.");
                    }
                } catch (Exception e) {
                    statusLabel.setText("Fehler beim Bearbeiten: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Bearbeitungsfehler", "Fehler beim Bearbeiten des Ordners: " + e.getMessage());
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
        TreeItem<VirtualFolder> selectedItem = folderTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Kein Ordner ausgewählt", "Bitte wählen Sie einen Ordner zum Löschen aus.");
            return;
        }

        VirtualFolder folder = selectedItem.getValue();
        if (folder.getId() == -1) {  // Root folder
            showAlert(Alert.AlertType.WARNING, "Ungültige Aktion", "Der Root-Ordner kann nicht gelöscht werden.");
            return;
        }

        if (!showConfirmationDialog("Ordner löschen", 
                "Möchten Sie den Ordner '" + folder.getName() + "' wirklich löschen?")) {
            return;
        }

        try {
            FolderManager.getInstance().deleteFolder(folder);
            refreshFolderTree();
            statusLabel.setText("Ordner erfolgreich gelöscht.");
        } catch (IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Ordner kann nicht gelöscht werden", 
                    "Der Ordner enthält Unterordner und kann daher nicht gelöscht werden.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Fehler beim Löschen", 
                    "Fehler beim Löschen des Ordners: " + e.getMessage());
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