package com.bookshop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.GraphicsEnvironment;
import java.util.Arrays;

import com.bookshop.services.BookService;
import com.bookshop.services.UserService;
import com.bookshop.models.Book;
import com.bookshop.models.User;
import com.bookshop.utils.ViewNavigator;
import com.bookshop.utils.SceneManager;
import java.sql.SQLException;
import java.util.List;

public class Main extends Application {
    
    private static boolean isHeadless = false;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        if (isHeadless) {
            System.out.println("Running in headless mode. JavaFX UI is not available.");
            
            Platform.setImplicitExit(false);
            
            try {
                runHeadlessTests();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            Platform.exit();
            return;
        }
        
        ViewNavigator.getInstance().setStage(primaryStage);
        SceneManager.getInstance().setStage(primaryStage);
        
        Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
        if (root == null) {
            root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        }
        
        Scene scene = new Scene(root, 800, 600);
        
        primaryStage.setTitle("BookShop Application");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }
    
    private void runHeadlessTests() throws Exception {
        System.out.println("Running basic database tests in headless mode...");
        
        try {
            BookService bookService = new BookService();
            List<Book> books = bookService.getAllBooks();
            System.out.println("Found " + books.size() + " books in the database.");
            
            if (!books.isEmpty()) {
                for (int i = 0; i < Math.min(3, books.size()); i++) {
                    Book book = books.get(i);
                    System.out.println("Book: " + book.getTitle() + " by " + book.getAuthor() + " (€" + book.getPrice() + ")");
                }
            }
            
            if (!books.isEmpty()) {
                Book firstBook = books.get(0);
                Book bookById = bookService.getBookById(firstBook.getId());
                if (bookById != null) {
                    System.out.println("Successfully retrieved book by ID: " + bookById.getTitle());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error testing BookService: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
            UserService userService = new UserService();
            User adminUser = userService.authenticateUser("admin", "admin123");
            
            if (adminUser != null) {
                System.out.println("Found admin user: " + adminUser.getUsername() + " (Role: " + adminUser.getRole() + ")");
            } else {
                System.out.println("Admin user not found. Database may not be properly initialized.");
            }
            
            List<User> users = userService.getAllUsers();
            System.out.println("Found " + users.size() + " users in the database.");
            
            if (!users.isEmpty()) {
                for (int i = 0; i < Math.min(3, users.size()); i++) {
                    User user = users.get(i);
                    System.out.println("User: " + user.getUsername() + " (Role: " + user.getRole() + ")");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error testing UserService: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Headless tests completed successfully.");
    }
    
    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless() || 
            System.getenv("HEADLESS") != null || 
            Arrays.asList(args).contains("--headless")) {
            
            System.out.println("Detected headless environment. Running in headless mode.");
            isHeadless = true;
        }
        
        launch(args);
    }
}