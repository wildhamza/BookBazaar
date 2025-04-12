-- Drop tables if they exist
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    address TEXT,
    phone_number VARCHAR(20),
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'CUSTOMER'))
);

-- Create books table
CREATE TABLE books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    publisher VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    image_url TEXT,
    description TEXT,
    stock_quantity INTEGER NOT NULL DEFAULT 0
);

-- Create reviews table
CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    book_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE (book_id, user_id)
);

-- Create orders table
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    shipping_address TEXT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create order_items table
CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL,
    book_id INTEGER NOT NULL,
    book_title VARCHAR(255) NOT NULL,
    book_author VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL,
    price_at_purchase DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX idx_books_title ON books (title);
CREATE INDEX idx_books_author ON books (author);
CREATE INDEX idx_books_category ON books (category);
CREATE INDEX idx_reviews_book_id ON reviews (book_id);
CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);

-- Insert admin user (password: admin123)
INSERT INTO users (username, password_hash, full_name, email, address, phone_number, role)
VALUES ('admin', '$2a$12$M8OSI5ZMKpWRn3tCjbzh6eXwkXQJmQg9Hw66O9Z6U0RW9wLqCCL5W', 'Admin User', 'admin@bookshop.com', '123 Admin St', '555-123-4567', 'ADMIN');

-- Insert default customer user (password: customer123)
INSERT INTO users (username, password_hash, full_name, email, address, phone_number, role)
VALUES ('customer', '$2a$12$h.dl5J86rGH7I8bD9bZeZeci0pDt0.VwR.k5.5wcn4p/7ZpQzJCqO', 'Regular Customer', 'customer@example.com', '456 Reader Lane', '555-987-6543', 'CUSTOMER');

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
('The Da Vinci Code', 'Dan Brown', 'Anchor', 9.99, 'Thriller', '9780307474278', 'https://covers.openlibrary.org/b/isbn/9780307474278-L.jpg', 'A mystery thriller novel that explores a conspiracy within the Catholic Church.', 40),
('Dune', 'Frank Herbert', 'Ace Books', 15.99, 'Science Fiction', '9780441172719', 'https://covers.openlibrary.org/b/isbn/9780441172719-L.jpg', 'Set on the desert planet Arrakis, Dune is the story of Paul Atreides, who would become the mysterious Muad''Dib.', 25),
('The Alchemist', 'Paulo Coelho', 'HarperOne', 9.99, 'Fiction', '9780062315007', 'https://covers.openlibrary.org/b/isbn/9780062315007-L.jpg', 'A magical story about following your dreams.', 35),
('Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', 'Harper', 16.99, 'Non-Fiction', '9780062316097', 'https://covers.openlibrary.org/b/isbn/9780062316097-L.jpg', 'A groundbreaking narrative of humanity''s creation and evolution.', 30),
('The Hunger Games', 'Suzanne Collins', 'Scholastic Press', 12.99, 'Young Adult', '9780439023481', 'https://covers.openlibrary.org/b/isbn/9780439023481-L.jpg', 'In a dystopian future, a young girl fights for survival in a televised competition.', 45),
('The Shining', 'Stephen King', 'Anchor', 11.99, 'Horror', '9780307743657', 'https://covers.openlibrary.org/b/isbn/9780307743657-L.jpg', 'A family heads to an isolated hotel for the winter where an evil presence influences the father.', 20),
('The Girl with the Dragon Tattoo', 'Stieg Larsson', 'Vintage Crime', 10.99, 'Mystery', '9780307454546', 'https://covers.openlibrary.org/b/isbn/9780307454546-L.jpg', 'A journalist and a hacker investigate a wealthy family''s dark secrets.', 25),
('The Road', 'Cormac McCarthy', 'Vintage Books', 14.99, 'Fiction', '9780307387899', 'https://covers.openlibrary.org/b/isbn/9780307387899-L.jpg', 'A father and son walk alone through burned America, heading through the ravaged landscape to the coast.', 15),
('Educated', 'Tara Westover', 'Random House', 13.99, 'Memoir', '9780399590504', 'https://covers.openlibrary.org/b/isbn/9780399590504-L.jpg', 'A memoir about a young girl who leaves her survivalist family and goes on to earn a PhD from Cambridge University.', 30),
('The Night Circus', 'Erin Morgenstern', 'Anchor Books', 12.99, 'Fantasy', '9780307744432', 'https://covers.openlibrary.org/b/isbn/9780307744432-L.jpg', 'A competition between two young magicians set in a mysterious circus that only appears at night.', 25),
('Where the Crawdads Sing', 'Delia Owens', 'G.P. Putnam''s Sons', 14.99, 'Fiction', '9780735219090', 'https://covers.openlibrary.org/b/isbn/9780735219090-L.jpg', 'A coming-of-age story and a surprising tale of possible murder.', 40);

-- Insert additional users
INSERT INTO users (username, password_hash, full_name, email, address, phone_number, role)
VALUES 
('sarah', '$2a$12$h.dl5J86rGH7I8bD9bZeZeci0pDt0.VwR.k5.5wcn4p/7ZpQzJCqO', 'Sarah Johnson', 'sarah@example.com', '789 Book Ave', '555-345-6789', 'CUSTOMER'),
('michael', '$2a$12$h.dl5J86rGH7I8bD9bZeZeci0pDt0.VwR.k5.5wcn4p/7ZpQzJCqO', 'Michael Smith', 'michael@example.com', '101 Reader Blvd', '555-567-8901', 'CUSTOMER'),
('emma', '$2a$12$h.dl5J86rGH7I8bD9bZeZeci0pDt0.VwR.k5.5wcn4p/7ZpQzJCqO', 'Emma Wilson', 'emma@example.com', '202 Novel St', '555-678-9012', 'CUSTOMER');

-- Insert sample reviews
INSERT INTO reviews (book_id, user_id, rating, comment, created_at)
VALUES 
(1, 2, 5, 'A true classic! The prose is beautiful and the story timeless.', '2023-01-15 10:30:00'),
(2, 2, 4, 'Powerful story that tackles important social issues.', '2023-01-20 15:45:00'),
(3, 3, 5, 'Chilling and thought-provoking. More relevant now than ever.', '2023-02-05 18:20:00'),
(4, 3, 5, 'The adventure that started my love for fantasy!', '2023-02-10 14:10:00'),
(5, 4, 4, 'Jane Austen at her best. Witty and engaging.', '2023-03-01 09:15:00'),
(7, 4, 5, 'Magical and perfect for all ages.', '2023-03-15 11:30:00'),
(11, 2, 4, 'Epic sci-fi at its finest. Complex world-building.', '2023-04-02 16:45:00'),
(13, 3, 5, 'Changed my perspective on human history.', '2023-04-10 13:20:00'),
(15, 4, 4, 'Genuinely scary. Couldn''t put it down!', '2023-05-05 20:15:00'),
(18, 3, 5, 'Heartbreaking and beautifully written.', '2023-05-12 17:30:00');

-- Insert sample orders
INSERT INTO orders (user_id, order_date, total_amount, status, shipping_address, payment_method)
VALUES
(2, '2023-06-01 09:15:00', 37.97, 'DELIVERED', '456 Reader Lane', 'Credit Card'),
(3, '2023-06-15 14:30:00', 43.97, 'DELIVERED', '789 Book Ave', 'PayPal'),
(4, '2023-07-01 11:45:00', 28.98, 'SHIPPED', '101 Reader Blvd', 'Credit Card'),
(2, '2023-07-15 16:20:00', 26.98, 'PROCESSING', '456 Reader Lane', 'Credit Card'),
(3, '2023-08-01 10:00:00', 46.97, 'PENDING', '789 Book Ave', 'PayPal');

-- Insert sample order items
INSERT INTO order_items (order_id, book_id, book_title, book_author, quantity, price_at_purchase)
VALUES
(1, 1, 'The Great Gatsby', 'F. Scott Fitzgerald', 1, 12.99),
(1, 5, 'Pride and Prejudice', 'Jane Austen', 1, 8.99),
(1, 3, '1984', 'George Orwell', 1, 9.99),
(2, 7, 'Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', 1, 15.99),
(2, 4, 'The Hobbit', 'J.R.R. Tolkien', 2, 13.99),
(3, 9, 'Brave New World', 'Aldous Huxley', 1, 11.99),
(3, 10, 'The Da Vinci Code', 'Dan Brown', 1, 9.99),
(3, 5, 'Pride and Prejudice', 'Jane Austen', 1, 8.99),
(4, 12, 'The Alchemist', 'Paulo Coelho', 1, 9.99),
(4, 11, 'Dune', 'Frank Herbert', 1, 15.99),
(5, 14, 'The Hunger Games', 'Suzanne Collins', 2, 12.99),
(5, 17, 'Educated', 'Tara Westover', 1, 13.99),
(5, 20, 'Where the Crawdads Sing', 'Delia Owens', 1, 14.99);
