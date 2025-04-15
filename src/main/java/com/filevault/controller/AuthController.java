package com.filevault.controller;

import com.filevault.FileVaultApp;
import com.filevault.model.UserManager;
import com.filevault.security.PasswordUtils;
import com.filevault.storage.DatabaseManager;
import com.filevault.util.FolderManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Controller für die Authentifizierung und Registrierung von Benutzern.
 * Diese Klasse verwaltet den Login- und Registrierungsprozess der Anwendung.
 */
public class AuthController {

    /** Label für Statusmeldungen */
    @FXML
    private Label messageLabel;
    
    /** Container für das Login-Formular */
    @FXML
    private VBox loginForm;
    
    /** Container für das Registrierungsformular */
    @FXML
    private VBox registerForm;
    
    /** Passwortfeld für die Anmeldung */
    @FXML
    private PasswordField passwordField;
    
    /** Passwortfeld für die Registrierung */
    @FXML
    private PasswordField newPasswordField;
    
    /** Passwortfeld zur Bestätigung bei der Registrierung */
    @FXML
    private PasswordField confirmPasswordField;
    
    /** Button zum Wechseln zwischen Login und Registrierung */
    @FXML
    private Button toggleFormButton;
    
    /** Flag, das angibt, ob die Login-Ansicht aktiv ist */
    private boolean isLoginView = true;
    
    /**
     * Initialisiert den Controller und prüft, ob bereits ein Benutzer existiert.
     * Falls kein Benutzer existiert, wird das Registrierungsformular angezeigt.
     */
    @FXML
    public void initialize() {
        boolean userExists = UserManager.getInstance().userExists();
        
        if (!userExists) {
            toggleForm();
        }
    }
    
    /**
     * Verarbeitet den Login-Vorgang.
     * Prüft das eingegebene Passwort und leitet bei erfolgreicher Authentifizierung
     * zur Hauptansicht weiter.
     */
    @FXML
    public void handleLogin() {
        String password = passwordField.getText();
        
        if (password.isEmpty()) {
            showMessage("Bitte geben Sie Ihr Passwort ein", true);
            return;
        }
        
        boolean authenticated = UserManager.getInstance().authenticate(password);
        
        if (authenticated) {
            try {
                FolderManager.getInstance().initialize();
                
                showMessage("Anmeldung erfolgreich!", false);
                
                PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                pause.setOnFinished(event -> {
                    try {
                        FileVaultApp.showMainView();
                    } catch (IOException e) {
                        showMessage("Fehler beim Laden der Hauptansicht: " + e.getMessage(), true);
                    }
                });
                pause.play();
                
            } catch (Exception e) {
                showMessage("Fehler beim Initialisieren der Anwendung: " + e.getMessage(), true);
            }
        } else {
            showMessage("Ungültiges Passwort", true);
        }
    }
    
    /**
     * Verarbeitet den Registrierungsvorgang.
     * Prüft die eingegebenen Passwörter auf Gültigkeit und erstellt bei Erfolg
     * einen neuen Benutzer.
     */
    @FXML
    public void handleRegister() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Bitte geben Sie Ihr Passwort ein und bestätigen Sie es", true);
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showMessage("Die Passwörter stimmen nicht überein", true);
            return;
        }
        
        if (newPassword.length() < 8) {
            showMessage("Das Passwort muss mindestens 8 Zeichen lang sein", true);
            return;
        }
        
        try {
            UserManager.getInstance().createUser(newPassword);
            
            FolderManager.getInstance().createBaseStructure();
            
            showMessage("Konto erfolgreich erstellt!", false);
            
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(event -> toggleForm());
            pause.play();
            
        } catch (Exception e) {
            showMessage("Fehler beim Erstellen des Kontos: " + e.getMessage(), true);
        }
    }
    
    /**
     * Wechselt zwischen Login- und Registrierungsformular.
     * Aktualisiert die Sichtbarkeit der Formulare und den Text des Toggle-Buttons.
     */
    @FXML
    public void toggleForm() {
        isLoginView = !isLoginView;
        
        if (isLoginView) {
            loginForm.setVisible(true);
            registerForm.setVisible(false);
            toggleFormButton.setText("Konto erstellen");
        } else {
            loginForm.setVisible(false);
            registerForm.setVisible(true);
            toggleFormButton.setText("Zurück zur Anmeldung");
        }
        
        messageLabel.setVisible(false);
        
        passwordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
    
    /**
     * Zeigt eine Nachricht im Message-Label an.
     * 
     * @param message Die anzuzeigende Nachricht
     * @param isError Gibt an, ob es sich um eine Fehlermeldung handelt
     */
    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        
        if (isError) {
            messageLabel.getStyleClass().remove("success-text");
            messageLabel.getStyleClass().add("error-text");
        } else {
            messageLabel.getStyleClass().remove("error-text");
            messageLabel.getStyleClass().add("success-text");
        }
    }
} 