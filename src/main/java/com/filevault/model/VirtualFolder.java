package com.filevault.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert einen virtuellen Ordner in der Anwendung.
 */
public class VirtualFolder {
    /** Eindeutige Kennung des Ordners */
    private int id;
    
    /** Name des Ordners */
    private String name;
    
    /** Beschreibung des Ordners */
    private String description;
    
    /** ID des übergeordneten Ordners */
    private Integer parentId;
    
    /** Erstellungsdatum des Ordners */
    private LocalDateTime createdAt;
    
    /** Liste der untergeordneten Ordner */
    private final List<VirtualFolder> children = new ArrayList<>();
    
    /**
     * Erstellt einen neuen virtuellen Ordner.
     * 
     * @param id Die eindeutige Kennung des Ordners
     * @param name Der Name des Ordners
     * @param description Die Beschreibung des Ordners
     * @param parentId Die ID des übergeordneten Ordners (null für Root-Ordner)
     */
    public VirtualFolder(int id, String name, String description, Integer parentId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentId = parentId;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Gibt die eindeutige Kennung des Ordners zurück.
     * 
     * @return Die eindeutige Kennung
     */
    public int getId() {
        return id;
    }
    
    /**
     * Gibt den Namen des Ordners zurück.
     * 
     * @return Der Ordnername
     */
    public String getName() {
        return name;
    }
    
    /**
     * Setzt den Namen des Ordners.
     * 
     * @param name Der neue Ordnername
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gibt die Beschreibung des Ordners zurück.
     * 
     * @return Die Ordnerbeschreibung
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Setzt die Beschreibung des Ordners.
     * 
     * @param description Die neue Ordnerbeschreibung
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Gibt die ID des übergeordneten Ordners zurück.
     * 
     * @return Die ID des übergeordneten Ordners oder null
     */
    public Integer getParentId() {
        return parentId;
    }
    
    /**
     * Setzt die ID des übergeordneten Ordners.
     * 
     * @param parentId Die ID des übergeordneten Ordners
     */
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }
    
    /**
     * Gibt das Erstellungsdatum des Ordners zurück.
     * 
     * @return Das Erstellungsdatum
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Setzt das Erstellungsdatum des Ordners.
     * 
     * @param createdAt Das neue Erstellungsdatum
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Gibt die Liste der untergeordneten Ordner zurück.
     * 
     * @return Die Liste der untergeordneten Ordner
     */
    public List<VirtualFolder> getChildren() {
        return children;
    }
    
    /**
     * Fügt einen untergeordneten Ordner hinzu.
     * 
     * @param child Der hinzuzufügende Ordner
     */
    public void addChild(VirtualFolder child) {
        children.add(child);
    }
    
    /**
     * Entfernt einen untergeordneten Ordner.
     * 
     * @param child Der zu entfernende Ordner
     */
    public void removeChild(VirtualFolder child) {
        children.remove(child);
    }
    
    /**
     * Gibt den vollständigen Pfad des Ordners zurück.
     * 
     * @return Der vollständige Pfad
     */
    public String getFullPath() {
        return name;
    }
    
    /**
     * Gibt den Namen des Ordners zurück.
     * 
     * @return Der Ordnername
     */
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Vergleicht diesen Ordner mit einem anderen Objekt.
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
        VirtualFolder other = (VirtualFolder) obj;
        return id == other.id && 
               (parentId == null ? other.parentId == null : parentId.equals(other.parentId));
    }
    
    /**
     * Gibt den Hash-Code dieses Ordners zurück.
     * 
     * @return Der Hash-Code
     */
    @Override
    public int hashCode() {
        int result = Integer.hashCode(id);
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        return result;
    }
} 