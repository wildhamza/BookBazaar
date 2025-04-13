package com.bookshop.controllers;

import com.bookshop.models.User;
import com.bookshop.services.AuthService;
import com.bookshop.utils.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextFormatter;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class RegisterController implements Initializable {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;
    @FXML private Label errorMessageLabel;
    
    private AuthService authService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = new AuthService();
        errorMessageLabel.setVisible(false);
        
        setupValidation();
    }
    
    private void setupValidation() {
        Pattern usernamePattern = Pattern.compile("[a-zA-Z0-9]*");
        TextFormatter<String> usernameFormatter = new TextFormatter<>(
            (UnaryOperator<TextFormatter.Change>) change -> {
                return usernamePattern.matcher(change.getControlNewText()).matches() ? change : null;
            }
        );
        usernameField.setTextFormatter(usernameFormatter);
        
        Pattern phonePattern = Pattern.compile("[0-9+\\-() ]*");
        TextFormatter<String> phoneFormatter = new TextFormatter<>(
            (UnaryOperator<TextFormatter.Change>) change -> {
                return phonePattern.matcher(change.getControlNewText()).matches() ? change : null;
            }
        );
        phoneField.setTextFormatter(phoneFormatter);
    }
    
    @FXML
    private void handleRegisterButton(ActionEvent event) {
        errorMessageLabel.setVisible(false);
        
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        
        if (!validateForm(username, password, confirmPassword, fullName, email, address, phone)) {
            return;
        }
        
        try {
            @SuppressWarnings("unused")
            String firstName = "";
            @SuppressWarnings("unused")
            String lastName = "";
            if (fullName != null && !fullName.isEmpty()) {
                String[] nameParts = fullName.trim().split("\\s+", 2);
                firstName = nameParts[0];
                lastName = nameParts.length > 1 ? nameParts[1] : "";
            }
            
            @SuppressWarnings("unused")
            User newUser = authService.register(
                username, password, fullName, email, address, phone
            );
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText(null);
            alert.setContentText("Your account has been created successfully. You can now log in.");
            alert.showAndWait();
            
            ViewNavigator.getInstance().navigateTo("login.fxml");
            
        } catch (Exception e) {
            showError("Registration error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLoginLink(ActionEvent event) {
        ViewNavigator.getInstance().navigateTo("login.fxml");
    }
    
    private boolean validateForm(String username, String password, String confirmPassword, 
                                String fullName, String email, String address, String phone) {
        
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || 
            fullName.isEmpty() || email.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            showError("All fields are required");
            return false;
        }
        
        if (username.length() < 4) {
            showError("Username must be at least 4 characters");
            return false;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return false;
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!email.matches(emailRegex)) {
            showError("Invalid email format");
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
    }
}
