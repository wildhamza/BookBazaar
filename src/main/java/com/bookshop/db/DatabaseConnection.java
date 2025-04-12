package com.bookshop.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

/**
 * Singleton class to manage database connections.
 */
public class DatabaseConnection {
    
    private static DatabaseConnection instance;
    private Connection connection;
    
    // Database configuration
    private String url;
    private String user;
    private String password;
    
    /**
     * Private constructor to enforce Singleton pattern.
     */
    private DatabaseConnection() {
        // Load database configuration from environment variables
        Map<String, String> env = System.getenv();
        
        this.url = env.getOrDefault("DATABASE_URL", "jdbc:postgresql://localhost:5432/bookshop");
        this.user = env.getOrDefault("PGUSER", "postgres");
        this.password = env.getOrDefault("PGPASSWORD", "password");
        
        // Extract proper JDBC URL if using the full Postgres URL format
        if (this.url.startsWith("postgres://")) {
            this.url = this.url.replace("postgres://", "jdbc:postgresql://");
        }
    }
    
    /**
     * Get the singleton instance of the database connection.
     * 
     * @return The singleton instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Get a connection to the database.
     * 
     * @return The database connection
     * @throws SQLException If a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load the JDBC driver
                Class.forName("org.postgresql.Driver");
                
                // Create the connection
                connection = DriverManager.getConnection(url, user, password);
                
                // Use connection.setAutoCommit(false) if manual transaction management is needed
                
                System.out.println("Database connection established");
            } catch (ClassNotFoundException e) {
                throw new SQLException("PostgreSQL JDBC driver not found", e);
            }
        }
        return connection;
    }
    
    /**
     * Close the database connection.
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}