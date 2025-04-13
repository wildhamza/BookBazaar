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
             
            String viewsPath = "/views/" + fxmlPath;
            System.out.println("Attempting to load from: " + viewsPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewsPath));
             
            if (loader.getLocation() == null) {
                String fxmlDirPath = "/fxml/" + fxmlPath;
                System.out.println("Views location was null, trying: " + fxmlDirPath);
                loader = new FXMLLoader(getClass().getResource(fxmlDirPath));
            }
             
            if (loader.getLocation() == null) {
                System.out.println("FXML directory was null, trying direct path: " + fxmlPath);
                loader = new FXMLLoader(getClass().getResource(fxmlPath));
            }
             
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found in any location: " + fxmlPath);
            }
            
            System.out.println("Loading FXML from: " + loader.getLocation());
            
            Parent root = loader.load();
            
            Object controller = loader.getController();
            if (controller != null) {
                if (controller instanceof com.bookshop.controllers.CustomerOrdersController) {
                    System.out.println("Found CustomerOrdersController, calling refreshView");
                    ((com.bookshop.controllers.CustomerOrdersController) controller).refreshView();
                }
            }
            
            Scene scene = new Scene(root);
            
            stage.setScene(scene);
            
            stage.show();
        } catch (Exception e) {
            System.err.println("Error loading view: " + e.getMessage());
            e.printStackTrace();
            
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Error loading view");
            alert.setContentText("Could not load the view: " + fxmlPath + "\nError: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    public void closeApplication() {
        if (stage != null) {
            stage.close();
        }
    }
}