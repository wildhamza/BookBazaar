package com.bookshop.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Utility class for managing scene navigation.
 * Implements the Singleton design pattern.
 */
public class SceneManager {
    
    private static SceneManager instance;
    private Stage stage;
    private Map<String, Parent> screenMap = new HashMap<>();
    private Stack<String> navigationHistory = new Stack<>();
    private StackPane mainContainer;
    
    /**
     * Private constructor for Singleton pattern.
     */
    private SceneManager() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Gets the singleton instance of SceneManager.
     * 
     * @return The singleton instance
     */
    public static synchronized SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
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
     * Sets the main container for swapping screens.
     * 
     * @param container The main container
     */
    public void setMainContainer(StackPane container) {
        this.mainContainer = container;
    }
    
    /**
     * Adds a screen to the screen map.
     * 
     * @param name The screen name
     * @param root The screen root node
     */
    public void addScreen(String name, Parent root) {
        screenMap.put(name, root);
    }
    
    /**
     * Activates a screen by swapping it into the main container.
     * 
     * @param name The screen name
     */
    public void activate(String name) {
        if (screenMap.containsKey(name) && mainContainer != null) {
            if (!mainContainer.getChildren().isEmpty()) {
                navigationHistory.push(
                    mainContainer.getChildren().get(0).getId()
                );
            }
            
            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(screenMap.get(name));
        } else {
            System.err.println("Screen " + name + " not found or mainContainer not set");
        }
    }
    
    /**
     * Navigates back to the previous screen.
     */
    public void goBack() {
        if (!navigationHistory.isEmpty() && mainContainer != null) {
            String previousScreen = navigationHistory.pop();
            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(screenMap.get(previousScreen));
        } else {
            System.err.println("Navigation history is empty or mainContainer not set");
        }
    }
    
    /**
     * Loads a scene from an FXML file and sets it on the primary stage.
     * 
     * @param fxmlPath The path to the FXML file
     */
    public void loadScene(String fxmlPath) {
        try {
            if (stage == null) {
                throw new IOException("Stage is not set. Make sure to call setStage() before loading scenes.");
            }
            
            // Log loading attempt
            System.out.println("Loading scene: " + fxmlPath);
            
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
            
            // Create a new scene
            Scene scene = new Scene(root);
            
            // Set the scene to the stage
            stage.setScene(scene);
            
            // Show the stage
            stage.show();
        } catch (Exception e) {
            System.err.println("Error loading scene: " + e.getMessage());
            e.printStackTrace();
            
            // Create an alert dialog to show the error
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Error loading scene");
            alert.setContentText("Could not load the scene: " + fxmlPath + "\nError: " + e.getMessage());
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