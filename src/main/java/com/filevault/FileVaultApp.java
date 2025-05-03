package com.filevault;

import java.io.IOException;
import java.util.Objects;

import com.filevault.storage.DatabaseManager;
import com.filevault.util.LoggingUtil;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Die Hauptklasse der FileVault-Anwendung.
 * Diese Klasse erweitert die JavaFX Application-Klasse und ist für das Starten
 * und Verwalten der Benutzeroberfläche zuständig.
 */
public class FileVaultApp extends Application {

    /** Die aktuelle Szene der Anwendung */
    private static Scene scene;
    
    /** Das Hauptfenster der Anwendung */
    private static Stage primaryStage;

    /**
     * Startet die Anwendung und initialisiert die Benutzeroberfläche.
     * 
     * @param stage Das Hauptfenster der Anwendung
     * @throws IOException Wenn das Laden der FXML-Datei fehlschlägt
     */
    @Override
    public void start(Stage stage) throws IOException {
        LoggingUtil.logInfo("FileVaultApp", "Starting FileVault application.");
        try {
            primaryStage = stage;
            scene = new Scene(loadFXML("login"), 400, 600);
            scene.getStylesheets().add(getClass().getResource("/com/filevault/css/dark-theme.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("FileVault - Sicherer Dateispeicher");
            try {
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/filevault/icons/app-icon.png"))));
            } catch (Exception e) {
                System.err.println("Anwendungs-Icon konnte nicht geladen werden: " + e.getMessage());
            }
            stage.setResizable(true);
            stage.show();
            LoggingUtil.logInfo("FileVaultApp", "FileVault application started successfully.");
        } catch (IOException e) {
            LoggingUtil.logSevere("FileVaultApp", "Failed to load FXML file: " + e.getMessage());
            throw e;
        } catch (NullPointerException e) {
            LoggingUtil.logSevere("FileVaultApp", "A required resource was not found: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LoggingUtil.logSevere("FileVaultApp", "An unexpected error occurred: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Ändert die aktuelle Ansicht der Anwendung.
     * 
     * @param fxml Der Name der FXML-Datei ohne Erweiterung
     * @throws IOException Wenn das Laden der FXML-Datei fehlschlägt
     */
    public static void setRoot(String fxml) throws IOException {
        LoggingUtil.logInfo("FileVaultApp", "Changing view to: " + fxml);
        scene.setRoot(loadFXML(fxml));
        primaryStage.sizeToScene();
        LoggingUtil.logInfo("FileVaultApp", "View changed to: " + fxml);
    }

    /**
     * Lädt eine FXML-Datei und gibt das resultierende Parent-Objekt zurück.
     * 
     * @param fxml Der Name der FXML-Datei ohne Erweiterung
     * @return Das geladene Parent-Objekt
     * @throws IOException Wenn das Laden der FXML-Datei fehlschlägt
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FileVaultApp.class.getResource("/com/filevault/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    /**
     * Zeigt die Hauptansicht der Anwendung an.
     * 
     * @throws IOException Wenn das Laden der FXML-Datei fehlschlägt
     */
    public static void showMainView() throws IOException {
        LoggingUtil.logInfo("FileVaultApp", "Displaying main view.");
        scene.setRoot(loadFXML("main"));
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);
        primaryStage.centerOnScreen();
        LoggingUtil.logInfo("FileVaultApp", "Main view displayed.");
    }
    
    /**
     * Gibt das Hauptfenster der Anwendung zurück.
     * 
     * @return Das Hauptfenster (Stage) der Anwendung
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Initialisiert die Datenbank und lädt Benutzerdaten.
     */
    @Override
    public void init() {
        LoggingUtil.logInfo("FileVaultApp", "Initializing database.");
        DatabaseManager.initDatabase();
        LoggingUtil.logInfo("FileVaultApp", "Database initialized.");
    }

    /**
     * Schließt die Datenbankverbindungen, wenn die Anwendung beendet wird.
     */
    @Override
    public void stop() {
        LoggingUtil.logInfo("FileVaultApp", "Closing database connections.");
        DatabaseManager.closeConnections();
        LoggingUtil.logInfo("FileVaultApp", "Database connections closed.");
    }

    /**
     * Der Einstiegspunkt der Anwendung.
     * 
     * @param args Die Kommandozeilenargumente
     */
    public static void main(String[] args) {
        launch();
    }
}