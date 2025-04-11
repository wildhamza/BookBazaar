package com.bookshop;

import com.bookshop.db.DatabaseConnection;
import com.bookshop.utils.ViewNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main entry point for the JavaFX Online Bookshop Application.
 * Initializes the database connection and starts the application.
 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database connection
            DatabaseConnection.getInstance();
            
            // Setup the primary stage
            primaryStage.setTitle("Online Bookshop");
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            
            // Initialize view navigator with primary stage and navigate to login view
            ViewNavigator.getInstance().setStage(primaryStage);
            ViewNavigator.getInstance().navigateTo("login.fxml");
            
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void stop() {
        // Clean up resources when application closes
        try {
            DatabaseConnection.getInstance().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
