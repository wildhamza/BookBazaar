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
 
public class SceneManager {
    
    private static SceneManager instance;
    private Stage stage;
    private Map<String, Parent> screenMap = new HashMap<>();
    private Stack<String> navigationHistory = new Stack<>();
    private StackPane mainContainer;
 
    private SceneManager() { 
    }
     
    public static synchronized SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }
     
    public void setStage(Stage stage) {
        this.stage = stage;
    }
     
    public Stage getStage() {
        return stage;
    }
     
    public void setMainContainer(StackPane container) {
        this.mainContainer = container;
    }
     
    public void addScreen(String name, Parent root) {
        screenMap.put(name, root);
    }
     
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
     
    public void goBack() {
        if (!navigationHistory.isEmpty() && mainContainer != null) {
            String previousScreen = navigationHistory.pop();
            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(screenMap.get(previousScreen));
        } else {
            System.err.println("Navigation history is empty or mainContainer not set");
        }
    }
     
    public void loadScene(String fxmlPath) {
        try {
            if (stage == null) {
                throw new IOException("Stage is not set. Make sure to call setStage() before loading scenes.");
            }
             
            System.out.println("Loading scene: " + fxmlPath);
             
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
             
            Scene scene = new Scene(root);
             
            stage.setScene(scene);
             
            stage.show();
        } catch (Exception e) {
            System.err.println("Error loading scene: " + e.getMessage());
            e.printStackTrace();
             
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Error loading scene");
            alert.setContentText("Could not load the scene: " + fxmlPath + "\nError: " + e.getMessage());
            alert.showAndWait();
        }
    }
     
    public void closeApplication() {
        if (stage != null) {
            stage.close();
        }
    }
} 