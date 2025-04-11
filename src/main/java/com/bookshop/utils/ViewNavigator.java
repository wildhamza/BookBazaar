package com.bookshop.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Singleton pattern implementation for navigating between views.
 * Manages scene switching in the application.
 */
public class ViewNavigator {
    private static ViewNavigator instance;
    private Stage stage;
    
    private static final String FXML_PATH = "/fxml/";
    
    // Private constructor
    private ViewNavigator() {
    }
    
    /**
     * Gets the single instance of ViewNavigator.
     * 
     * @return The ViewNavigator instance
     */
    public static synchronized ViewNavigator getInstance() {
        if (instance == null) {
            instance = new ViewNavigator();
        }
        return instance;
    }
    
    /**
     * Sets the primary stage for the application.
     * 
     * @param stage The primary Stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    /**
     * Navigates to the specified FXML view.
     * 
     * @param fxmlFileName The name of the FXML file to load
     */
    public void navigateTo(String fxmlFileName) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(FXML_PATH + fxmlFileName));
            Scene scene = new Scene(root);
            
            // Apply global stylesheet if needed
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            
            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlFileName);
            e.printStackTrace();
        }
    }
    
    /**
     * Navigates to the specified FXML view with a controller factory.
     * Useful when you need to pass parameters to the controller.
     * 
     * @param fxmlFileName The name of the FXML file to load
     * @param controllerFactory The controller factory
     */
    public void navigateTo(String fxmlFileName, javafx.util.Callback<Class<?>, Object> controllerFactory) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH + fxmlFileName));
            loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            // Apply global stylesheet if needed
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            
            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlFileName);
            e.printStackTrace();
        }
    }
    
    /**
     * Gets the current stage.
     * 
     * @return The current Stage
     */
    public Stage getStage() {
        return stage;
    }
}
