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
 * Controller for the main application view.
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
     * Refreshes the folder list.
     */
    private void refreshFolderList() {
        List<VirtualFolder> folders = FolderManager.getInstance().getFolders();
        folderListView.setItems(FXCollections.observableArrayList(folders));
    }
    
    /**
     * Refreshes the file list for the current folder.
     */
    private void refreshFileList() {
        VirtualFolder selectedFolder = FolderManager.getInstance().getCurrentFolder();
        if (selectedFolder != null) {
            List<EncryptedFile> files = FileStorage.getInstance().getFilesInFolder(selectedFolder);
            fileTableView.setItems(FXCollections.observableArrayList(files));
            currentFolderLabel.setText(selectedFolder.getName());
        } else {
            fileTableView.setItems(FXCollections.emptyObservableList());
            currentFolderLabel.setText("[No Folder Selected]");
        }
    }
    
    /**
     * Handles folder selection.
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
     * Handles file selection.
     */
    @FXML
    public void handleFileSelection(MouseEvent event) {
        if (event != null && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            handleExportFile();
        }
    }
    
    /**
     * Imports a file into the vault.
     */
    @FXML
    public void handleImportFile() {
        VirtualFolder currentFolder = FolderManager.getInstance().getCurrentFolder();
        if (currentFolder == null) {
            showAlert(Alert.AlertType.WARNING, "No Folder Selected", "Please select a folder first.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Import");
        
        File file = fileChooser.showOpenDialog(FileVaultApp.getPrimaryStage());
        if (file != null) {
            try {
                statusLabel.setText("Importing file...");
                
                EncryptedFile encryptedFile = FileStorage.getInstance().importFile(file, currentFolder);
                
                if (encryptedFile != null) {
                    refreshFileList();
                    statusLabel.setText("File imported successfully.");
                } else {
                    statusLabel.setText("Failed to import file.");
                }
            } catch (Exception e) {
                statusLabel.setText("Error importing file: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Import Error", "Failed to import file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Exports a file from the vault.
     */
    @FXML
    public void handleExportFile() {
        EncryptedFile selectedFile = fileTableView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "No File Selected", "Please select a file to export.");
            return;
        }
        
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Export Location");
        
        File directory = directoryChooser.showDialog(FileVaultApp.getPrimaryStage());
        if (directory != null) {
            File outputFile = new File(directory, selectedFile.getOriginalName());
            
            // Check if file already exists
            if (outputFile.exists()) {
                boolean overwrite = showConfirmationDialog("File Exists", 
                        "A file with the same name already exists. Do you want to overwrite it?");
                
                if (!overwrite) {
                    return;
                }
            }
            
            try {
                statusLabel.setText("Exporting file...");
                
                boolean success = FileStorage.getInstance().exportFile(selectedFile, outputFile);
                
                if (success) {
                    statusLabel.setText("File exported successfully.");
                } else {
                    statusLabel.setText("Failed to export file.");
                }
            } catch (Exception e) {
                statusLabel.setText("Error exporting file: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Export Error", "Failed to export file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Renames a file.
     */
    @FXML
    public void handleRenameFile() {
        EncryptedFile selectedFile = fileTableView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "No File Selected", "Please select a file to rename.");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(selectedFile.getOriginalName());
        dialog.setTitle("Rename File");
        dialog.setHeaderText("Enter a new name for the file");
        dialog.setContentText("New name:");
        
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.isEmpty()) {
                try {
                    boolean success = FileStorage.getInstance().renameFile(selectedFile, newName);
                    
                    if (success) {
                        refreshFileList();
                        statusLabel.setText("File renamed successfully.");
                    } else {
                        statusLabel.setText("Failed to rename file.");
                    }
                } catch (Exception e) {
                    statusLabel.setText("Error renaming file: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Rename Error", "Failed to rename file: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Deletes a file.
     */
    @FXML
    public void handleDeleteFile() {
        EncryptedFile selectedFile = fileTableView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "No File Selected", "Please select a file to delete.");
            return;
        }
        
        boolean confirm = showConfirmationDialog("Delete File", 
                "Are you sure you want to delete \"" + selectedFile.getOriginalName() + "\"?");
        
        if (confirm) {
            try {
                boolean success = FileStorage.getInstance().deleteFile(selectedFile);
                
                if (success) {
                    refreshFileList();
                    statusLabel.setText("File deleted successfully.");
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
     * Creates a new folder.
     */
    @FXML
    public void handleNewFolder() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("New Folder");
        dialog.setHeaderText("Create a new folder");
        dialog.setContentText("Folder name:");
        
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
                        statusLabel.setText("Folder created successfully.");
                    } else {
                        statusLabel.setText("Failed to create folder.");
                    }
                } catch (Exception e) {
                    statusLabel.setText("Error creating folder: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Folder Error", "Failed to create folder: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Renames a folder.
     */
    @FXML
    public void handleRenameFolder() {
        VirtualFolder selectedFolder = folderListView.getSelectionModel().getSelectedItem();
        if (selectedFolder == null) {
            showAlert(Alert.AlertType.WARNING, "No Folder Selected", "Please select a folder to rename.");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(selectedFolder.getName());
        dialog.setTitle("Rename Folder");
        dialog.setHeaderText("Enter a new name for the folder");
        dialog.setContentText("New name:");
        
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.isEmpty()) {
                try {
                    boolean success = FolderManager.getInstance().renameFolder(selectedFolder, newName);
                    
                    if (success) {
                        refreshFolderList();
                        folderListView.getSelectionModel().select(selectedFolder);
                        handleFolderSelection(null);
                        statusLabel.setText("Folder renamed successfully.");
                    } else {
                        statusLabel.setText("Failed to rename folder.");
                    }
                } catch (Exception e) {
                    statusLabel.setText("Error renaming folder: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Rename Error", "Failed to rename folder: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Deletes a folder.
     */
    @FXML
    public void handleDeleteFolder() {
        VirtualFolder selectedFolder = folderListView.getSelectionModel().getSelectedItem();
        if (selectedFolder == null) {
            showAlert(Alert.AlertType.WARNING, "No Folder Selected", "Please select a folder to delete.");
            return;
        }
        
        boolean confirm = showConfirmationDialog("Delete Folder", 
                "Are you sure you want to delete the folder \"" + selectedFolder.getName() + "\" and all its contents?");
        
        if (confirm) {
            try {
                boolean success = FolderManager.getInstance().deleteFolder(selectedFolder);
                
                if (success) {
                    refreshFolderList();
                    handleFolderSelection(null);
                    statusLabel.setText("Folder deleted successfully.");
                } else {
                    statusLabel.setText("Failed to delete folder.");
                }
            } catch (Exception e) {
                statusLabel.setText("Error deleting folder: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Delete Error", "Failed to delete folder: " + e.getMessage());
            }
        }
    }
    
    /**
     * Changes the master password.
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
                showAlert(Alert.AlertType.ERROR, "Password Error", "New passwords do not match.");
                return;
            }
            
            if (newPassword.length() < 8) {
                showAlert(Alert.AlertType.ERROR, "Password Error", "New password must be at least 8 characters long.");
                return;
            }
            
            try {
                boolean success = UserManager.getInstance().changePassword(currentPassword, newPassword);
                
                if (success) {
                    statusLabel.setText("Password changed successfully.");
                    showAlert(Alert.AlertType.INFORMATION, "Password Changed", "Your master password has been changed successfully.");
                } else {
                    statusLabel.setText("Failed to change password.");
                    showAlert(Alert.AlertType.ERROR, "Password Error", "Failed to change password. Current password may be incorrect.");
                }
            } catch (Exception e) {
                statusLabel.setText("Error changing password: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Password Error", "Failed to change password: " + e.getMessage());
            }
        });
    }
    
    /**
     * Shows the settings dialog.
     */
    @FXML
    public void handleSettings() {
        showAlert(Alert.AlertType.INFORMATION, "Settings", "Settings functionality not implemented yet.");
    }
    
    /**
     * Shows the about dialog.
     */
    @FXML
    public void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About FileVault");
        alert.setHeaderText("FileVault");
        alert.setContentText("A secure file organization and encryption system.\n" +
                             "Version 1.0\n\n" +
                             "© 2023 FileVault");
        
        alert.showAndWait();
    }
    
    /**
     * Exits the application.
     */
    @FXML
    public void handleExit() {
        boolean confirm = showConfirmationDialog("Exit", "Are you sure you want to exit FileVault?");
        
        if (confirm) {
            Platform.exit();
        }
    }
    
    /**
     * Shows an alert dialog.
     * 
     * @param type The type of alert
     * @param title The title of the alert
     * @param message The message to display
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
     * Shows a confirmation dialog.
     * 
     * @param title The title of the dialog
     * @param message The message to display
     * @return true if the user confirmed, false otherwise
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