package com.filevault.controller;

import java.io.IOException;

import com.filevault.FileVaultApp;
import com.filevault.model.UserManager;
import com.filevault.util.FolderManager;
import com.filevault.util.LoggingUtil;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
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
    
    /** App logo image */
    @FXML
    private ImageView logoImage;
    
    /** Login button */
    @FXML
    private Button loginButton;
    
    /** Register button */
    @FXML
    private Button registerButton;
    
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
        
        // Apply initial animations
        applyEntryAnimations();
    }
    
    /**
     * Applies entry animations to login screen elements.
     */
    private void applyEntryAnimations() {
        // Logo animation - scale and fade with more dramatic start
        logoImage.setScaleX(0.2);
        logoImage.setScaleY(0.2);
        logoImage.setOpacity(0);
        logoImage.setRotate(-10);  // Slight initial rotation for more epic effect
        
        ScaleTransition scaleLogo = new ScaleTransition(Duration.millis(500), logoImage);  // Faster animation
        scaleLogo.setToX(1.2);  // Overshoot scale for bounce effect
        scaleLogo.setToY(1.2);
        scaleLogo.setDelay(Duration.millis(100));  // Shorter delay
        
        RotateTransition rotateLogo = new RotateTransition(Duration.millis(500), logoImage);
        rotateLogo.setToAngle(0);  // Rotate to normal
        rotateLogo.setDelay(Duration.millis(100));
        
        ScaleTransition scaleBackLogo = new ScaleTransition(Duration.millis(200), logoImage);
        scaleBackLogo.setToX(1.0);  // Back to normal scale (bounce effect)
        scaleBackLogo.setToY(1.0);
        scaleBackLogo.setDelay(Duration.millis(500));
        
        FadeTransition fadeLogo = new FadeTransition(Duration.millis(400), logoImage);  // Faster fade
        fadeLogo.setToValue(1.0);
        fadeLogo.setDelay(Duration.millis(100));  // Shorter delay
        
        // Form container animation - slide up and fade with more dramatic effect
        Node formContainer = loginForm.getParent();
        formContainer.setOpacity(0);
        formContainer.setTranslateY(100);  // Start from further down for more dramatic slide
        
        TranslateTransition slideForm = new TranslateTransition(Duration.millis(500), formContainer);  // Faster slide
        slideForm.setToY(0);
        slideForm.setDelay(Duration.millis(300));  // Shorter delay
        slideForm.setInterpolator(Interpolator.EASE_OUT);  // Smoother deceleration
        
        FadeTransition fadeForm = new FadeTransition(Duration.millis(400), formContainer);  // Faster fade
        fadeForm.setToValue(1.0);
        fadeForm.setDelay(Duration.millis(300));  // Shorter delay
        
        // Play animations
        ParallelTransition logoEntryAnimation = new ParallelTransition(scaleLogo, fadeLogo, rotateLogo);
        ParallelTransition formAnimation = new ParallelTransition(slideForm, fadeForm);
        
        SequentialTransition sequence = new SequentialTransition(logoEntryAnimation, scaleBackLogo, formAnimation);
        sequence.play();
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
            
            // Shake animation for empty field
            applyShakeAnimation(passwordField);
            return;
        }
        
        // Disable login button and show loading animation
        loginButton.setDisable(true);
        loginButton.setText("Anmelden...");
        
        // Add slight delay to show the loading state
        PauseTransition loadingDelay = new PauseTransition(Duration.millis(500));
        loadingDelay.setOnFinished(event -> {
            boolean authenticated = UserManager.getInstance().authenticate(password);
            
            if (authenticated) {
                try {
                    FolderManager.getInstance().initialize();
                    
                    showMessage("Anmeldung erfolgreich!", false);
                    LoggingUtil.logInfo("AuthController", "Login successful.");
                    
                    // Apply exit animation before transition
                    applyExitAnimation();
                    
                } catch (Exception e) {
                    loginButton.setDisable(false);
                    loginButton.setText("Anmelden");
                    showMessage("Fehler beim Initialisieren der Anwendung: " + e.getMessage(), true);
                    LoggingUtil.logError("AuthController", "Error initializing application: " + e.getMessage());
                }
            } else {
                loginButton.setDisable(false);
                loginButton.setText("Anmelden");
                showMessage("Ungültiges Passwort", true);
                LoggingUtil.logError("AuthController", "Login failed: Invalid password.");
                
                // Shake animation for wrong password
                applyShakeAnimation(passwordField);
            }
        });
        loadingDelay.play();
    }
    
    /**
     * Applies a shake animation to a node to indicate error.
     */
    private void applyShakeAnimation(Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
    
    /**
     * Applies exit animation before transitioning to main view.
     */
    private void applyExitAnimation() {
        // Fade out everything except the logo
        Node formContainer = loginForm.getParent();
        
        FadeTransition fadeOutForm = new FadeTransition(Duration.millis(200), formContainer);  // Faster fade out
        fadeOutForm.setToValue(0.0);
        
        // Scale up and fade out logo last with more dramatic effect
        ScaleTransition scaleLogo = new ScaleTransition(Duration.millis(300), logoImage);  // Faster scale
        scaleLogo.setToX(1.8);  // Scale up more for dramatic effect
        scaleLogo.setToY(1.8);
        scaleLogo.setDelay(Duration.millis(150));  // Shorter delay
        
        RotateTransition rotateLogo = new RotateTransition(Duration.millis(300), logoImage);
        rotateLogo.setToAngle(15);  // Rotate slightly for effect
        rotateLogo.setDelay(Duration.millis(150));
        
        FadeTransition fadeLogo = new FadeTransition(Duration.millis(250), logoImage);  // Faster fade
        fadeLogo.setToValue(0.0);
        fadeLogo.setDelay(Duration.millis(200));  // Shorter delay
        
        // Sequence the animations
        ParallelTransition logoAnimation = new ParallelTransition(scaleLogo, fadeLogo, rotateLogo);
        
        SequentialTransition exitSequence = new SequentialTransition(fadeOutForm, logoAnimation);
        exitSequence.setOnFinished(e -> {
            try {
                FileVaultApp.showMainView();
            } catch (IOException ex) {
                showMessage("Fehler beim Laden der Hauptansicht: " + ex.getMessage(), true);
                LoggingUtil.logError("AuthController", "Error loading main view: " + ex.getMessage());
            }
        });
        
        exitSequence.play();
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
            
            // Apply shake animation to empty fields
            if (newPassword.isEmpty()) applyShakeAnimation(newPasswordField);
            if (confirmPassword.isEmpty()) applyShakeAnimation(confirmPasswordField);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            LoggingUtil.logError("AuthController", "Registration failed: Passwords do not match.");
            showMessage("Die Passwörter stimmen nicht überein", true);
            applyShakeAnimation(confirmPasswordField);
            return;
        }

        if (newPassword.length() < 8) {
            LoggingUtil.logError("AuthController", "Registration failed: Password too short.");
            showMessage("Das Passwort muss mindestens 8 Zeichen lang sein", true);
            applyShakeAnimation(newPasswordField);
            return;
        }

        // Disable register button and show loading state
        registerButton.setDisable(true);
        registerButton.setText("Erstelle Konto...");
        
        // Add slight delay to show loading state
        PauseTransition loadingDelay = new PauseTransition(Duration.millis(500));
        loadingDelay.setOnFinished(event -> {
            try {
                UserManager.getInstance().createUser(newPassword);
                FolderManager.getInstance().createBaseStructure();
                showMessage("Konto erfolgreich erstellt!", false);
                LoggingUtil.logInfo("AuthController", "User registered successfully.");

                toggleFormButton.setVisible(false); // Hide the "Konto erstellen" button after user creation

                // Apply success animation and toggle to login form
                ScaleTransition successPulse = new ScaleTransition(Duration.millis(300), registerForm);
                successPulse.setToX(1.05);
                successPulse.setToY(1.05);
                successPulse.setCycleCount(2);
                successPulse.setAutoReverse(true);
                
                successPulse.setOnFinished(e -> toggleForm());
                successPulse.play();
                
            } catch (Exception e) {
                LoggingUtil.logError("AuthController", "Error during registration: " + e.getMessage());
                showMessage("Fehler beim Erstellen des Kontos: " + e.getMessage(), true);
                registerButton.setDisable(false);
                registerButton.setText("Konto erstellen");
            }
        });
        loadingDelay.play();
    }
    
    /**
     * Wechselt zwischen Login- und Registrierungsformular.
     * Aktualisiert die Sichtbarkeit der Formulare und den Text des Toggle-Buttons.
     */
    @FXML
    public void toggleForm() {
        LoggingUtil.logInfo("AuthController", "Toggling form. Current view: " + (isLoginView ? "Login" : "Register"));
        isLoginView = !isLoginView;

        // Create animation for transition
        FadeTransition fadeOutCurrent = new FadeTransition(Duration.millis(300), 
            isLoginView ? registerForm : loginForm);
        fadeOutCurrent.setToValue(0);
        
        FadeTransition fadeInNew = new FadeTransition(Duration.millis(300), 
            isLoginView ? loginForm : registerForm);
        fadeInNew.setFromValue(0);
        fadeInNew.setToValue(1);
        
        TranslateTransition slideOutCurrent = new TranslateTransition(Duration.millis(300),
            isLoginView ? registerForm : loginForm);
        slideOutCurrent.setByY(30);
        
        TranslateTransition slideInNew = new TranslateTransition(Duration.millis(300),
            isLoginView ? loginForm : registerForm);
        slideInNew.setFromY(-30);
        slideInNew.setToY(0);
        
        // Build the animation sequence
        ParallelTransition parallelOut = new ParallelTransition(fadeOutCurrent, slideOutCurrent);
        parallelOut.setOnFinished(e -> {
            // Update visibility
            if (isLoginView) {
                loginForm.setVisible(true);
                registerForm.setVisible(false);
                toggleFormButton.setText("Konto erstellen");
            } else {
                loginForm.setVisible(false);
                registerForm.setVisible(true);
                toggleFormButton.setText("Zurück zur Anmeldung");
            }
            
            // Reset password fields
            passwordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            
            // Reset any error messages
            messageLabel.setVisible(false);
            
            // Continue with the animation for the newly visible form
            ParallelTransition parallelIn = new ParallelTransition(fadeInNew, slideInNew);
            parallelIn.play();
        });
        
        parallelOut.play();
        LoggingUtil.logInfo("AuthController", "Form toggle animation initiated. New view: " + (isLoginView ? "Login" : "Register"));
    }
    
    /**
     * Zeigt eine Nachricht im Message-Label an.
     * 
     * @param message Die anzuzeigende Nachricht
     * @param isError Gibt an, ob es sich um eine Fehlermeldung handelt
     */
    protected void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        
        if (isError) {
            messageLabel.getStyleClass().remove("success-text");
            messageLabel.getStyleClass().add("error-text");
        } else {
            messageLabel.getStyleClass().remove("error-text");
            messageLabel.getStyleClass().add("success-text");
        }
        
        // Animate the message appearance
        messageLabel.setOpacity(0);
        messageLabel.setVisible(true);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), messageLabel);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    
    /**
     * Öffnet das GitHub-Repository, wenn das Logo angeklickt wird.
     */
    @FXML
    public void handleLogoClick() {
        LoggingUtil.logInfo("AuthController", "Logo clicked. Opening GitHub repository.");
        try {
            // Check which operating system is running
            String os = System.getProperty("os.name").toLowerCase();
            String repoUrl = "https://github.com/GulfGulfinson/fileVault";
            
            ProcessBuilder processBuilder;
            if (os.contains("win")) {
                // Windows
                processBuilder = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", repoUrl);
            } else if (os.contains("mac")) {
                // MacOS
                processBuilder = new ProcessBuilder("open", repoUrl);
            } else if (os.contains("nix") || os.contains("nux") || os.contains("linux")) {
                // Linux
                processBuilder = new ProcessBuilder("xdg-open", repoUrl);
            } else {
                // Fallback
                throw new UnsupportedOperationException("Ihr Betriebssystem wird nicht unterstützt.");
            }
            
            // Start the process
            processBuilder.start();
            
            // Apply a brief animation to show feedback
            RotateTransition rotate = new RotateTransition(Duration.millis(200), logoImage);
            rotate.setByAngle(360);
            rotate.setCycleCount(1);
            
            ScaleTransition pulse = new ScaleTransition(Duration.millis(200), logoImage);
            pulse.setToX(1.2);
            pulse.setToY(1.2);
            pulse.setCycleCount(2);
            pulse.setAutoReverse(true);
            
            ParallelTransition animation = new ParallelTransition(rotate, pulse);
            animation.play();
            
        } catch (Exception e) {
            showMessage("Fehler beim Öffnen des Browsers: " + e.getMessage(), true);
            LoggingUtil.logError("AuthController", "Error opening browser: " + e.getMessage());
        }
    }
}