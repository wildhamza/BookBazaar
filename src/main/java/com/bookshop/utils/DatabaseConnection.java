package com.bookshop.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton class for managing database connections.
 * Implements the Singleton design pattern to ensure a single database connection instance
 * is reused throughout the application, promoting resource efficiency.
 */
public class DatabaseConnection {
    
    private static DatabaseConnection instance;
    private Connection connection;
    
    // Database configuration - using PostgreSQL environment variables
    private static final String DB_URL = System.getenv("DATABASE_URL");
    private static final String DB_USER = System.getenv("PGUSER") != null ? System.getenv("PGUSER") : "postgres";
    private static final String DB_PASSWORD = System.getenv("PGPASSWORD") != null ? System.getenv("PGPASSWORD") : "password";
    private static final String DB_HOST = System.getenv("PGHOST") != null ? System.getenv("PGHOST") : "localhost";
    private static final String DB_PORT = System.getenv("PGPORT") != null ? System.getenv("PGPORT") : "5432";
    private static final String DB_NAME = System.getenv("PGDATABASE") != null ? System.getenv("PGDATABASE") : "bookshop";
    
    /**
     * Private constructor to prevent direct instantiation.
     * This is a key aspect of the Singleton pattern - the constructor is made private
     * to ensure that the class cannot be instantiated from outside, forcing the use
     * of the getInstance() method to access the single instance.
     * 
     * @throws SQLException If a database error occurs
     */
    private DatabaseConnection() throws SQLException {
        try {
            // Load the PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            
            // Establish connection using available parameters
            if (DB_HOST != null && !DB_HOST.isEmpty() && DB_PORT != null && !DB_PORT.isEmpty() && 
                DB_NAME != null && !DB_NAME.isEmpty()) {
                
                // Connect using individual parameters
                String jdbcUrl = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
                System.out.println("Connecting to PostgreSQL: " + jdbcUrl);
                connection = DriverManager.getConnection(
                    jdbcUrl,
                    DB_USER,
                    DB_PASSWORD
                );
            } else if (DB_URL != null && !DB_URL.isEmpty()) {
                // Use the provided DATABASE_URL (useful for cloud hosting services)
                String url = DB_URL;
                
                // Format could be: postgres://username:password@hostname:port/database_name
                if (url.startsWith("postgres://")) {
                    String jdbcUrl = url.replace("postgres://", "jdbc:postgresql://");
                    System.out.println("Connecting to PostgreSQL with URL: " + jdbcUrl);
                    connection = DriverManager.getConnection(jdbcUrl);
                } else {
                    // Use URL as is, assuming it's a valid JDBC URL
                    System.out.println("Connecting using direct URL: " + url);
                    connection = DriverManager.getConnection(url);
                }
            } else {
                // Use default localhost connection
                String jdbcUrl = "jdbc:postgresql://localhost:5432/bookshop";
                System.out.println("Connecting to default PostgreSQL: " + jdbcUrl);
                connection = DriverManager.getConnection(jdbcUrl, "postgres", "password");
            }
            
            // Initialize database if needed
            initializeDatabase();
            
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found! Make sure the PostgreSQL connector JAR is in the classpath.");
            e.printStackTrace();
            throw new SQLException("Database driver not found", e);
        } catch (SQLException e) {
            System.err.println("Failed to connect to PostgreSQL database!");
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Get the singleton instance.
     * 
     * @return The singleton instance
     * @throws SQLException If a database error occurs
     */
    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else if (instance.connection.isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Get the database connection.
     * 
     * @return The database connection
     * @throws SQLException If a database error occurs
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            // Re-establish connection if closed
            instance = new DatabaseConnection();
        }
        return connection;
    }
    
    /**
     * Close the database connection.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection!");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Initialize the database schema if needed.
     * This method creates all necessary tables with appropriate relationships,
     * ensuring the database is properly structured for application needs.
     * 
     * @throws SQLException If a database error occurs
     */
    private void initializeDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create users table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id SERIAL PRIMARY KEY, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) NOT NULL, " +
                "address TEXT, " +
                "phone_number VARCHAR(20), " +
                "role VARCHAR(20) NOT NULL, " +
                "order_count INT DEFAULT 0" +
                ")"
            );
            
            // Create books table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS books (" +
                "id SERIAL PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "author VARCHAR(100) NOT NULL, " +
                "publisher VARCHAR(100) NOT NULL, " +
                "price DECIMAL(10, 2) NOT NULL, " +
                "category VARCHAR(50) NOT NULL, " +
                "isbn VARCHAR(20) NOT NULL, " +
                "image_url TEXT, " +
                "description TEXT, " +
                "stock_quantity INT NOT NULL DEFAULT 0" +
                ")"
            );
            
            // Create cart_items table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS cart_items (" +
                "id SERIAL PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "book_id INT NOT NULL, " +
                "quantity INT DEFAULT 1, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE, " +
                "CONSTRAINT unique_user_book UNIQUE (user_id, book_id)" +
                ")"
            );
            
            // Create orders table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS orders (" +
                "id SERIAL PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "status VARCHAR(20) DEFAULT 'pending', " +
                "total_amount DECIMAL(10, 2) NOT NULL, " +
                "shipping_address TEXT, " +
                "payment_method VARCHAR(50), " +
                "discount_applied DECIMAL(5, 2) DEFAULT 0.0, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")"
            );
            
            // Create order_items table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS order_items (" +
                "id SERIAL PRIMARY KEY, " +
                "order_id INT NOT NULL, " +
                "book_id INT NOT NULL, " +
                "quantity INT NOT NULL, " +
                "price DECIMAL(10, 2) NOT NULL, " +
                "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE RESTRICT" +
                ")"
            );
            
            // Create reviews table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS reviews (" +
                "id SERIAL PRIMARY KEY, " +
                "book_id INT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5), " +
                "comment TEXT, " +
                "review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "CONSTRAINT unique_user_book_review UNIQUE (user_id, book_id)" +
                ")"
            );
            
            // Insert admin user if it doesn't exist
            stmt.execute(
                "INSERT INTO users (username, password_hash, full_name, email, role) " +
                "SELECT 'admin', '$2a$10$h.dl5J86rGH7I8bD9bZeZeci0pDt0.VwR.k5.5wcn4p/7ZpQzJCqO', 'Admin User', 'admin@bookshop.com', 'ADMIN' " +
                "WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')"
            );
            
            // Check if books table is empty
            boolean booksExist = false;
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT 1 FROM books LIMIT 1")) {
                booksExist = rs.next();
            }
            
            // Insert sample books only if the table is empty
            if (!booksExist) {
                stmt.execute(
                    "INSERT INTO books (title, author, isbn, publisher, category, description, price, stock_quantity) " +
                    "VALUES ('To Kill a Mockingbird', 'Harper Lee', '9780061120084', 'HarperCollins', 'Fiction', " +
                    "'A classic novel about racial injustice in the American South.', 12.99, 50)"
                );
                
                stmt.execute(
                    "INSERT INTO books (title, author, isbn, publisher, category, description, price, stock_quantity) " +
                    "VALUES ('1984', 'George Orwell', '9780451524935', 'Signet Classic', 'Fiction', " +
                    "'A dystopian novel set in a totalitarian society.', 9.99, 45)"
                );
                
                stmt.execute(
                    "INSERT INTO books (title, author, isbn, publisher, category, description, price, stock_quantity) " +
                    "VALUES ('Pride and Prejudice', 'Jane Austen', '9780141439518', 'Penguin Classics', 'Fiction', " +
                    "'A romantic novel about the Bennet sisters and their suitors.', 8.99, 30)"
                );
            }
            
            System.out.println("PostgreSQL database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing PostgreSQL database!");
            e.printStackTrace();
            throw e;
        }
    }
}