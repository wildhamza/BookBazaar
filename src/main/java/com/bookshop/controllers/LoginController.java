package com.bookshop.controllers;

import com.bookshop.models.User;
import com.bookshop.services.UserService;
import com.bookshop.utils.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the login view.
 */
public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label messageLabel;
    
    private UserService userService;
    
    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        userService = new UserService();
    }
    
    /**
     * Handles the login action.
     * 
     * @param event The action event
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password");
            return;
        }
        
        try {
            // For now, just support admin login with hardcoded credentials
            if (username.equals("admin") && password.equals("admin123")) {
                // Create admin user
                User adminUser = new User();
                adminUser.setId(1);
                adminUser.setUsername("admin");
                adminUser.setRole("admin");
                
                // Set current user in session
                SessionManager.getInstance().setCurrentUser(adminUser);
                
                // Navigate to admin dashboard
                loadAdminDashboard();
            } else {
                // In a real application, we would check the database
                // For now, show error message
                messageLabel.setText("Invalid username or password");
            }
        } catch (Exception e) {
            messageLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the register action.
     * 
     * @param event The action event
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            // Load the registration view
            Parent root = FXMLLoader.load(getClass().getResource("/views/register.fxml"));
            Scene scene = new Scene(root);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            messageLabel.setText("Error loading registration page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads the admin dashboard.
     */
    private void loadAdminDashboard() {
        try {
            // Load the admin dashboard view
            Parent root = FXMLLoader.load(getClass().getResource("/views/admin_dashboard.fxml"));
            Scene scene = new Scene(root);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            messageLabel.setText("Error loading admin dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}