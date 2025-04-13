package com.bookshop.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

@Deprecated
public class DatabaseConnection {
    
    private static DatabaseConnection instance;
    private Connection connection;
    
    private String url;
    private String user;
    private String password;
    
    private DatabaseConnection() {
        Map<String, String> env = System.getenv();
        
        String dbHost = env.getOrDefault("PGHOST", "localhost");
        String dbPort = env.getOrDefault("PGPORT", "5432");
        String dbName = env.getOrDefault("PGDATABASE", "bookshop");
        
        this.url = env.getOrDefault("DATABASE_URL", "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName);
        this.user = env.getOrDefault("PGUSER", "postgres");
        this.password = env.getOrDefault("PGPASSWORD", "905477");
        
        if (this.url.startsWith("postgres://")) {
            this.url = this.url.replace("postgres://", "jdbc:postgresql://");
        }
        
        System.out.println("Note: Using deprecated DatabaseConnection class. Consider migrating to com.bookshop.utils.DatabaseConnection");
    }
    
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
                
                connection = DriverManager.getConnection(url, user, password);
                
                System.out.println("PostgreSQL database connection established");
            } catch (ClassNotFoundException e) {
                throw new SQLException("PostgreSQL JDBC driver not found", e);
            }
        }
        return connection;
    }
    
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("PostgreSQL database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing PostgreSQL database connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}