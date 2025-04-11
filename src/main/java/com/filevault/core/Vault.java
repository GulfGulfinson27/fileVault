package com.filevault.core;

import com.filevault.model.EncryptedFile;
import com.filevault.model.VirtualFolder;
import com.filevault.storage.FileStorage;
import com.filevault.util.FolderManager;

import java.io.File;
import java.util.List;

/**
 * Core class that manages the file vault operations.
 */
public class Vault {
    private static Vault instance;
    private final FileStorage fileStorage;
    private final FolderManager folderManager;

    private Vault() {
        this.fileStorage = FileStorage.getInstance();
        this.folderManager = FolderManager.getInstance();
    }

    public static synchronized Vault getInstance() {
        if (instance == null) {
            instance = new Vault();
        }
        return instance;
    }

    /**
     * Deletes a folder from the vault.
     * @param folderName The name of the folder to delete
     * @throws Exception if the folder cannot be deleted
     */
    public void deleteFolder(String folderName) throws Exception {
        VirtualFolder folder = folderManager.getFolderByName(folderName);
        if (folder == null) {
            throw new Exception("Folder not found: " + folderName);
        }

        // Delete all files in the folder
        List<EncryptedFile> files = fileStorage.getFilesInFolder(folder);
        for (EncryptedFile file : files) {
            fileStorage.deleteFile(file);
        }

        // Delete the folder from the database
        folderManager.deleteFolder(folder);
    }

    /**
     * Imports a file into the vault.
     * @param sourceFile The file to import
     * @param folderName The name of the folder to import into
     * @return The imported encrypted file
     * @throws Exception if the file cannot be imported
     */
    public EncryptedFile importFile(File sourceFile, String folderName) throws Exception {
        VirtualFolder folder = folderManager.getFolderByName(folderName);
        if (folder == null) {
            throw new Exception("Folder not found: " + folderName);
        }
        return fileStorage.importFile(sourceFile, folder);
    }

    /**
     * Exports a file from the vault.
     * @param encryptedFile The encrypted file to export
     * @param destinationFile The destination file
     * @return true if export was successful
     * @throws Exception if the file cannot be exported
     */
    public boolean exportFile(EncryptedFile encryptedFile, File destinationFile) throws Exception {
        return fileStorage.exportFile(encryptedFile, destinationFile);
    }
} 