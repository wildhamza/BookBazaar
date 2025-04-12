package com.bookshop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class.
 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the login view
        Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
        // If for some reason the resource can't be found in /views/, try the /fxml/ directory
        if (root == null) {
            root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        }
        
        // Create scene
        Scene scene = new Scene(root, 800, 600);
        
        // Configure stage
        primaryStage.setTitle("BookShop Application");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }
    
    /**
     * Main method.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}