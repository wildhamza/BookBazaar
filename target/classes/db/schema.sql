-- Drop tables if they exist (in correct order to handle foreign keys)
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    address TEXT,
    phone_number VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create books table
CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    publisher VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    image_url TEXT,
    description TEXT,
    stock_quantity INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create reviews table
CREATE TABLE reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    user_id INT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create orders table
CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    shipping_address TEXT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create order_items table
CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    book_id INT,
    book_title VARCHAR(255) NOT NULL,
    book_author VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    price_at_purchase DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE SET NULL
);

-- Insert admin user (password: admin123)
INSERT INTO users (username, password_hash, full_name, email, address, phone_number, role)
VALUES ('admin', 'admin123', 'Admin User', 'admin@bookshop.com', '123 Admin St', '555-123-4567', 'ADMIN');

-- Insert sample books
INSERT INTO books (title, author, publisher, price, category, isbn, image_url, description, stock_quantity)
VALUES 
('The Great Gatsby', 'F. Scott Fitzgerald', 'Scribner', 12.99, 'Fiction', '9780743273565', 'https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg', 'The story of eccentric millionaire Jay Gatsby and his passion for the beautiful Daisy Buchanan.', 50),
('To Kill a Mockingbird', 'Harper Lee', 'HarperCollins', 14.99, 'Fiction', '9780061120084', 'https://covers.openlibrary.org/b/isbn/9780061120084-L.jpg', 'The story of a young girl confronting racism in a small Southern town.', 45),
('1984', 'George Orwell', 'Signet Classics', 9.99, 'Fiction', '9780451524935', 'https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg', 'A dystopian novel set in a totalitarian society ruled by Big Brother.', 30),
('The Hobbit', 'J.R.R. Tolkien', 'Houghton Mifflin Harcourt', 13.99, 'Fantasy', '9780547928227', 'https://covers.openlibrary.org/b/isbn/9780547928227-L.jpg', 'Bilbo Baggins is a hobbit who enjoys a comfortable life until he joins a quest.', 25),
('Pride and Prejudice', 'Jane Austen', 'Penguin Classics', 8.99, 'Fiction', '9780141439518', 'https://covers.openlibrary.org/b/isbn/9780141439518-L.jpg', 'The story of Elizabeth Bennet and her complicated relationship with Mr. Darcy.', 40),
('The Catcher in the Rye', 'J.D. Salinger', 'Little, Brown and Company', 10.99, 'Fiction', '9780316769488', 'https://covers.openlibrary.org/b/isbn/9780316769488-L.jpg', 'The story of Holden Caulfield, a teenage boy who has been expelled from prep school.', 35),
('Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', 'Scholastic', 15.99, 'Fantasy', '9780590353427', 'https://covers.openlibrary.org/b/isbn/9780590353427-L.jpg', 'The first book in the Harry Potter series.', 60),
('The Lord of the Rings', 'J.R.R. Tolkien', 'Houghton Mifflin Harcourt', 19.99, 'Fantasy', '9780618640157', 'https://covers.openlibrary.org/b/isbn/9780618640157-L.jpg', 'The epic quest to destroy the One Ring.', 20),
('Brave New World', 'Aldous Huxley', 'Harper Perennial Modern Classics', 11.99, 'Fiction', '9780060850524', 'https://covers.openlibrary.org/b/isbn/9780060850524-L.jpg', 'A dystopian novel set in a genetically-engineered future.', 30),
('The Da Vinci Code', 'Dan Brown', 'Anchor', 9.99, 'Thriller', '9780307474278', 'https://covers.openlibrary.org/b/isbn/9780307474278-L.jpg', 'A mystery thriller novel that explores a conspiracy within the Catholic Church.', 40);

-- Insert sample customer
INSERT INTO users (username, password_hash, full_name, email, address, phone_number, role)
VALUES ('customer', 'customer123', 'John Doe', 'customer@example.com', '456 Customer St', '555-987-6543', 'CUSTOMER');

-- Insert sample reviews
INSERT INTO reviews (book_id, user_id, rating, comment)
VALUES 
(1, 2, 5, 'A timeless classic that everyone should read!'),
(2, 2, 4, 'Powerful story about justice and morality.'),
(3, 2, 5, 'Scarily relevant even today.'),
(4, 2, 4, 'A wonderful adventure story.'),
(5, 2, 3, 'Classic romance, but a bit slow-paced.');

-- Insert sample order
INSERT INTO orders (user_id, total_amount, status, shipping_address, payment_method)
VALUES (2, 35.97, 'DELIVERED', '456 Customer St', 'Credit Card');

-- Insert sample order items
INSERT INTO order_items (order_id, book_id, book_title, book_author, quantity, price_at_purchase)
VALUES 
(1, 1, 'The Great Gatsby', 'F. Scott Fitzgerald', 1, 12.99),
(1, 3, '1984', 'George Orwell', 1, 9.99),
(1, 4, 'The Hobbit', 'J.R.R. Tolkien', 1, 13.99); 