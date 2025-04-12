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
            // Build the full path to the FXML file
            String fullPath = "/views/" + fxmlPath;
            
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fullPath));
            Parent root = loader.load();
            
            // Create a new scene
            Scene scene = new Scene(root);
            
            // Set the scene to the stage
            stage.setScene(scene);
            
            // Show the stage
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading view: " + e.getMessage());
            e.printStackTrace();
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