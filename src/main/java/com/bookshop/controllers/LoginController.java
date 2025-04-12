package com.bookshop.controllers;

import com.bookshop.models.User;
import com.bookshop.services.UserService;
import com.bookshop.utils.SessionManager;
import com.bookshop.utils.ViewNavigator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

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
            // Authenticate user using UserService
            User user = userService.authenticateUser(username, password);
            
            if (user != null) {
                // Set current user in session
                SessionManager.getInstance().setCurrentUser(user);
                
                // Navigate to the appropriate dashboard based on user role
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    loadAdminDashboard();
                } else {
                    loadCustomerDashboard();
                }
            } else {
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
            System.out.println("Attempting to load register view...");
            
            // Try to load from ViewNavigator first
            try {
                System.out.println("Trying to use ViewNavigator...");
                ViewNavigator.getInstance().navigateTo("register.fxml");
                return;
            } catch (Exception e) {
                System.out.println("ViewNavigator failed: " + e.getMessage());
                // Fall back to manual loading
            }
            
            // Try views directory
            System.out.println("Trying to load from /views/register.fxml");
            java.net.URL viewsUrl = getClass().getResource("/views/register.fxml");
            System.out.println("Views URL: " + viewsUrl);
            
            // Try fxml directory
            System.out.println("Trying to load from /fxml/register.fxml");
            java.net.URL fxmlUrl = getClass().getResource("/fxml/register.fxml");
            System.out.println("FXML URL: " + fxmlUrl);
            
            // Choose the URL based on what's available
            java.net.URL url = viewsUrl != null ? viewsUrl : fxmlUrl;
            
            if (url == null) {
                throw new IOException("Could not find register.fxml in any location");
            }
            
            System.out.println("Loading from URL: " + url);
            
            // Load from the URL
            Parent root = FXMLLoader.load(url);
            
            Scene scene = new Scene(root);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            messageLabel.setText("Error loading registration page: " + e.getMessage());
            System.err.println("Error loading register view: " + e.getMessage());
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
            // If for some reason the resource can't be found in /views/, try the /fxml/ directory
            if (root == null) {
                root = FXMLLoader.load(getClass().getResource("/fxml/admin_dashboard.fxml"));
            }
            Scene scene = new Scene(root);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            messageLabel.setText("Error loading admin dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads the customer dashboard.
     */
    private void loadCustomerDashboard() {
        try {
            // Load the customer dashboard view
            Parent root = FXMLLoader.load(getClass().getResource("/views/customer_dashboard.fxml"));
            // If for some reason the resource can't be found in /views/, try the /fxml/ directory
            if (root == null) {
                root = FXMLLoader.load(getClass().getResource("/fxml/customer_dashboard.fxml"));
            }
            Scene scene = new Scene(root);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            messageLabel.setText("Error loading customer dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}