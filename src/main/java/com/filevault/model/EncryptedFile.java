package com.filevault.model;

import java.time.LocalDateTime;

/**
 * Represents an encrypted file stored in the vault.
 */
public class EncryptedFile {
    private int id;
    private int folderId;
    private String originalName;
    private String encryptedPath;
    private long sizeBytes;
    private String mimeType;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccess;

    /**
     * Creates a new EncryptedFile.
     * 
     * @param id Unique identifier
     * @param folderId ID of the folder containing this file
     * @param originalName Original name of the file before encryption
     * @param encryptedPath Path to the encrypted file on disk
     * @param sizeBytes Size of the file in bytes
     * @param mimeType MIME type of the file
     * @param createdAt Date and time when the file was added
     * @param lastAccess Date and time when the file was last accessed
     */
    public EncryptedFile(int id, int folderId, String originalName, String encryptedPath, 
                      long sizeBytes, String mimeType, LocalDateTime createdAt, LocalDateTime lastAccess) {
        this.id = id;
        this.folderId = folderId;
        this.originalName = originalName;
        this.encryptedPath = encryptedPath;
        this.sizeBytes = sizeBytes;
        this.mimeType = mimeType;
        this.createdAt = createdAt;
        this.lastAccess = lastAccess;
    }

    public int getId() {
        return id;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getEncryptedPath() {
        return encryptedPath;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(LocalDateTime lastAccess) {
        this.lastAccess = lastAccess;
    }
    
    /**
     * Returns a formatted file size string (e.g., "1.2 MB").
     * 
     * @return Formatted file size
     */
    public String getFormattedSize() {
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        } else if (sizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", sizeBytes / 1024.0);
        } else if (sizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", sizeBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Gets the file extension from the original file name.
     * 
     * @return File extension or empty string if none
     */
    public String getFileExtension() {
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalName.length() - 1) {
            return originalName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    @Override
    public String toString() {
        return originalName;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EncryptedFile other = (EncryptedFile) obj;
        return id == other.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 