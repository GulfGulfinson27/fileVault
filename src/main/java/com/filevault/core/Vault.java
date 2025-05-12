package com.filevault.core;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.filevault.model.EncryptedFile;
import com.filevault.model.VirtualFolder;
import com.filevault.storage.FileStorage;
import com.filevault.util.FolderManager;
import com.filevault.util.LoggingUtil;

import javafx.scene.control.TreeItem;

/**
 * Kernklasse, die die Operationen des Datei-Tresors verwaltet.
 * Diese Klasse bietet Funktionen zum Importieren, Exportieren und Löschen von Dateien und Ordnern.
 */
public class Vault {
    private static Vault instance;
    private final FileStorage fileStorage;
    private final FolderManager folderManager;

    /**
     * Privater Konstruktor, um die Singleton-Instanz zu erstellen.
     */
    private Vault() {
        this.fileStorage = FileStorage.getInstance();
        this.folderManager = FolderManager.getInstance();
    }

    /**
     * Gibt die Singleton-Instanz des Tresors zurück.
     *
     * @return Die Singleton-Instanz von Vault.
     */
    public static synchronized Vault getInstance() {
        if (instance == null) {
            instance = new Vault();
        }
        return instance;
    }

    /**
     * Löscht einen Ordner aus dem Tresor.
     *
     * @param folderName Der Name des zu löschenden Ordners.
     * @throws Exception Wenn der Ordner nicht gelöscht werden kann.
     */
    public void deleteFolder(String folderName) throws Exception {
        LoggingUtil.logInfo("Vault", "Attempting to delete folder: " + folderName);
        VirtualFolder folder = folderManager.getFolderByName(folderName);
        if (folder == null) {
            LoggingUtil.logError("Vault", "Folder not found: " + folderName);
            throw new Exception("Ordner nicht gefunden: " + folderName);
        }

        List<EncryptedFile> files = fileStorage.getFilesInFolder(folder);
        for (EncryptedFile file : files) {
            fileStorage.deleteFile(file);
            LoggingUtil.logInfo("Vault", "Deleted file: " + file.getOriginalName());
        }

        folderManager.deleteFolder(folder);
        LoggingUtil.logInfo("Vault", "Folder deleted successfully: " + folderName);
    }

    /**
     * Löscht einen Ordner und alle seine Unterordner rekursiv aus dem Tresor.
     *
     * @param folderName Der Name des zu löschenden Ordners.
     * @throws Exception Wenn der Ordner nicht gelöscht werden kann.
     */
    public void deleteFolderRecursive(String folderName) throws Exception {
        LoggingUtil.logInfo("Vault", "Attempting to recursively delete folder: " + folderName);
        VirtualFolder folder = folderManager.getFolderByName(folderName);
        if (folder == null) {
            LoggingUtil.logError("Vault", "Folder not found: " + folderName);
            throw new Exception("Ordner nicht gefunden: " + folderName);
        }

        folderManager.deleteFolderRecursive(folder);
        LoggingUtil.logInfo("Vault", "Folder and all subfolders deleted successfully: " + folderName);
    }

    /**
     * Importiert eine Datei in den Tresor.
     *
     * @param sourceFile Die zu importierende Datei.
     * @param folderName Der Name des Ordners, in den die Datei importiert werden soll.
     * @return Die importierte verschlüsselte Datei.
     * @throws Exception Wenn die Datei nicht importiert werden kann.
     */
    public EncryptedFile importFile(File sourceFile, String folderName) throws Exception {
        LoggingUtil.logInfo("Vault", "Attempting to import file: " + sourceFile.getName() + " into folder: " + folderName);
        VirtualFolder folder = folderManager.getFolderByName(folderName);
        if (folder == null) {
            LoggingUtil.logError("Vault", "Folder not found: " + folderName);
            throw new Exception("Ordner nicht gefunden: " + folderName);
        }
        EncryptedFile importedFile = fileStorage.importFile(sourceFile, folder);
        LoggingUtil.logInfo("Vault", "File imported successfully: " + sourceFile.getName());
        return importedFile;
    }

    /**
     * Exportiert eine Datei aus dem Tresor.
     *
     * @param encryptedFile   Die verschlüsselte Datei, die exportiert werden soll.
     * @param destinationFile Die Zieldatei, in die exportiert werden soll.
     * @return true, wenn der Export erfolgreich war.
     * @throws Exception Wenn die Datei nicht exportiert werden kann.
     */
    public boolean exportFile(EncryptedFile encryptedFile, File destinationFile) throws Exception {
        LoggingUtil.logInfo("Vault", "Attempting to export file: " + encryptedFile.getOriginalName() + " to destination: " + destinationFile.getAbsolutePath());
        boolean success = fileStorage.exportFile(encryptedFile, destinationFile);
        if (success) {
            LoggingUtil.logInfo("Vault", "File exported successfully: " + encryptedFile.getOriginalName());
        } else {
            LoggingUtil.logError("Vault", "Failed to export file: " + encryptedFile.getOriginalName());
        }
        return success;
    }

    /**
     * Benennt einen Ordner im Tresor um.
     *
     * @param oldName Der aktuelle Name des Ordners.
     * @param newName Der neue Name des Ordners.
     * @throws Exception Wenn der Ordner nicht umbenannt werden kann.
     */
    public void renameFolder(String oldName, String newName) throws Exception {
        LoggingUtil.logInfo("Vault", "Attempting to rename folder: " + oldName + " to: " + newName);
        VirtualFolder folder = folderManager.getFolderByName(oldName);
        if (folder == null) {
            LoggingUtil.logError("Vault", "Folder not found: " + oldName);
            throw new Exception("Ordner nicht gefunden: " + oldName);
        }

        boolean success = folderManager.renameFolder(folder, newName);
        if (success) {
            LoggingUtil.logInfo("Vault", "Folder renamed successfully: " + oldName + " to: " + newName);
        } else {
            LoggingUtil.logError("Vault", "Failed to rename folder: " + oldName);
            throw new Exception("Fehler beim Umbenennen des Ordners: " + oldName);
        }
    }

    /**
     * Gibt die Ordnerstruktur als TreeItem zurück.
     *
     * @return Die Wurzel des Ordnerbaums.
     */
    public TreeItem<String> getFolderTree() {
        TreeItem<String> root = new TreeItem<>("Root");
        List<VirtualFolder> folders = folderManager.getAllFolders();

        // Erstelle die Baumstruktur
        Map<Integer, TreeItem<String>> folderMap = new HashMap<>();
        folderMap.put(null, root);

        for (VirtualFolder folder : folders) {
            TreeItem<String> item = new TreeItem<>(folder.getName());
            folderMap.put(folder.getId(), item);

            TreeItem<String> parent = folderMap.get(folder.getParentId());
            if (parent != null) {
                parent.getChildren().add(item);
            }
        }

        return root;
    }
}