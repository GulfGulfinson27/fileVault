package com.filevault;

import com.filevault.storage.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class FileVaultApp extends Application {

    private static Scene scene;
    private static Stage primaryStage;

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
            System.err.println("Could not load application icon: " + e.getMessage());
        }
        stage.setResizable(true);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
        primaryStage.sizeToScene();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FileVaultApp.class.getResource("/com/filevault/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void showMainView() throws IOException {
        scene.setRoot(loadFXML("main"));
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);
        primaryStage.centerOnScreen();
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void init() {
        // Initialize database and load user data
        DatabaseManager.initDatabase();
    }

    @Override
    public void stop() {
        // Cleanup resources when the application is closed
        DatabaseManager.closeConnections();
    }

    public static void main(String[] args) {
        launch();
    }
} 