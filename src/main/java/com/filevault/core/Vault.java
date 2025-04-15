package com.filevault.core;

import com.filevault.model.EncryptedFile;
import com.filevault.model.VirtualFolder;
import com.filevault.storage.FileStorage;
import com.filevault.util.FolderManager;

import java.io.File;
import java.util.List;

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
        VirtualFolder folder = folderManager.getFolderByName(folderName);
        if (folder == null) {
            throw new Exception("Ordner nicht gefunden: " + folderName);
        }

        // Lösche alle Dateien im Ordner
        List<EncryptedFile> files = fileStorage.getFilesInFolder(folder);
        for (EncryptedFile file : files) {
            fileStorage.deleteFile(file);
        }

        // Lösche den Ordner aus der Datenbank
        folderManager.deleteFolder(folder);
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
        VirtualFolder folder = folderManager.getFolderByName(folderName);
        if (folder == null) {
            throw new Exception("Ordner nicht gefunden: " + folderName);
        }
        return fileStorage.importFile(sourceFile, folder);
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
        return fileStorage.exportFile(encryptedFile, destinationFile);
    }
}