package com.bookshop.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * A singleton class for managing database connections.
 */
public class DatabaseConnection {
    
    private static DatabaseConnection instance;
    private Connection connection;
    
    // Database connection details
    private final String url = "jdbc:postgresql://localhost:5432/bookshop";
    private final String user = "postgres";
    private final String password = "password";
    
    /**
     * Private constructor to prevent instantiation.
     */
    private DatabaseConnection() {
        try {
            // Try to establish a connection
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets the singleton instance.
     * 
     * @return The instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Gets the database connection.
     * 
     * @return The connection
     * @throws SQLException If a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        // Check if connection is closed or invalid
        if (connection == null || connection.isClosed()) {
            // Re-establish connection
            this.connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }
    
    /**
     * Closes the database connection.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}