package com.filevault;

import java.io.IOException;
import java.util.Objects;

import com.filevault.storage.DatabaseManager;

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
    }

    /**
     * Ändert die aktuelle Ansicht der Anwendung.
     * 
     * @param fxml Der Name der FXML-Datei ohne Erweiterung
     * @throws IOException Wenn das Laden der FXML-Datei fehlschlägt
     */
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
        primaryStage.sizeToScene();
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
        scene.setRoot(loadFXML("main"));
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);
        primaryStage.centerOnScreen();
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
        DatabaseManager.initDatabase();
    }

    /**
     * Schließt die Datenbankverbindungen, wenn die Anwendung beendet wird.
     */
    @Override
    public void stop() {
        DatabaseManager.closeConnections();
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