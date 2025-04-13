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

public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label messageLabel;
    
    private UserService userService;
    
    @FXML
    private void initialize() {
        userService = new UserService();
    }
    
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password");
            return;
        }
        
        try {
            User user = userService.authenticateUser(username, password);
            
            if (user != null) {
                SessionManager.getInstance().setCurrentUser(user);
                
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
    
    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            System.out.println("Attempting to load register view...");
            
            try {
                System.out.println("Trying to use ViewNavigator...");
                ViewNavigator.getInstance().navigateTo("register.fxml");
                return;
            } catch (Exception e) {
                System.out.println("ViewNavigator failed: " + e.getMessage());
            }
            
            System.out.println("Trying to load from /views/register.fxml");
            java.net.URL viewsUrl = getClass().getResource("/views/register.fxml");
            System.out.println("Views URL: " + viewsUrl);
            
            System.out.println("Trying to load from /fxml/register.fxml");
            java.net.URL fxmlUrl = getClass().getResource("/fxml/register.fxml");
            System.out.println("FXML URL: " + fxmlUrl);
            
            java.net.URL url = viewsUrl != null ? viewsUrl : fxmlUrl;
            
            if (url == null) {
                throw new IOException("Could not find register.fxml in any location");
            }
            
            System.out.println("Loading from URL: " + url);
            
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
    
    private void loadAdminDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/admin_dashboard.fxml"));
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
    
    private void loadCustomerDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/customer_dashboard.fxml"));
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