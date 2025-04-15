package com.filevault.model;

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
    
    /**
     * Erstellt einen neuen virtuellen Ordner.
     * 
     * @param id Die eindeutige Kennung des Ordners
     * @param name Der Name des Ordners
     * @param description Die Beschreibung des Ordners
     */
    public VirtualFolder(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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
        return id == other.id;
    }
    
    /**
     * Gibt den Hash-Code dieses Ordners zurück.
     * 
     * @return Der Hash-Code
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 