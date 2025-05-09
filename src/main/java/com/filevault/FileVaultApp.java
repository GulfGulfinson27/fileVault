package com.filevault;

import java.io.IOException;
import java.util.prefs.Preferences;

import com.filevault.util.LoggingUtil;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Hauptklasse der FileVault-Anwendung
 */
public class FileVaultApp extends Application {
    
    /** Konstante für den Eintrag des Theme in den Benutzereinstellungen */
    private static final String PREF_DARK_MODE = "darkMode";
    
    /** Haupt-Stage der Anwendung */
    private static Stage primaryStage;
    
    /** Szene für die Login-Ansicht */
    private static Scene loginScene;
    
    /** Szene für die Hauptansicht */
    private static Scene mainScene;
    
    /** Benutzereinstellungen */
    private static Preferences prefs;
    
    /** Flag, das angibt, ob das Dark Mode aktiv ist */
    private static boolean isDarkMode;

    @Override
    public void start(Stage stage) throws Exception {
        LoggingUtil.configureLogger();
        LoggingUtil.logInfo("FileVaultApp", "Starting FileVault application");
        
        primaryStage = stage;
        prefs = Preferences.userNodeForPackage(FileVaultApp.class);
        isDarkMode = prefs.getBoolean(PREF_DARK_MODE, false);
        
        try {
            // Lade Login-Ansicht
            Parent loginRoot = loadFXML("login");
            loginScene = new Scene(loginRoot);
            
            // Füge CSS-Datei hinzu
            String mainCssPath = getClass().getResource("style.css").toExternalForm();
            loginScene.getStylesheets().add(mainCssPath);
            
            // Add appropriate theme CSS
            if (isDarkMode) {
                String darkThemeCssPath = getClass().getResource("css/dark-theme.css").toExternalForm();
                loginScene.getStylesheets().add(darkThemeCssPath);
                loginRoot.getStyleClass().add("dark-theme");
            } else {
                String lightThemeCssPath = getClass().getResource("css/light-theme.css").toExternalForm();
                loginScene.getStylesheets().add(lightThemeCssPath);
            }
            
            // Setze Titel und Icon
            primaryStage.setTitle("FileVault");
            try {
                Image icon = new Image(getClass().getResourceAsStream("icons/icon.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                LoggingUtil.logError("FileVaultApp", "Could not load application icon: " + e.getMessage());
            }
            
            // Konfiguriere Fenster
            primaryStage.setScene(loginScene);
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(500);
            primaryStage.setResizable(true);
            
            // Füge Close-Handler hinzu
            primaryStage.setOnCloseRequest(event -> {
                LoggingUtil.logInfo("FileVaultApp", "Application closing");
                Platform.exit();
            });
            
            // Zeige Anwendung an
            primaryStage.show();
            
            // Apply initial fade-in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), loginRoot);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
            
        } catch (IOException e) {
            LoggingUtil.logError("FileVaultApp", "Error loading FXML: " + e.getMessage());
            showError("Error loading application layout: " + e.getMessage());
        } catch (Exception e) {
            LoggingUtil.logError("FileVaultApp", "Unexpected error: " + e.getMessage());
            showError("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Lädt eine FXML-Datei
     * 
     * @param fxml Der Name der FXML-Datei (ohne Erweiterung)
     * @return Parent-Knoten der geladenen FXML-Datei
     * @throws IOException Falls ein Fehler beim Laden der Datei auftritt
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FileVaultApp.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
    
    /**
     * Zeigt die Hauptansicht der Anwendung
     * 
     * @throws IOException Falls ein Fehler beim Laden der Hauptansicht auftritt
     */
    public static void showMainView() throws IOException {
        LoggingUtil.logInfo("FileVaultApp", "Loading main view");
        
        if (primaryStage == null) {
            LoggingUtil.logError("FileVaultApp", "Primary stage is null");
            return;
        }
        
        if (loginScene != null) {
            // Fade out login scene first
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), loginScene.getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> {
                try {
                    loadAndShowMainView();
                } catch (IOException e) {
                    LoggingUtil.logError("FileVaultApp", "Error loading main view: " + e.getMessage());
                    showError("Error loading main view: " + e.getMessage());
                }
            });
            fadeOut.play();
        } else {
            loadAndShowMainView();
        }
    }
    
    /**
     * Helper method to load and show the main view with animation
     */
    private static void loadAndShowMainView() throws IOException {
        Parent mainRoot = loadFXML("main");
        
        // Apply current theme
        if (isDarkMode) {
            mainRoot.getStyleClass().add("dark-theme");
        }
        
        // Make it initially transparent for animation
        mainRoot.setOpacity(0);
        
        // Create new scene or update existing one
        if (mainScene == null) {
            mainScene = new Scene(mainRoot);
            
            // Add main CSS and appropriate theme CSS
            mainScene.getStylesheets().add(FileVaultApp.class.getResource("style.css").toExternalForm());
            
            if (isDarkMode) {
                String darkThemeCssPath = FileVaultApp.class.getResource("css/dark-theme.css").toExternalForm();
                mainScene.getStylesheets().add(darkThemeCssPath);
            } else {
                String lightThemeCssPath = FileVaultApp.class.getResource("css/light-theme.css").toExternalForm();
                mainScene.getStylesheets().add(lightThemeCssPath);
            }
        } else {
            mainScene.setRoot(mainRoot);
        }
        
        // Switch scenes
        primaryStage.setScene(mainScene);
        
        // Apply fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), mainRoot);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    
    /**
     * Wechselt zwischen Licht- und Dunkel-Modus
     * 
     * @param darkMode true für Dunkel-Modus, false für Licht-Modus
     */
    public static void toggleTheme(boolean darkMode) {
        LoggingUtil.logInfo("FileVaultApp", "Toggling theme. Dark mode: " + darkMode);
        isDarkMode = darkMode;
        
        // Speichere Einstellung
        prefs.putBoolean(PREF_DARK_MODE, darkMode);
        
        // Aktualisiere Styling der aktiven Szene
        Scene currentScene = primaryStage.getScene();
        if (currentScene != null) {
            Parent root = currentScene.getRoot();
            
            // Create transition effect for theme change
            FadeTransition fadeOut = new FadeTransition(Duration.millis(100), root);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.9);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(100), root);
            fadeIn.setFromValue(0.9);
            fadeIn.setToValue(1.0);
            
            fadeOut.setOnFinished(event -> {
                // Update stylesheets
                if (darkMode) {
                    if (!root.getStyleClass().contains("dark-theme")) {
                        root.getStyleClass().add("dark-theme");
                    }
                    
                    // Remove light theme and add dark theme
                    currentScene.getStylesheets().removeIf(css -> css.contains("light-theme.css"));
                    String darkThemeCssPath = FileVaultApp.class.getResource("css/dark-theme.css").toExternalForm();
                    if (!currentScene.getStylesheets().contains(darkThemeCssPath)) {
                        currentScene.getStylesheets().add(darkThemeCssPath);
                    }
                } else {
                    root.getStyleClass().remove("dark-theme");
                    
                    // Remove dark theme and add light theme
                    currentScene.getStylesheets().removeIf(css -> css.contains("dark-theme.css"));
                    String lightThemeCssPath = FileVaultApp.class.getResource("css/light-theme.css").toExternalForm();
                    if (!currentScene.getStylesheets().contains(lightThemeCssPath)) {
                        currentScene.getStylesheets().add(lightThemeCssPath);
                    }
                }
                fadeIn.play();
            });
            
            fadeOut.play();
        }
    }
    
    /**
     * Gibt zurück, ob der Dark Mode aktiv ist
     * 
     * @return true wenn der Dark Mode aktiv ist, sonst false
     */
    public static boolean isDarkMode() {
        return isDarkMode;
    }
    
    /**
     * Zeigt einen Fehler-Dialog an
     * 
     * @param message Die anzuzeigende Fehlermeldung
     */
    private static void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Startet die Anwendung
     * 
     * @param args Kommandozeilenargumente
     */
    public static void main(String[] args) {
        launch(args);
    }
}