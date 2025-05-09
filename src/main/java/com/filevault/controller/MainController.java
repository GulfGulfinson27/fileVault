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
import com.filevault.util.LoggingUtil;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
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
import javafx.stage.Window;
import javafx.util.Duration;
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

    /** Button für Theme-Toggle */
    @FXML
    private Button themeToggleButton;

    /** Button für Refresh */
    @FXML
    private Button refreshButton;

    /** Formatierer für Datumsangaben */
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Initialisiert den Controller und die Benutzeroberfläche.
     * Richtet die Ordnerliste und Dateitabelle ein.
     */
    @FXML
    public void initialize() {
        LoggingUtil.logInfo("MainController", "MainController initialized.");
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

        // Add tooltips for folders in the file table and handle right-click options for folders
        fileTableView.setRowFactory(tv -> {
            TableRow<Object> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem renameItem = new MenuItem("Rename");
            renameItem.setOnAction(event -> {
                Object selectedItem = row.getItem();
                if (selectedItem instanceof VirtualFolder folder) {
                    handleRenameFolder(folder);
                } else if (selectedItem instanceof EncryptedFile file) {
                    handleRenameFile(file);
                }
            });
            renameItem.getStyleClass().add("rename-context-item");

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> handleDeleteFile());
            deleteItem.getStyleClass().add("delete-context-item");

            contextMenu.getItems().addAll(renameItem, deleteItem);

            // Show context menu only for non-empty rows
            row.contextMenuProperty().bind(
                Bindings.when(row.emptyProperty())
                        .then((ContextMenu) null)
                        .otherwise(contextMenu)
            );

            // Add tooltips for folders
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem instanceof VirtualFolder folder) {
                    row.setTooltip(new Tooltip(folder.getDescription()));
                } else {
                    row.setTooltip(null);
                }
            });

            // Fix double-click behavior to ensure only files are exported and folders are opened
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    Object selectedItem = row.getItem();
                    LoggingUtil.logInfo("MainController", "Double-click detected. Selected item: " + selectedItem);

                    if (selectedItem instanceof EncryptedFile file) {
                        LoggingUtil.logInfo("MainController", "Exporting file: " + file.getOriginalName());
                        if (!row.isSelected()) { // Ensure the row is selected only once
                            fileTableView.getSelectionModel().select(file);
                            handleExportFile();
                        }
                    } else if (selectedItem instanceof VirtualFolder folder) {
                        LoggingUtil.logInfo("MainController", "Opening folder: " + folder.getName());
                        FolderManager.getInstance().setCurrentFolder(folder);
                        refreshFileList();
                        event.consume(); // Stop further event propagation
                    } else {
                        LoggingUtil.logInfo("MainController", "Unknown item type selected. No action taken.");
                    }
                }
            });

            return row;
        });

        // Select the first folder if available
        Platform.runLater(() -> {
            if (folderTreeView.getRoot() != null && !folderTreeView.getRoot().getChildren().isEmpty()) {
                folderTreeView.getSelectionModel().clearSelection();
                handleFolderSelection(null);
            }
        });

        // Initialize theme toggle button
        initializeThemeToggle();
        
        // Initialize refresh button
        initializeRefreshButton();
    }

    /**
     * Initialisiert den Theme-Toggle-Button
     */
    private void initializeThemeToggle() {
        LoggingUtil.logInfo("MainController", "Initializing theme toggle button");
        
        // Set initial button text based on current theme
        updateThemeToggleText(FileVaultApp.isDarkMode());
        
        // Add click event handler
        themeToggleButton.setOnAction(event -> {
            boolean newDarkMode = !FileVaultApp.isDarkMode();
            
            // Apply rotation animation to the button
            RotateTransition rotateTransition = new RotateTransition(Duration.millis(500), themeToggleButton);
            rotateTransition.setByAngle(newDarkMode ? 360 : -360);
            rotateTransition.setCycleCount(1);
            rotateTransition.setInterpolator(Interpolator.EASE_BOTH);
            
            // Toggle theme and update button text
            rotateTransition.setOnFinished(e -> updateThemeToggleText(newDarkMode));
            rotateTransition.play();
            
            // Toggle theme in app
            FileVaultApp.toggleTheme(newDarkMode);
        });
    }
    
    /**
     * Updates the theme toggle button text with appropriate emoji
     */
    private void updateThemeToggleText(boolean isDarkMode) {
        themeToggleButton.setText(isDarkMode ? "☀️" : "🌕");
        themeToggleButton.setTooltip(new Tooltip(isDarkMode ? "Light Mode" : "Dark Mode"));
    }

    /**
     * Aktualisiert die Baumansicht der Ordner.
     */
    private void refreshFolderTree() {
        LoggingUtil.logInfo("MainController", "Refreshing folder tree.");
        // Create a single root folder
        VirtualFolder rootFolder = new VirtualFolder(-1, "Root", "Root folder", null);
        TreeItem<VirtualFolder> rootItem = new TreeItem<>(rootFolder);
        folderTreeView.setRoot(rootItem);
        folderTreeView.setShowRoot(true);

        // Populate the root folder with other folders
        List<VirtualFolder> folders = FolderManager.getInstance().getFolders();
        for (VirtualFolder folder : folders) {
            if (folder.getParentId() == null) {
                TreeItem<VirtualFolder> folderItem = createTreeItem(folder, folders);
                rootItem.getChildren().add(folderItem);
            }
        }

        // Allow root folder to be selectable
        folderTreeView.getSelectionModel().select(rootItem);
        LoggingUtil.logInfo("MainController", "Folder tree refreshed.");
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
            LoggingUtil.logInfo("MainController", "Folder selected: " + selectedFolder.getName());
            
            // Check if this is the root folder
            if (selectedFolder.getId() == -1) {
                // For root folder, show the top-level folders and their files
                currentFolderLabel.setText(selectedFolder.getName());
                
                // Get only top-level folders
                List<VirtualFolder> topFolders = new ArrayList<>();
                for (VirtualFolder folder : FolderManager.getInstance().getFolders()) {
                    if (folder.getParentId() == null) {
                        topFolders.add(folder);
                    }
                }
                
                fileTableView.setItems(FXCollections.observableArrayList(topFolders));
                LoggingUtil.logInfo("MainController", "Root folder selected, showing top-level folders");
            } else {
                // Normal folder selection
                FolderManager.getInstance().setCurrentFolder(selectedFolder);
                refreshFileList();
            }
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
            LoggingUtil.logInfo("MainController", "File double-clicked for export.");
            handleExportFile();
        }
    }

    /**
     * Öffnet einen Datei-Auswahldialog, um eine Datei zu importieren.
     */
    @FXML
    private void handleImportFile() {
        LoggingUtil.logInfo("MainController", "Import file dialog opened");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Datei importieren");
        
        // Get current window from any control in the scene
        Window currentWindow = folderTreeView.getScene().getWindow();
        File file = fileChooser.showOpenDialog(currentWindow);
        
        if (file != null) {
            importFile(file);
        }
    }

    /**
     * Öffnet einen Verzeichnis-Auswahldialog, um einen Ordner zu importieren.
     */
    @FXML
    private void handleImportFolder() {
        LoggingUtil.logInfo("MainController", "Import folder dialog opened");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Ordner importieren");
        
        // Get current window from any control in the scene
        Window currentWindow = folderTreeView.getScene().getWindow();
        File directory = directoryChooser.showDialog(currentWindow);
        
        if (directory != null) {
            importFolder(directory);
        }
    }

    /**
     * Öffnet einen Dialog zum Exportieren einer Datei.
     */
    @FXML
    private void handleExportFile() {
        LoggingUtil.logInfo("MainController", "Export file dialog opened");
        EncryptedFile selectedFile = (EncryptedFile) fileTableView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "Keine Datei ausgewählt", "Bitte wählen Sie eine Datei zum Exportieren aus.");
            return;
        }
        
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Exportieren nach");
        
        // Get current window from any control in the scene
        Window currentWindow = folderTreeView.getScene().getWindow();
        File directory = directoryChooser.showDialog(currentWindow);
        
        if (directory != null) {
            exportFile(selectedFile, directory);
        }
    }

    /**
     * Benennt eine Datei um.
     * Zeigt einen Dialog zur Eingabe des neuen Namens.
     */
    @FXML
    public void handleRenameFile(EncryptedFile file) {
        if (file == null) {
            showAlert(Alert.AlertType.WARNING, "Keine Datei ausgewählt", "Bitte wählen Sie eine Datei zum Umbenennen aus.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(file.getOriginalName());
        dialog.setTitle("Datei umbenennen");
        dialog.setHeaderText("Geben Sie einen neuen Namen für die Datei ein");
        dialog.setContentText("Neuer Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.isEmpty()) {
                try {
                    boolean success = FileStorage.getInstance().renameFile(file, newName);
                    if (success) {
                        refreshFileList();
                        statusLabel.setText("Datei erfolgreich umbenannt.");
                    } else {
                        statusLabel.setText("Umbenennen der Datei fehlgeschlagen.");
                    }
                } catch (Exception e) {
                    statusLabel.setText("Fehler beim Umbenennen: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    public void handleRenameFile(ActionEvent event) {
        Object selectedItem = fileTableView.getSelectionModel().getSelectedItem();
        if (selectedItem instanceof EncryptedFile file) {
            handleRenameFile(file);
        } else {
            showAlert(Alert.AlertType.WARNING, "Keine Datei ausgewählt", "Bitte wählen Sie eine Datei zum Umbenennen aus.");
        }
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
            if (selectedItem instanceof VirtualFolder folder) {
                FolderManager.getInstance().deleteFolder(folder);
                statusLabel.setText("Ordner erfolgreich gelöscht.");
            } else if (selectedItem instanceof EncryptedFile file) {
                FileStorage.getInstance().deleteFile(file);
                statusLabel.setText("Datei erfolgreich gelöscht.");
            } else {
                throw new IllegalStateException("Unexpected value: " + selectedItem);
            }
            refreshUI();
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Fehler beim Löschen", "Fehler beim Löschen: " + e.getMessage());
        }
    }
    
    /**
     * Aktualisiert die Liste der Dateien im aktuellen Ordner.
     */
    private void refreshFileList() {
        LoggingUtil.logInfo("MainController", "Refreshing file list.");
        VirtualFolder currentFolder = FolderManager.getInstance().getCurrentFolder();
        if (currentFolder != null) {
            currentFolderLabel.setText(currentFolder.getName());

            List<EncryptedFile> files = FileStorage.getInstance().getFilesInFolder(currentFolder);
            List<VirtualFolder> subfolders = FolderManager.getInstance().getSubfolders(currentFolder.getId());

            List<Object> items = new ArrayList<>();
            items.addAll(subfolders);
            items.addAll(files);

            fileTableView.setItems(FXCollections.observableArrayList(items));
            LoggingUtil.logInfo("MainController", "File list refreshed for folder: " + currentFolder.getName());
        } else {
            currentFolderLabel.setText("[Kein Ordner ausgewählt]");
            fileTableView.setItems(FXCollections.observableArrayList());
            LoggingUtil.logInfo("MainController", "No folder selected. File list cleared.");
        }
    }

    /**
     * Refreshes both the folder tree and file table while preserving the folder tree's state.
     */
    private void refreshUI() {
        TreeItem<VirtualFolder> selectedFolder = folderTreeView.getSelectionModel().getSelectedItem();
        refreshFolderTree();
        if (selectedFolder != null) {
            selectFolderInTree(selectedFolder.getValue());
        }
        refreshFileList();
    }

    /**
     * Refreshes both the folder tree and file table while preserving the folder tree's state.
     */
    private void refreshUIAfterRename(VirtualFolder folder) {
        TreeItem<VirtualFolder> selectedFolder = folderTreeView.getSelectionModel().getSelectedItem();
        refreshFolderTree();
        if (folder != null) {
            selectFolderInTree(folder);
        } else if (selectedFolder != null) {
            selectFolderInTree(selectedFolder.getValue());
        }
        refreshFileList();
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
    public void handleRenameFolder(VirtualFolder folder) {
        if (folder == null) {
            showAlert(Alert.AlertType.WARNING, "Kein Ordner ausgewählt", "Bitte wählen Sie einen Ordner zum Umbenennen aus.");
            return;
        }

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Ordner bearbeiten");
        dialog.setHeaderText("Bearbeiten Sie die Details des Ordners");

        ButtonType saveButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(folder.getName());
        nameField.setPromptText("Ordnername");
        TextArea descriptionField = new TextArea(folder.getDescription());
        descriptionField.setPromptText("Beschreibung");
        descriptionField.setPrefRowCount(3);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Beschreibung:"), 0, 1);
        grid.add(descriptionField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Pair<>(nameField.getText(), descriptionField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            String newName = pair.getKey();
            String newDescription = pair.getValue();

            if (!newName.isEmpty()) {
                try {
                    boolean success = FolderManager.getInstance().renameFolder(folder, newName);
                    folder.setDescription(newDescription);

                    if (success) {
                        refreshFolderTree();
                        selectFolderInTree(folder);
                        refreshFileList();
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

    @FXML
    public void handleRenameFolder(ActionEvent event) {
        Object selectedItem = fileTableView.getSelectionModel().getSelectedItem();
        if (selectedItem instanceof VirtualFolder folder) {
            handleRenameFolder(folder);
        } else {
            showAlert(Alert.AlertType.WARNING, "Kein Ordner ausgewählt", "Bitte wählen Sie einen Ordner zum Umbenennen aus.");
        }
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
        Platform.runLater(() -> {
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
                        String errorMessage = "Fehler beim Ändern des Passworts.";
                        if (currentPassword.equals(newPassword)) {
                            errorMessage = "Das neue Passwort darf nicht mit dem alten Passwort übereinstimmen.";
                        } else if (!UserManager.getInstance().authenticate(currentPassword)) {
                            errorMessage = "Das aktuelle Passwort ist falsch.";
                        }
                        statusLabel.setText(errorMessage);
                        showAlert(Alert.AlertType.ERROR, "Passwort Error", errorMessage);
                    }
                } catch (Exception e) {
                    statusLabel.setText("Fehler beim Ändern des Passworts: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Passwort Error", "Fehler beim Ändern des Passworts: " + e.getMessage());
                }
            });
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
        alert.setContentText("""
            Version 1.0
            2025 Phillip Schneider - Projekt FileVault- Java II
        """);
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

    /**
     * Setter for folderTreeView (for testing purposes).
     */
    public void setFolderTreeView(TreeView<VirtualFolder> folderTreeView) {
        this.folderTreeView = folderTreeView;
    }

    /**
     * Setter for fileTableView (for testing purposes).
     */
    public void setFileTableView(TableView<Object> fileTableView) {
        this.fileTableView = fileTableView;
    }

    /**
     * Setter for currentFolderLabel (for testing purposes).
     */
    public void setCurrentFolderLabel(Label currentFolderLabel) {
        this.currentFolderLabel = currentFolderLabel;
    }

    /**
     * Setter for statusLabel (for testing purposes).
     */
    public void setStatusLabel(Label statusLabel) {
        this.statusLabel = statusLabel;
    }

    /**
     * Setter for fileNameColumn (for testing purposes).
     */
    public void setFileNameColumn(TableColumn<Object, String> fileNameColumn) {
        this.fileNameColumn = fileNameColumn;
    }

    /**
     * Setter for fileSizeColumn (for testing purposes).
     */
    public void setFileSizeColumn(TableColumn<Object, String> fileSizeColumn) {
        this.fileSizeColumn = fileSizeColumn;
    }

    /**
     * Setter for fileDateColumn (for testing purposes).
     */
    public void setFileDateColumn(TableColumn<Object, String> fileDateColumn) {
        this.fileDateColumn = fileDateColumn;
    }

    /**
     * Setter für den Theme-Toggle-Button (für Testzwecke).
     * 
     * @param themeToggleButton Der zu setzende Button
     */
    public void setThemeToggleButton(Button themeToggleButton) {
        this.themeToggleButton = themeToggleButton;
    }

    /**
     * Initialisiert den Refresh-Button mit Styling und Animation.
     */
    private void initializeRefreshButton() {
        if (refreshButton != null) {
            refreshButton.getStyleClass().add("refresh-button");
            
            // Set button text with refresh symbol
            refreshButton.setText("↻ Aktualisieren");
            refreshButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            refreshButton.setTooltip(new Tooltip("Ansicht aktualisieren"));
        }
    }

    /**
     * Verarbeitet das Klicken auf den Refresh-Button.
     * Aktualisiert die Ordnerliste und Dateiliste mit Animation.
     */
    @FXML
    public void handleRefresh() {
        LoggingUtil.logInfo("MainController", "Refreshing UI with animation.");
        
        try {
            if (refreshButton != null) {
                // Create icon rotation animation
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(10, javafx.scene.paint.Color.TRANSPARENT);
                javafx.scene.text.Text rotatingIcon = new javafx.scene.text.Text("↻");
                rotatingIcon.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                
                javafx.scene.layout.StackPane iconPane = new javafx.scene.layout.StackPane(circle, rotatingIcon);
                
                // Temporarily replace button content with the animated icon and text
                String originalText = refreshButton.getText();
                javafx.scene.layout.HBox content = new javafx.scene.layout.HBox(5, iconPane, 
                    new javafx.scene.control.Label("Aktualisieren"));
                content.setAlignment(javafx.geometry.Pos.CENTER);
                refreshButton.setGraphic(content);
                refreshButton.setText("");
                
                // Create the rotation animation
                RotateTransition rotateTransition = 
                    new RotateTransition(Duration.seconds(1), rotatingIcon);
                rotateTransition.setFromAngle(0);
                rotateTransition.setToAngle(360);
                rotateTransition.setCycleCount(1);
                rotateTransition.setInterpolator(javafx.animation.Interpolator.LINEAR);
                
                // Start the rotation animation
                rotateTransition.play();
            }
        } catch (Exception ex) {
            // Gracefully handle any animation setup errors in tests
            LoggingUtil.logError("MainController", "Error setting up refresh animation: " + ex.getMessage());
        }
        
        // Fade out the current views
        FadeTransition fadeOutFolders = new FadeTransition(Duration.millis(200), folderTreeView);
        fadeOutFolders.setFromValue(1.0);
        fadeOutFolders.setToValue(0.5);
        
        FadeTransition fadeOutFiles = new FadeTransition(Duration.millis(200), fileTableView);
        fadeOutFiles.setFromValue(1.0);
        fadeOutFiles.setToValue(0.5);
        
        ParallelTransition fadeOut = new ParallelTransition(fadeOutFolders, fadeOutFiles);
        fadeOut.setOnFinished(event -> {
            // Refresh UI after a short delay to make animation visible
            Platform.runLater(() -> {
                try {
                    // Refresh the UI
                    refreshUI();
                    if (statusLabel != null) {
                        statusLabel.setText("Ansicht erfolgreich aktualisiert");
                    }
                    
                    // Fade in the refreshed views
                    FadeTransition fadeInFolders = new FadeTransition(Duration.millis(400), folderTreeView);
                    fadeInFolders.setFromValue(0.5);
                    fadeInFolders.setToValue(1.0);
                    
                    FadeTransition fadeInFiles = new FadeTransition(Duration.millis(400), fileTableView);
                    fadeInFiles.setFromValue(0.5);
                    fadeInFiles.setToValue(1.0);
                    
                    ParallelTransition fadeIn = new ParallelTransition(fadeInFolders, fadeInFiles);
                    fadeIn.play();
                    
                    // Restore original button text after animation completes
                    if (refreshButton != null) {
                        try {
                            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                                Duration.seconds(1.1));
                            pause.setOnFinished(e -> {
                                try {
                                    refreshButton.setGraphic(null);
                                    refreshButton.setText("↻ Aktualisieren");
                                } catch (Exception ex) {
                                    LoggingUtil.logError("MainController", "Error resetting button after animation: " + ex.getMessage());
                                }
                            });
                            pause.play();
                        } catch (Exception ex) {
                            LoggingUtil.logError("MainController", "Error restoring button state: " + ex.getMessage());
                        }
                    }
                    
                } catch (Exception ex) {
                    LoggingUtil.logError("MainController", "Error refreshing UI: " + ex.getMessage());
                    if (statusLabel != null) {
                        statusLabel.setText("Fehler beim Aktualisieren: " + ex.getMessage());
                    }
                    
                    try {
                        showAlert(Alert.AlertType.ERROR, "Aktualisierungsfehler", 
                            "Fehler beim Aktualisieren der Ansicht: " + ex.getMessage());
                    } catch (Exception alertEx) {
                        LoggingUtil.logError("MainController", "Error showing alert: " + alertEx.getMessage());
                    }
                    
                    // Restore original button immediately on error
                    if (refreshButton != null) {
                        try {
                            refreshButton.setGraphic(null);
                            refreshButton.setText("↻ Aktualisieren");
                        } catch (Exception btnEx) {
                            LoggingUtil.logError("MainController", "Error resetting button: " + btnEx.getMessage());
                        }
                    }
                }
            });
        });
        
        // Start fade out
        fadeOut.play();
    }

    /**
     * Setter für den Refresh-Button (für Testzwecke).
     * 
     * @param refreshButton Der zu setzende Button
     */
    public void setRefreshButton(Button refreshButton) {
        this.refreshButton = refreshButton;
    }

    /**
     * Zeigt einen Dialog zum Löschen einer Datei an.
     * 
     * @param fileToDelete Die zu löschende Datei.
     * @return true, wenn der Benutzer das Löschen bestätigt hat, false sonst.
     */
    private boolean showDeleteConfirmationDialog(File fileToDelete) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Löschen bestätigen");
        alert.setHeaderText("Datei löschen");
        alert.setContentText("Möchten Sie die Datei/den Ordner " + fileToDelete.getName() + " wirklich löschen?");
        
        // Apply current theme to dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/filevault/style.css").toExternalForm());
        if (FileVaultApp.isDarkMode()) {
            dialogPane.getStyleClass().add("dark-theme");
        }
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Zeigt einen Dialog zum Umbenennen einer Datei oder eines Ordners an.
     * 
     * @param fileToRename Die umzubenennende Datei oder der umzubenennende Ordner.
     * @return Der neue Name oder null, wenn der Dialog abgebrochen wurde.
     */
    private String showRenameDialog(File fileToRename) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Umbenennen");
        dialog.setHeaderText("Datei/Ordner umbenennen");
        
        // Apply current theme to dialog
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/filevault/style.css").toExternalForm());
        if (FileVaultApp.isDarkMode()) {
            dialogPane.getStyleClass().add("dark-theme");
        }
        
        ButtonType renameButtonType = new ButtonType("Umbenennen", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(renameButtonType, ButtonType.CANCEL);
        
        TextField nameField = new TextField(fileToRename.getName());
        nameField.setMinWidth(250);
        dialog.getDialogPane().setContent(nameField);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == renameButtonType) {
                return nameField.getText();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Importiert eine ausgewählte Datei in den aktuellen Ordner.
     * 
     * @param file Die zu importierende Datei
     */
    private void importFile(File file) {
        LoggingUtil.logInfo("MainController", "Starting file import: " + file.getName());
        // Implementation will be added later
        
        // For now, just show a notification
        showAlert(Alert.AlertType.INFORMATION, "Import", "Datei wird importiert: " + file.getName());
    }
    
    /**
     * Importiert einen ausgewählten Ordner in den aktuellen Ordner.
     * 
     * @param directory Der zu importierende Ordner
     */
    private void importFolder(File directory) {
        LoggingUtil.logInfo("MainController", "Starting folder import: " + directory.getName());
        // Implementation will be added later
        
        // For now, just show a notification
        showAlert(Alert.AlertType.INFORMATION, "Import", "Ordner wird importiert: " + directory.getName());
    }
    
    /**
     * Exportiert eine ausgewählte Datei an einen bestimmten Ort.
     * 
     * @param file Die zu exportierende Datei
     * @param directory Das Zielverzeichnis
     */
    private void exportFile(EncryptedFile file, File directory) {
        LoggingUtil.logInfo("MainController", "Starting file export: " + file.getOriginalName());
        // Implementation will be added later
        
        // For now, just show a notification
        showAlert(Alert.AlertType.INFORMATION, "Export", "Datei wird exportiert: " + file.getOriginalName());
    }
}