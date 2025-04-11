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
    
    @FXML
    public void initialize() {
        // Check if user exists
        boolean userExists = UserManager.getInstance().userExists();
        
        // If no user exists, show the registration form
        if (!userExists) {
            toggleForm();
        }
    }
    
    @FXML
    public void handleLogin() {
        String password = passwordField.getText();
        
        if (password.isEmpty()) {
            showMessage("Please enter your master password", true);
            return;
        }
        
        boolean authenticated = UserManager.getInstance().authenticate(password);
        
        if (authenticated) {
            try {
                // Initialize folders and files after successful login
                FolderManager.getInstance().initialize();
                
                // Show success message briefly before transition
                showMessage("Login successful!", false);
                
                // Delay the transition to main screen for a short period
                PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                pause.setOnFinished(event -> {
                    try {
                        FileVaultApp.showMainView();
                    } catch (IOException e) {
                        showMessage("Error loading main view: " + e.getMessage(), true);
                    }
                });
                pause.play();
                
            } catch (Exception e) {
                showMessage("Error initializing application: " + e.getMessage(), true);
            }
        } else {
            showMessage("Invalid master password", true);
        }
    }
    
    @FXML
    public void handleRegister() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Please enter and confirm your master password", true);
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showMessage("Passwords do not match", true);
            return;
        }
        
        if (newPassword.length() < 8) {
            showMessage("Password must be at least 8 characters long", true);
            return;
        }
        
        try {
            // Create a new user
            UserManager.getInstance().createUser(newPassword);
            
            // Initialize app structure
            FolderManager.getInstance().createBaseStructure();
            
            // Show success message
            showMessage("Account created successfully!", false);
            
            // Switch back to login form after a short delay
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(event -> toggleForm());
            pause.play();
            
        } catch (Exception e) {
            showMessage("Error creating account: " + e.getMessage(), true);
        }
    }
    
    @FXML
    public void toggleForm() {
        isLoginView = !isLoginView;
        
        if (isLoginView) {
            loginForm.setVisible(true);
            registerForm.setVisible(false);
            toggleFormButton.setText("Create Account");
        } else {
            loginForm.setVisible(false);
            registerForm.setVisible(true);
            toggleFormButton.setText("Back to Login");
        }
        
        // Clear any error messages
        messageLabel.setVisible(false);
        
        // Clear password fields
        passwordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
    
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