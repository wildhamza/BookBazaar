package com.bookshop.controllers;

import com.bookshop.models.User;
import com.bookshop.services.AuthService;
import com.bookshop.utils.SessionManager;
import com.bookshop.utils.ViewNavigator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

/**
 * Controller for the login view.
 */
public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Hyperlink registerLink;
    
    @FXML
    private Label errorMessage;
    
    private AuthService authService;
    
    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        authService = new AuthService();
        errorMessage.setText("");
    }
    
    /**
     * Handles the login button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            errorMessage.setText("Please enter both username and password");
            return;
        }
        
        try {
            User user = authService.authenticateUser(username, password);
            
            if (user != null) {
                // Authentication successful
                SessionManager.getInstance().setCurrentUser(user);
                
                // Navigate to appropriate dashboard based on user role
                if (user.isAdmin()) {
                    ViewNavigator.getInstance().navigateTo("admin_dashboard.fxml");
                } else {
                    ViewNavigator.getInstance().navigateTo("customer_dashboard.fxml");
                }
            } else {
                // Authentication failed
                errorMessage.setText("Invalid username or password");
                passwordField.clear();
            }
        } catch (SQLException e) {
            errorMessage.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the register link action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleRegisterLink(ActionEvent event) {
        ViewNavigator.getInstance().navigateTo("register.fxml");
    }
}