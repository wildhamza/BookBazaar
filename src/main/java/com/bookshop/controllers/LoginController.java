package com.bookshop.controllers;

import com.bookshop.models.User;
import com.bookshop.services.AuthService;
import com.bookshop.utils.SessionManager;
import com.bookshop.utils.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Hyperlink;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the login view.
 */
public class LoginController implements Initializable {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;
    @FXML private Label errorMessageLabel;
    
    private AuthService authService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = new AuthService();
        errorMessageLabel.setVisible(false);
        
        // Clear any existing session
        SessionManager.getInstance().clearSession();
    }
    
    @FXML
    private void handleLoginButton(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Basic form validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required");
            return;
        }
        
        try {
            // Attempt to authenticate user
            User user = authService.authenticateUser(username, password);
            
            if (user != null) {
                // Store user in session
                SessionManager.getInstance().setCurrentUser(user);
                
                // Navigate to appropriate dashboard
                if (user.isAdmin()) {
                    ViewNavigator.getInstance().navigateTo("admin_dashboard.fxml");
                } else {
                    ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
                }
            } else {
                showError("Invalid username or password");
            }
        } catch (Exception e) {
            showError("Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleRegisterLink(ActionEvent event) {
        // Show registration form
        try {
            ViewNavigator.getInstance().navigateTo("register.fxml");
        } catch (Exception e) {
            showError("Error opening registration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showError(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
    }
}
