package com.bookshop.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Singleton pattern implementation for database connection.
 * Manages a single connection instance throughout the application.
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/bookshop";
    
    // Private constructor to prevent instantiation
    private DatabaseConnection() {
        try {
            String dbUrl = System.getenv("DATABASE_URL");
            String dbUser = System.getenv("PGUSER");
            String dbPassword = System.getenv("PGPASSWORD");
            String dbHost = System.getenv("PGHOST");
            String dbPort = System.getenv("PGPORT");
            String dbName = System.getenv("PGDATABASE");
            
            // Use environment variables if available, otherwise use default
            if (dbUrl == null && dbHost != null && dbPort != null && dbName != null) {
                dbUrl = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
            } else if (dbUrl == null) {
                dbUrl = DEFAULT_URL;
            }
            
            // Connect to database
            if (dbUser != null && dbPassword != null) {
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            } else {
                connection = DriverManager.getConnection(dbUrl);
            }
            
            System.out.println("Database connection established successfully");
            
            // Initialize database if needed
            initializeDatabase();
            
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize database tables if they don't exist
     */
    private void initializeDatabase() {
        try {
            // Read SQL script from resources
            InputStream is = getClass().getClassLoader().getResourceAsStream("db/init_database.sql");
            if (is == null) {
                System.err.println("Could not find init_database.sql");
                return;
            }
            
            String sql = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
            
            // Execute SQL script
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
            stmt.close();
            
            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets the single instance of DatabaseConnection.
     * 
     * @return The DatabaseConnection instance
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
     * @return The active Connection object
     */
    public Connection getConnection() {
        return connection;
    }
    
    /**
     * Closes the database connection.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
