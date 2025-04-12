package com.bookshop.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utility class for managing view navigation.
 * Implements the Singleton design pattern.
 */
public class ViewNavigator {
    
    private static ViewNavigator instance;
    private Stage stage;
    
    /**
     * Private constructor for Singleton pattern.
     */
    private ViewNavigator() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Gets the singleton instance of ViewNavigator.
     * 
     * @return The singleton instance
     */
    public static synchronized ViewNavigator getInstance() {
        if (instance == null) {
            instance = new ViewNavigator();
        }
        return instance;
    }
    
    /**
     * Sets the primary stage.
     * 
     * @param stage The primary stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    /**
     * Gets the primary stage.
     * 
     * @return The primary stage
     */
    public Stage getStage() {
        return stage;
    }
    
    /**
     * Navigates to the specified view.
     * 
     * @param fxmlPath The path to the FXML file
     */
    public void navigateTo(String fxmlPath) {
        try {
            if (stage == null) {
                throw new IOException("Stage is not set. Make sure to call setStage() before navigating.");
            }
            
            // Log navigation attempt
            System.out.println("Navigating to: " + fxmlPath);
            
            // Try loading from /views/ directory first
            String viewsPath = "/views/" + fxmlPath;
            System.out.println("Attempting to load from: " + viewsPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewsPath));
            
            // If not found in /views/, try /fxml/ directory
            if (loader.getLocation() == null) {
                String fxmlDirPath = "/fxml/" + fxmlPath;
                System.out.println("Views location was null, trying: " + fxmlDirPath);
                loader = new FXMLLoader(getClass().getResource(fxmlDirPath));
            }
            
            // If still not found, try using the path directly (might be absolute)
            if (loader.getLocation() == null) {
                System.out.println("FXML directory was null, trying direct path: " + fxmlPath);
                loader = new FXMLLoader(getClass().getResource(fxmlPath));
            }
            
            // If none of the above locations worked, throw an exception
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found in any location: " + fxmlPath);
            }
            
            System.out.println("Loading FXML from: " + loader.getLocation());
            
            // Load the FXML file
            Parent root = loader.load();
            
            // For specific controllers, call refreshView when available
            Object controller = loader.getController();
            if (controller != null) {
                if (controller instanceof com.bookshop.controllers.CustomerOrdersController) {
                    System.out.println("Found CustomerOrdersController, calling refreshView");
                    ((com.bookshop.controllers.CustomerOrdersController) controller).refreshView();
                }
            }
            
            // Create a new scene
            Scene scene = new Scene(root);
            
            // Set the scene to the stage
            stage.setScene(scene);
            
            // Show the stage
            stage.show();
        } catch (Exception e) {
            System.err.println("Error loading view: " + e.getMessage());
            e.printStackTrace();
            
            // Create an alert dialog to show the error
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Error loading view");
            alert.setContentText("Could not load the view: " + fxmlPath + "\nError: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Closes the application.
     */
    public void closeApplication() {
        if (stage != null) {
            stage.close();
        }
    }
}