package com.filevault.controller;

import com.filevault.FileVaultApp;
import com.filevault.model.UserManager;
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
 * Controller für die Authentifizierungsansicht.
 * Verwaltet die Anmeldung und Registrierung von Benutzern.
 */
public class AuthController {

    @FXML
    private Label messageLabel;

    @FXML
    private VBox loginForm;

    @FXML
    private VBox registerForm;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button toggleFormButton;

    private boolean isLoginView = true;

    /**
     * Initialisiert die Ansicht.
     * Zeigt das Registrierungsformular an, wenn kein Benutzer existiert.
     */
    @FXML
    public void initialize() {
        boolean userExists = UserManager.getInstance().userExists();

        // Wenn kein Benutzer existiert, zeige das Registrierungsformular
        if (!userExists) {
            toggleForm();
        }
    }

    /**
     * Verarbeitet die Anmeldung eines Benutzers.
     * Überprüft das eingegebene Passwort und authentifiziert den Benutzer.
     */
    @FXML
    public void handleLogin() {
        String password = passwordField.getText();
        if (password.isEmpty()) {
            showMessage("Bitte Passwort eingeben", true);
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
     * Verarbeitet die Registrierung eines neuen Benutzers.
     * Überprüft die Eingaben und erstellt ein neues Benutzerkonto.
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
     * Schaltet zwischen Anmelde- und Registrierungsformular um.
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
     * Zeigt eine Nachricht im Formular an.
     *
     * @param message Die anzuzeigende Nachricht.
     * @param isError Gibt an, ob es sich um eine Fehlermeldung handelt.
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