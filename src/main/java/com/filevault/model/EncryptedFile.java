package com.filevault.model;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Repräsentiert eine verschlüsselte Datei im Tresor.
 */
public class EncryptedFile {
    /** Eindeutige Kennung der Datei */
    private int id;
    
    /** Kennung des Ordners, der die Datei enthält */
    private int folderId;
    
    /** Originalname der Datei vor der Verschlüsselung */
    private String originalName;
    
    /** Pfad zur verschlüsselten Datei auf der Festplatte */
    private String encryptedPath;
    
    /** Größe der Datei in Bytes */
    private long sizeBytes;
    
    /** MIME-Typ der Datei */
    private String mimeType;
    
    /** Datum und Uhrzeit der Erstellung */
    private LocalDateTime createdAt;
    
    /** Datum und Uhrzeit des letzten Zugriffs */
    private LocalDateTime lastAccess;

    /**
     * Erstellt eine neue verschlüsselte Datei.
     * 
     * @param id Eindeutige Kennung
     * @param folderId Kennung des Ordners, der diese Datei enthält
     * @param originalName Originalname der Datei vor der Verschlüsselung
     * @param encryptedPath Pfad zur verschlüsselten Datei auf der Festplatte
     * @param sizeBytes Größe der Datei in Bytes
     * @param mimeType MIME-Typ der Datei
     * @param createdAt Datum und Uhrzeit der Erstellung
     * @param lastAccess Datum und Uhrzeit des letzten Zugriffs
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

    /**
     * Gibt die eindeutige Kennung der Datei zurück.
     * 
     * @return Die Kennung der Datei
     */
    public int getId() {
        return id;
    }

    /**
     * Gibt die Kennung des Ordners zurück, der die Datei enthält.
     * 
     * @return Die Kennung des Ordners
     */
    public int getFolderId() {
        return folderId;
    }

    /**
     * Setzt die Kennung des Ordners, der die Datei enthält.
     * 
     * @param folderId Die neue Kennung des Ordners
     */
    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    /**
     * Gibt den Originalnamen der Datei zurück.
     * 
     * @return Der Originalname der Datei
     */
    public String getOriginalName() {
        return originalName;
    }

    /**
     * Setzt den Originalnamen der Datei.
     * 
     * @param originalName Der neue Originalname
     */
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    /**
     * Gibt den Pfad zur verschlüsselten Datei zurück.
     * 
     * @return Der Pfad zur verschlüsselten Datei
     */
    public String getEncryptedPath() {
        return encryptedPath;
    }

    /**
     * Gibt die Größe der Datei in Bytes zurück.
     * 
     * @return Die Größe der Datei in Bytes
     */
    public long getSizeBytes() {
        return sizeBytes;
    }

    /**
     * Gibt den MIME-Typ der Datei zurück.
     * 
     * @return Der MIME-Typ der Datei
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Setzt den MIME-Typ der Datei.
     * 
     * @param mimeType Der neue MIME-Typ
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Gibt das Erstellungsdatum und die Uhrzeit zurück.
     * 
     * @return Das Erstellungsdatum und die Uhrzeit
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gibt das Datum und die Uhrzeit des letzten Zugriffs zurück.
     * 
     * @return Das Datum und die Uhrzeit des letzten Zugriffs
     */
    public LocalDateTime getLastAccess() {
        return lastAccess;
    }

    /**
     * Setzt das Datum und die Uhrzeit des letzten Zugriffs.
     * 
     * @param lastAccess Das neue Datum und die neue Uhrzeit des letzten Zugriffs
     */
    public void setLastAccess(LocalDateTime lastAccess) {
        this.lastAccess = lastAccess;
    }
    
    /**
     * Gibt die Dateigröße in einem lesbaren Format zurück (z. B. "500 B", "2.0 KB", "2.0 MB" oder "3.0 GB").
     * 
     * @return Die formatierte Dateigröße.
     */
    public String getFormattedSize() {
        if (sizeBytes < 1024) {
            return sizeBytes + " B"; // Größen unter 1 KB in Bytes anzeigen
        } else if (sizeBytes < 1024 * 1024) {
            double sizeInKB = sizeBytes / 1024.0;
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US); // Erzwinge Punkt als Dezimaltrennzeichen
            DecimalFormat df = new DecimalFormat("#.0", symbols);
            return df.format(sizeInKB) + " KB";
        } else if (sizeBytes < 1024 * 1024 * 1024) {
            double sizeInMB = sizeBytes / (1024.0 * 1024.0);
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US); // Erzwinge Punkt als Dezimaltrennzeichen
            DecimalFormat df = new DecimalFormat("#.0", symbols);
            return df.format(sizeInMB) + " MB";
        } else {
            double sizeInGB = sizeBytes / (1024.0 * 1024.0 * 1024.0);
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US); // Erzwinge Punkt als Dezimaltrennzeichen
            DecimalFormat df = new DecimalFormat("#.0", symbols);
            return df.format(sizeInGB) + " GB";
        }
    }
    
    /**
     * Gibt die Dateierweiterung aus dem Originalnamen zurück.
     * 
     * @return Die Dateierweiterung oder einen leeren String, falls keine vorhanden ist
     */
    public String getFileExtension() {
        if (originalName == null) {
            return "";
        }
        
        // Prüfe, ob es sich um eine versteckte Datei handelt (.dateiname)
        if (originalName.startsWith(".") && originalName.length() > 1 && !originalName.substring(1).contains(".")) {
            return originalName.substring(1);
        }
        
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalName.length() - 1) {
            return originalName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * Gibt den Originalnamen der Datei zurück.
     * 
     * @return Der Originalname der Datei
     */
    @Override
    public String toString() {
        return originalName;
    }
    
    /**
     * Vergleicht diese Datei mit einem anderen Objekt.
     * 
     * @param obj Das zu vergleichende Objekt
     * @return true, wenn die Objekte gleich sind, sonst false
     */
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
    
    /**
     * Gibt den Hash-Code dieser Datei zurück.
     * 
     * @return Der Hash-Code
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}