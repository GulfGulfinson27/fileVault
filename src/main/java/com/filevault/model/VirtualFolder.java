package com.filevault.model;

/**
 * Represents a virtual folder in the application.
 */
public class VirtualFolder {
    private int id;
    private String name;
    private String description;
    
    /**
     * Creates a new virtual folder.
     * 
     * @param id The unique identifier of the folder
     * @param name The name of the folder
     * @param description The description of the folder
     */
    public VirtualFolder(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    /**
     * Gets the unique identifier of the folder.
     * 
     * @return The unique identifier
     */
    public int getId() {
        return id;
    }
    
    /**
     * Gets the name of the folder.
     * 
     * @return The folder name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of the folder.
     * 
     * @param name The new folder name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the description of the folder.
     * 
     * @return The folder description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the description of the folder.
     * 
     * @param description The new folder description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
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
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 