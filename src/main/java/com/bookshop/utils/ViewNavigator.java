package com.bookshop.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewNavigator {
    
    private static ViewNavigator instance;
    private Stage stage;
 
    private ViewNavigator() { 
    }
 
    public static synchronized ViewNavigator getInstance() {
        if (instance == null) {
            instance = new ViewNavigator();
        }
        return instance;
    }
     
    public void setStage(Stage stage) {
        this.stage = stage;
    }
     
    public Stage getStage() {
        return stage;
    }
     
    public void navigateTo(String fxmlPath) {
        try {
            if (stage == null) {
                throw new IOException("Stage is not set. Make sure to call setStage() before navigating.");
            }
             
            System.out.println("Navigating to: " + fxmlPath);
            
            // Try multiple locations to find the FXML file
            Parent root = null;
            Exception lastException = null;
            
            // Array of possible paths to try
            String[] pathsToTry = {
                "/fxml/" + fxmlPath,
                "/views/" + fxmlPath,
                "/" + fxmlPath,
                fxmlPath
            };
            
            for (String path : pathsToTry) {
                try {
                    System.out.println("Attempting to load from: " + path);
                    
                    // Try different class loaders to ensure we find the resource
                    FXMLLoader loader = null;
                    java.net.URL resourceUrl = getClass().getResource(path);
                    
                    if (resourceUrl == null) {
                        // Try the class loader
                        resourceUrl = getClass().getClassLoader().getResource(path.startsWith("/") ? path.substring(1) : path);
                    }
                    
                    if (resourceUrl == null) {
                        // Try the thread context class loader
                        resourceUrl = Thread.currentThread().getContextClassLoader().getResource(path.startsWith("/") ? path.substring(1) : path);
                    }
                    
                    if (resourceUrl != null) {
                        System.out.println("Located FXML at: " + resourceUrl);
                        loader = new FXMLLoader(resourceUrl);
                    } else {
                        System.out.println("Could not locate FXML at: " + path);
                        continue;
                    }
                    
                    if (loader.getLocation() != null) {
                        System.out.println("Located FXML at: " + loader.getLocation());
                        root = loader.load();
                        
                        // Initialize controller
                        Object controller = loader.getController();
                        if (controller != null) {
                            if (controller instanceof com.bookshop.controllers.CustomerOrdersController) {
                                System.out.println("Found CustomerOrdersController, calling refreshView");
                                ((com.bookshop.controllers.CustomerOrdersController) controller).refreshView();
                            }
                            // Force layout pass to ensure proper initialization
                            root.applyCss();
                            root.layout();
                        }
                        
                        break; // Successfully loaded, stop trying
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load from " + path + ": " + e.getMessage());
                    if (lastException == null) {
                        lastException = e;
                    }
                }
            }
            
            // If we couldn't load the FXML from any path, throw the last exception
            if (root == null) {
                if (lastException != null) {
                    throw lastException;
                } else {
                    throw new IOException("FXML file not found: " + fxmlPath);
                }
            }
            
            Scene scene = new Scene(root, 800, 600);
            // Apply any global stylesheets if needed
            try {
                scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            } catch (Exception ex) {
                System.err.println("Could not load stylesheet: " + ex.getMessage());
            }
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading view: " + e.getMessage());
            e.printStackTrace();
            
            // Try a more direct approach as a fallback
            try {
                System.out.println("Attempting direct fallback loading...");
                String directPath = "/fxml/" + fxmlPath;
                
                // Try different class loaders
                java.net.URL resourceUrl = getClass().getResource(directPath);
                
                if (resourceUrl == null) {
                    // Try the class loader
                    resourceUrl = getClass().getClassLoader().getResource(directPath.startsWith("/") ? directPath.substring(1) : directPath);
                }
                
                if (resourceUrl == null) {
                    // Try the thread context class loader
                    resourceUrl = Thread.currentThread().getContextClassLoader().getResource(directPath.startsWith("/") ? directPath.substring(1) : directPath);
                }
                
                if (resourceUrl == null) {
                    throw new IOException("Could not find resource: " + directPath);
                }
                
                System.out.println("Found resource at: " + resourceUrl);
                Parent root = FXMLLoader.load(resourceUrl);
                Scene scene = new Scene(root, 800, 600);
                try {
                    scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
                } catch (Exception ex) {
                    System.err.println("Could not load stylesheet: " + ex.getMessage());
                }
                stage.setScene(scene);
                stage.show();
                return;
            } catch (Exception fallbackEx) {
                System.err.println("Fallback loading also failed: " + fallbackEx.getMessage());
                fallbackEx.printStackTrace();
            }
            
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Error loading view");
            alert.setContentText("Could not load the view: " + fxmlPath + "\nError: " + e.getMessage() + 
                                "\n\nPlease check that the FXML file exists and that the application has access to it.");
            alert.showAndWait();
        }
    }
    
    public void closeApplication() {
        if (stage != null) {
            stage.close();
        }
    }
}