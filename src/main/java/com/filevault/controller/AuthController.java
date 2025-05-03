package com.filevault.controller;

import java.io.IOException;

import com.filevault.FileVaultApp;
import com.filevault.model.UserManager;
import com.filevault.util.FolderManager;
import com.filevault.util.LoggingUtil;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

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
        LoggingUtil.logInfo("AuthController", "Initializing AuthController.");
        boolean userExists = UserManager.getInstance().userExists();
        LoggingUtil.logInfo("AuthController", "User exists: " + userExists);

        if (userExists) {
            toggleFormButton.setVisible(false); // Hide the "Konto erstellen" button if a user exists
        } else {
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
        LoggingUtil.logInfo("AuthController", "Login attempt started.");
        String password = passwordField.getText();
        
        if (password.isEmpty()) {
            showMessage("Bitte geben Sie Ihr Passwort ein", true);
            LoggingUtil.logError("AuthController", "Login failed: Password field is empty.");
            return;
        }
        
        boolean authenticated = UserManager.getInstance().authenticate(password);
        
        if (authenticated) {
            try {
                FolderManager.getInstance().initialize();
                
                showMessage("Anmeldung erfolgreich!", false);
                LoggingUtil.logInfo("AuthController", "Login successful.");
                
                PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                pause.setOnFinished(event -> {
                    try {
                        FileVaultApp.showMainView();
                    } catch (IOException e) {
                        showMessage("Fehler beim Laden der Hauptansicht: " + e.getMessage(), true);
                        LoggingUtil.logError("AuthController", "Error loading main view: " + e.getMessage());
                    }
                });
                pause.play();
                
            } catch (Exception e) {
                showMessage("Fehler beim Initialisieren der Anwendung: " + e.getMessage(), true);
                LoggingUtil.logError("AuthController", "Error initializing application: " + e.getMessage());
            }
        } else {
            showMessage("Ungültiges Passwort", true);
            LoggingUtil.logError("AuthController", "Login failed: Invalid password.");
        }
    }
    
    /**
     * Verarbeitet den Registrierungsvorgang.
     * Prüft die eingegebenen Passwörter auf Gültigkeit und erstellt bei Erfolg
     * einen neuen Benutzer.
     */
    @FXML
    public void handleRegister() {
        LoggingUtil.logInfo("AuthController", "Registration attempt started.");
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            LoggingUtil.logError("AuthController", "Registration failed: Empty password fields.");
            showMessage("Bitte geben Sie Ihr Passwort ein und bestätigen Sie es", true);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            LoggingUtil.logError("AuthController", "Registration failed: Passwords do not match.");
            showMessage("Die Passwörter stimmen nicht überein", true);
            return;
        }

        if (newPassword.length() < 8) {
            LoggingUtil.logError("AuthController", "Registration failed: Password too short.");
            showMessage("Das Passwort muss mindestens 8 Zeichen lang sein", true);
            return;
        }

        try {
            UserManager.getInstance().createUser(newPassword);
            FolderManager.getInstance().createBaseStructure();
            showMessage("Konto erfolgreich erstellt!", false);
            LoggingUtil.logInfo("AuthController", "User registered successfully.");

            toggleFormButton.setVisible(false); // Hide the "Konto erstellen" button after user creation

            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(event -> toggleForm());
            pause.play();
        } catch (Exception e) {
            LoggingUtil.logError("AuthController", "Error during registration: " + e.getMessage());
            showMessage("Fehler beim Erstellen des Kontos: " + e.getMessage(), true);
        }
    }
    
    /**
     * Wechselt zwischen Login- und Registrierungsformular.
     * Aktualisiert die Sichtbarkeit der Formulare und den Text des Toggle-Buttons.
     */
    @FXML
    public void toggleForm() {
        LoggingUtil.logInfo("AuthController", "Toggling form. Current view: " + (isLoginView ? "Login" : "Register"));
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
        LoggingUtil.logInfo("AuthController", "Form toggled. New view: " + (isLoginView ? "Login" : "Register"));
    }
    
    /**
     * Zeigt eine Nachricht im Message-Label an.
     * 
     * @param message Die anzuzeigende Nachricht
     * @param isError Gibt an, ob es sich um eine Fehlermeldung handelt
     */
    protected void showMessage(String message, boolean isError) {
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