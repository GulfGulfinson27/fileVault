package com.filevault.util;

import java.io.InputStream;

import javafx.scene.text.Font;

/**
 * Utility class to load and register custom fonts.
 */
public class FontLoader {
    
    private static boolean fontsLoaded = false;
    
    /**
     * Loads and registers all custom fonts used in the application.
     * This method should be called once at application startup.
     */
    public static void loadFonts() {
        if (fontsLoaded) {
            return;
        }
        
        try {
            // Load Material Icons font
            InputStream materialIconsStream = FontLoader.class.getResourceAsStream(
                    "/com/filevault/icons/material-icons.woff2");
            
            if (materialIconsStream != null) {
                Font.loadFont(materialIconsStream, 18);
                LoggingUtil.logInfo("FontLoader", "Material Icons font loaded successfully");
            } else {
                LoggingUtil.logWarning("FontLoader", "Could not load Material Icons font - resource not found");
            }
            
            fontsLoaded = true;
        } catch (Exception e) {
            LoggingUtil.logError("FontLoader", "Failed to load fonts: " + e.getMessage());
        }
    }
    
    /**
     * Gets the character code for a specific Material Icon name.
     * 
     * @param iconName the name of the Material Icon
     * @return the Unicode character for the icon
     */
    public static String getMaterialIcon(String iconName) {
        switch (iconName) {
            case "folder": return "\uE2C7";
            case "folder_open": return "\uE2C8";
            case "insert_drive_file": return "\uE24D";
            case "delete": return "\uE872";
            case "edit": return "\uE3C9";
            case "add": return "\uE145";
            case "refresh": return "\uE5D5";
            case "dark_mode": return "\uE51C";
            case "light_mode": return "\uE518";
            case "upload": return "\uE2C6";
            case "download": return "\uE2C4";
            case "home": return "\uE88A";
            case "lock": return "\uE897";
            case "person": return "\uE7FD";
            case "settings": return "\uE8B8";
            default: return "";
        }
    }
} 