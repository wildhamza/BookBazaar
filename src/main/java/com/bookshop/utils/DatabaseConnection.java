package com.bookshop.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    
    private static DatabaseConnection instance;
    private Connection connection;
    
    private static final String DB_URL = System.getenv("DATABASE_URL");
    private static final String DB_USER = System.getenv("PGUSER") != null ? System.getenv("PGUSER") : "postgres";
    private static final String DB_PASSWORD = System.getenv("PGPASSWORD") != null ? System.getenv("PGPASSWORD") : "905477";
    private static final String DB_HOST = System.getenv("PGHOST") != null ? System.getenv("PGHOST") : "localhost";
    private static final String DB_PORT = System.getenv("PGPORT") != null ? System.getenv("PGPORT") : "5432";
    private static final String DB_NAME = System.getenv("PGDATABASE") != null ? System.getenv("PGDATABASE") : "bookshop";
    
    private DatabaseConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");

            if (DB_HOST != null && !DB_HOST.isEmpty() && DB_PORT != null && !DB_PORT.isEmpty() &&
                    DB_NAME != null && !DB_NAME.isEmpty()) {

                String jdbcUrl = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
                System.out.println("Connecting to PostgreSQL: " + jdbcUrl);
                connection = DriverManager.getConnection(
                        jdbcUrl,
                        DB_USER,
                        DB_PASSWORD);
            } else if (DB_URL != null && !DB_URL.isEmpty()) {
                String url = DB_URL;

                if (url.startsWith("postgres://")) {
                    String jdbcUrl = url.replace("postgres://", "jdbc:postgresql://");
                    System.out.println("Connecting to PostgreSQL with URL: " + jdbcUrl);
                    connection = DriverManager.getConnection(jdbcUrl);
                } else {
                    System.out.println("Connecting using direct URL: " + url);
                    connection = DriverManager.getConnection(url);
                }
            } else {
                String jdbcUrl = "jdbc:postgresql://localhost:5432/bookshop";
                System.out.println("Connecting to default PostgreSQL: " + jdbcUrl);
                connection = DriverManager.getConnection(jdbcUrl, "postgres", "admin123");
            }

            initializeDatabase();

        } catch (ClassNotFoundException e) {
            System.err.println(
                    "PostgreSQL JDBC driver not found! Make sure the PostgreSQL connector JAR is in the classpath.");
            e.printStackTrace();
            throw new SQLException("Database driver not found", e);
        } catch (SQLException e) {
            System.err.println("Failed to connect to PostgreSQL database!");
            e.printStackTrace();
            throw e;
        }
    }
    
    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else if (instance.connection.isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            instance = new DatabaseConnection();
        }
        return connection;
    }
    
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
    
    private void initializeDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
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
            
            stmt.execute(
                "INSERT INTO users (username, password_hash, full_name, email, role) " +
                "SELECT 'admin', '$2a$10$h.dl5J86rGH7I8bD9bZeZeci0pDt0.VwR.k5.5wcn4p/7ZpQzJCqO', 'Admin User', 'admin@bookshop.com', 'ADMIN' " +
                "WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')"
            );
            
            stmt.execute(
                "INSERT INTO users (username, password_hash, full_name, email, address, phone_number, role) " +
                "SELECT 'customer', '$2a$12$h.dl5J86rGH7I8bD9bZeZeci0pDt0.VwR.k5.5wcn4p/7ZpQzJCqO', 'Regular Customer', 'customer@example.com', '456 Reader Lane', '555-987-6543', 'CUSTOMER' " +
                "WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'customer')"
            );
            
            boolean booksExist = false;
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT 1 FROM books LIMIT 1")) {
                booksExist = rs.next();
            }
            
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
                
                stmt.execute(
                    "INSERT INTO books (title, author, isbn, publisher, category, description, price, stock_quantity) " +
                    "VALUES ('The Great Gatsby', 'F. Scott Fitzgerald', '9780743273565', 'Scribner', 'Fiction', " +
                    "'A novel about the mysterious Jay Gatsby and his love for Daisy Buchanan.', 10.99, 40)"
                );
                
                stmt.execute(
                    "INSERT INTO books (title, author, isbn, publisher, category, description, price, stock_quantity) " +
                    "VALUES ('The Hobbit', 'J.R.R. Tolkien', '9780547928227', 'Houghton Mifflin', 'Fantasy', " +
                    "'A fantasy novel about the journey of Bilbo Baggins to reclaim a treasure guarded by a dragon.', 14.99, 55)"
                );
                
                stmt.execute(
                    "INSERT INTO books (title, author, isbn, publisher, category, description, price, stock_quantity) " +
                    "VALUES ('Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', '9780590353427', 'Scholastic', 'Fantasy', " +
                    "'The first book in the Harry Potter series about a young wizard and his adventures.', 11.99, 60)"
                );
                
                stmt.execute(
                    "INSERT INTO books (title, author, isbn, publisher, category, description, price, stock_quantity) " +
                    "VALUES ('The Catcher in the Rye', 'J.D. Salinger', '9780316769488', 'Little, Brown and Company', 'Fiction', " +
                    "'A novel about teenage alienation and loss of innocence.', 9.49, 35)"
                );
                
                stmt.execute(
                    "INSERT INTO books (title, author, isbn, publisher, category, description, price, stock_quantity) " +
                    "VALUES ('The Lord of the Rings', 'J.R.R. Tolkien', '9780618640157', 'Mariner Books', 'Fantasy', " +
                    "'An epic fantasy trilogy following the quest to destroy the One Ring.', 19.99, 45)"
                );
                
                stmt.execute(
                    "INSERT INTO books (title, author, isbn, publisher, category, description, price, stock_quantity) " +
                    "VALUES ('Brave New World', 'Aldous Huxley', '9780060850524', 'Harper Perennial', 'Science Fiction', " +
                    "'A dystopian novel set in a futuristic World State of genetically modified citizens.', 10.49, 38)"
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