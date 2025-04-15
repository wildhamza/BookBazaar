package com.bookshop;

import com.bookshop.services.BookService;
import com.bookshop.services.UserService;
import com.bookshop.services.CartService;
import com.bookshop.services.ReviewService;
import com.bookshop.services.DiscountService;
import com.bookshop.services.OrderService;
import com.bookshop.models.*;
import com.bookshop.utils.SessionManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class ConsoleMain {
    
    private static Scanner scanner = new Scanner(System.in);
    private static BookService bookService;
    private static UserService userService;
    private static CartService cartService;
    private static OrderService orderService;
    private static ReviewService reviewService;
    private static DiscountService discountService;
    private static User currentUser = null;
    
    public static void main(String[] args) {
        System.out.println("BookShop Console Application");
        System.out.println("----------------------------");
        
        try {
            bookService = new BookService();
            userService = new UserService();
            cartService = CartService.getInstance();
            orderService = new OrderService();
            reviewService = new ReviewService();
            discountService = new DiscountService();
            
            runDatabaseTests();
            
            if (args.length > 0 && args[0].equals("--test")) {
                System.out.println("Test mode completed successfully.");
            } else {
                runInteractiveMode();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
     
    private static void runDatabaseTests() throws SQLException {
        System.out.println("Testing database connection...");
        
        List<Book> books = bookService.getAllBooks();
        System.out.println("Found " + books.size() + " books in the database.");
        
        if (!books.isEmpty()) {
            for (int i = 0; i < Math.min(3, books.size()); i++) {
                Book book = books.get(i);
                System.out.println("Book: " + book.getTitle() + " by " + book.getAuthor() + " (€" + book.getPrice() + ")");
            }
            
            Book firstBook = books.get(0);
            Book bookById = bookService.getBookById(firstBook.getId());
            if (bookById != null) {
                System.out.println("Successfully retrieved book by ID: " + bookById.getTitle());
            }
        }
        
        System.out.println("Testing UserService...");
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
    }
    
    private static void runInteractiveMode() {
        boolean running = true;
        
        while (running) {
            if (currentUser == null) {
                displayLoginMenu();
            } else if (currentUser.getRole().equals("ADMIN")) {
                displayAdminMenu();
            } else {
                displayCustomerMenu();
            }
            
            int choice = getIntInput("Enter your choice: ");
            
            try {
                if (currentUser == null) {
                    running = handleLoginMenu(choice);
                } else if (currentUser.getRole().equals("ADMIN")) {
                    running = handleAdminMenu(choice);
                } else {
                    running = handleCustomerMenu(choice);
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        
        System.out.println("Thank you for using BookShop Console Application!");
    }
    
    // Menu display methods
    
    private static void displayLoginMenu() {
        System.out.println("\n=== Login Menu ===");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
    }
    
    private static void displayAdminMenu() {
        System.out.println("\n=== Admin Menu ===");
        System.out.println("Welcome, " + currentUser.getUsername() + " (Admin)");
        System.out.println("1. View All Books");
        System.out.println("2. Add New Book");
        System.out.println("3. Edit Book");
        System.out.println("4. Delete Book");
        System.out.println("5. View All Users");
        System.out.println("6. View All Orders");
        System.out.println("7. Logout");
        System.out.println("8. Exit");
    }
    
    private static void displayCustomerMenu() {
        System.out.println("\n=== Customer Menu ===");
        System.out.println("Welcome, " + currentUser.getUsername());
        System.out.println("1. View All Books");
        System.out.println("2. Search Books");
        System.out.println("3. View Cart");
        System.out.println("4. View Orders");
        System.out.println("5. View Profile");
        System.out.println("6. Logout");
        System.out.println("7. Exit");
    }
     
    private static boolean handleLoginMenu(int choice) throws SQLException {
        switch (choice) {
            case 1: 
                System.out.print("Username: ");
                String username = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();
                
                User user = userService.authenticateUser(username, password);
                if (user != null) {
                    currentUser = user;
                    SessionManager.getInstance().setCurrentUser(user);
                    System.out.println("Login successful!");
                } else {
                    System.out.println("Invalid username or password!");
                }
                return true;
                
            case 2: 
                User newUser = new User();
                System.out.print("Username: ");
                newUser.setUsername(scanner.nextLine());
                System.out.print("Full Name: ");
                newUser.setFullName(scanner.nextLine());
                System.out.print("Email: ");
                newUser.setEmail(scanner.nextLine());
                System.out.print("Password: ");
                String newPassword = scanner.nextLine();
                
                newUser.setRole("CUSTOMER");
                newUser.setOrderCount(0);
                
                boolean registered = userService.registerUser(newUser, newPassword);
                if (registered) {
                    System.out.println("Registration successful! Please login.");
                } else {
                    System.out.println("Registration failed. Username may already exist.");
                }
                return true;
                
            case 3: 
                return false;
                
            default:
                System.out.println("Invalid choice!");
                return true;
        }
    }
     
    private static boolean handleAdminMenu(int choice) throws SQLException {
        switch (choice) {
            case 1: 
                displayAllBooks();
                return true;
                
            case 2: 
                addNewBook();
                return true;
                
            case 3: 
                editBook();
                return true;
                
            case 4: 
                deleteBook();
                return true;
                
            case 5: 
                displayAllUsers();
                return true;
                
            case 6: 
                displayAllOrders();
                return true;
                
            case 7: 
                currentUser = null;
                SessionManager.getInstance().setCurrentUser(null);
                System.out.println("Logged out successfully!");
                return true;
                
            case 8: 
                return false;
                
            default:
                System.out.println("Invalid choice!");
                return true;
        }
    }
     
    private static boolean handleCustomerMenu(int choice) throws SQLException {
        switch (choice) {
            case 1: 
                displayAllBooks();
                System.out.print("Would you like to add a book to cart? (Y/N): ");
                String addToCart = scanner.nextLine();
                if (addToCart.equalsIgnoreCase("Y")) {
                    addBookToCart();
                }
                return true;
                
            case 2: 
                searchBooks();
                return true;
                
            case 3: 
                viewCart();
                return true;
                
            case 4: 
                viewOrders();
                return true;
                
            case 5: 
                viewProfile();
                return true;
                
            case 6: 
                currentUser = null;
                SessionManager.getInstance().setCurrentUser(null);
                System.out.println("Logged out successfully!");
                return true;
                
            case 7: 
                return false;
                
            default:
                System.out.println("Invalid choice!");
                return true;
        }
    }
     
    private static void displayAllBooks() throws SQLException {
        List<Book> books = bookService.getAllBooks();
        System.out.println("\n=== All Books ===");
        
        if (books.isEmpty()) {
            System.out.println("No books found!");
            return;
        }
        
        for (Book book : books) {
            System.out.println("ID: " + book.getId());
            System.out.println("Title: " + book.getTitle());
            System.out.println("Author: " + book.getAuthor());
            System.out.println("Category: " + book.getCategory());
            System.out.println("Price: €" + book.getPrice());
            System.out.println("Stock: " + book.getStockQuantity());
            System.out.println("--------------------");
        }
    }
     
    private static void addNewBook() throws SQLException {
        Book book = new Book();
        
        System.out.println("\n=== Add New Book ===");
        System.out.print("Title: ");
        book.setTitle(scanner.nextLine());
        System.out.print("Author: ");
        book.setAuthor(scanner.nextLine());
        System.out.print("Publisher: ");
        book.setPublisher(scanner.nextLine());
        System.out.print("ISBN: ");
        book.setIsbn(scanner.nextLine());
        System.out.print("Category: ");
        book.setCategory(scanner.nextLine());
        System.out.print("Description: ");
        book.setDescription(scanner.nextLine());
        
        BigDecimal price = new BigDecimal(getStringInput("Price: €"));
        book.setPrice(price);
        
        int stockQuantity = getIntInput("Stock Quantity: ");
        book.setStockQuantity(stockQuantity);
        
        int bookId = bookService.addBook(book);
        if (bookId > 0) {
            System.out.println("Book added successfully with ID: " + bookId);
        } else {
            System.out.println("Failed to add book!");
        }
    }
     
    private static void editBook() throws SQLException {
        int bookId = getIntInput("Enter Book ID to edit: ");
        Book book = bookService.getBookById(bookId);
        
        if (book == null) {
            System.out.println("Book not found!");
            return;
        }
        
        System.out.println("\n=== Edit Book ===");
        System.out.println("Current Title: " + book.getTitle());
        System.out.print("New Title (leave empty to keep current): ");
        String title = scanner.nextLine();
        if (!title.isEmpty()) {
            book.setTitle(title);
        }
        
        System.out.println("Current Author: " + book.getAuthor());
        System.out.print("New Author (leave empty to keep current): ");
        String author = scanner.nextLine();
        if (!author.isEmpty()) {
            book.setAuthor(author);
        }
        
        System.out.println("Current Price: €" + book.getPrice());
        System.out.print("New Price (leave empty to keep current): ");
        String priceStr = scanner.nextLine();
        if (!priceStr.isEmpty()) {
            book.setPrice(new BigDecimal(priceStr));
        }
        
        System.out.println("Current Stock: " + book.getStockQuantity());
        System.out.print("New Stock (leave empty to keep current): ");
        String stockStr = scanner.nextLine();
        if (!stockStr.isEmpty()) {
            book.setStockQuantity(Integer.parseInt(stockStr));
        }
        
        boolean updated = bookService.updateBook(book);
        if (updated) {
            System.out.println("Book updated successfully!");
        } else {
            System.out.println("Failed to update book!");
        }
    }
     
    private static void deleteBook() throws SQLException {
        int bookId = getIntInput("Enter Book ID to delete: ");
        
        System.out.print("Are you sure you want to delete this book? (Y/N): ");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            boolean deleted = bookService.deleteBook(bookId);
            if (deleted) {
                System.out.println("Book deleted successfully!");
            } else {
                System.out.println("Failed to delete book! It may be referenced in orders or reviews.");
            }
        } else {
            System.out.println("Delete operation cancelled.");
        }
    }
     
    private static void displayAllUsers() throws SQLException {
        List<User> users = userService.getAllUsers();
        System.out.println("\n=== All Users ===");
        
        if (users.isEmpty()) {
            System.out.println("No users found!");
            return;
        }
        
        for (User user : users) {
            System.out.println("ID: " + user.getId());
            System.out.println("Username: " + user.getUsername());
            System.out.println("Full Name: " + user.getFullName());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Role: " + user.getRole());
            System.out.println("Order Count: " + user.getOrderCount());
            System.out.println("--------------------");
        }
    }
     
    private static void displayAllOrders() throws SQLException {
        List<Order> orders = orderService.getAllOrders();
        System.out.println("\n=== All Orders ===");
        
        if (orders.isEmpty()) {
            System.out.println("No orders found!");
            return;
        }
        
        for (Order order : orders) {
            System.out.println("Order ID: " + order.getId());
            System.out.println("User ID: " + order.getUserId());
            System.out.println("Date: " + order.getOrderDate());
            System.out.println("Status: " + order.getStatus());
            System.out.println("Total Amount: €" + order.getTotalAmount());
            System.out.println("Payment Method: " + order.getPaymentMethod());
             
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            System.out.println("Items:");
            for (OrderItem item : items) {
                Book book = bookService.getBookById(item.getBookId());
                String bookTitle = book != null ? book.getTitle() : "Unknown Book";
                System.out.println("  - " + bookTitle + " (Qty: " + item.getQuantity() + ", Price: €" + item.getPrice() + ")");
            }
            System.out.println("--------------------");
        }
    }
     
    private static void searchBooks() throws SQLException {
        System.out.print("Enter search term: ");
        String searchTerm = scanner.nextLine();
        
        List<Book> books = bookService.searchBooks(searchTerm);
        System.out.println("\n=== Search Results ===");
        
        if (books.isEmpty()) {
            System.out.println("No books found matching '" + searchTerm + "'");
            return;
        }
        
        for (Book book : books) {
            System.out.println("ID: " + book.getId());
            System.out.println("Title: " + book.getTitle());
            System.out.println("Author: " + book.getAuthor());
            System.out.println("Category: " + book.getCategory());
            System.out.println("Price: €" + book.getPrice());
            System.out.println("--------------------");
        }
        
        if (currentUser != null && !currentUser.getRole().equals("ADMIN")) {
            System.out.print("Would you like to add a book to cart? (Y/N): ");
            String addToCart = scanner.nextLine();
            if (addToCart.equalsIgnoreCase("Y")) {
                addBookToCart();
            }
        }
    }
    
    private static void addBookToCart() throws SQLException {
        int bookId = getIntInput("Enter Book ID to add to cart: ");
        Book book = bookService.getBookById(bookId);
        
        if (book == null) {
            System.out.println("Book not found!");
            return;
        }
        
        if (book.getStockQuantity() <= 0) {
            System.out.println("Sorry, this book is out of stock!");
            return;
        }
        
        int quantity = getIntInput("Enter quantity: ");
        
        if (quantity <= 0) {
            System.out.println("Quantity must be greater than 0!");
            return;
        }
        
        if (quantity > book.getStockQuantity()) {
            System.out.println("Sorry, only " + book.getStockQuantity() + " copies available!");
            return;
        }
        
        boolean added = cartService.addToCart(currentUser.getId(), bookId, quantity);
        if (added) {
            System.out.println("Book added to cart successfully!");
        } else {
            System.out.println("Failed to add book to cart!");
        }
    }
    
    private static void viewCart() throws SQLException {
        List<CartItem> cartItems = cartService.getCartItems(currentUser.getId());
        System.out.println("\n=== Your Cart ===");
        
        if (cartItems.isEmpty()) {
            System.out.println("Your cart is empty!");
            return;
        }
        
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            Book book = bookService.getBookById(item.getBookId());
            if (book != null) {
                System.out.println("ID: " + item.getId());
                System.out.println("Book: " + book.getTitle());
                System.out.println("Price: €" + book.getPrice());
                System.out.println("Quantity: " + item.getQuantity());
                BigDecimal itemTotal = book.getPrice().multiply(new BigDecimal(item.getQuantity()));
                System.out.println("Subtotal: €" + itemTotal);
                System.out.println("--------------------");
                total = total.add(itemTotal);
            }
        }
        
        System.out.println("Total: €" + total);
        
        BigDecimal discountedTotal = discountService.calculateDiscountedPrice(total, currentUser);
        if (!discountedTotal.equals(total)) {
            BigDecimal discountAmount = total.subtract(discountedTotal);
            System.out.println("Discount: €" + discountAmount);
            System.out.println("Total after discount: €" + discountedTotal);
        }
        
        System.out.println("\nOptions:");
        System.out.println("1. Update quantities");
        System.out.println("2. Remove item");
        System.out.println("3. Checkout");
        System.out.println("4. Return to menu");
        
        int choice = getIntInput("Enter your choice: ");
        switch (choice) {
            case 1:
                updateCartItemQuantity();
                break;
            case 2:
                removeCartItem();
                break;
            case 3:
                checkout(discountedTotal);
                break;
            case 4:
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }
    
    private static void updateCartItemQuantity() throws SQLException {
        int cartItemId = getIntInput("Enter Cart Item ID to update: ");
        int newQuantity = getIntInput("Enter new quantity: ");
        
        if (newQuantity <= 0) {
            System.out.println("Quantity must be greater than 0!");
            return;
        }
        
        boolean updated = cartService.updateCartItemQuantity(cartItemId, newQuantity);
        if (updated) {
            System.out.println("Cart item updated successfully!");
        } else {
            System.out.println("Failed to update cart item!");
        }
    }
    
    private static void removeCartItem() throws SQLException {
        int cartItemId = getIntInput("Enter Cart Item ID to remove: ");
        
        boolean removed = cartService.removeFromCart(cartItemId);
        if (removed) {
            System.out.println("Cart item removed successfully!");
        } else {
            System.out.println("Failed to remove cart item!");
        }
    }
    
    @SuppressWarnings("unused")
    private static void checkout(BigDecimal totalAmount) throws SQLException {
        List<CartItem> cartItems = cartService.getCartItems(currentUser.getId());
        
        if (cartItems.isEmpty()) {
            System.out.println("Your cart is empty!");
            return;
        }
        
        System.out.println("\n=== Checkout ===");
        System.out.println("Total Amount: €" + totalAmount);
        
        System.out.print("Shipping Address: ");
        String shippingAddress = scanner.nextLine();
        
        System.out.println("Select Payment Method:");
        System.out.println("1. Credit Card");
        System.out.println("2. PayPal");
        System.out.println("3. Bank Transfer");
        
        int paymentChoice = getIntInput("Enter your choice: ");
        String paymentMethod;
        
        switch (paymentChoice) {
            case 1:
                paymentMethod = "Credit Card";
                break;
            case 2:
                paymentMethod = "PayPal";
                break;
            case 3:
                paymentMethod = "Bank Transfer";
                break;
            default:
                System.out.println("Invalid choice, defaulting to Credit Card");
                paymentMethod = "Credit Card";
        }
        
        System.out.print("Confirm purchase? (Y/N): ");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("Y")) {
            int orderId = orderService.createOrder(currentUser.getId(), cartItems, paymentMethod);
            if (orderId > 0) {
                System.out.println("Order placed successfully! Order ID: " + orderId);
                cartService.clearCart(currentUser.getId());
            } else {
                System.out.println("Failed to place order!");
            }
        } else {
            System.out.println("Checkout cancelled.");
        }
    }
    
    private static void viewOrders() throws SQLException {
        List<Order> orders = orderService.getOrdersByUser(currentUser.getId());
        System.out.println("\n=== Your Orders ===");
        
        if (orders.isEmpty()) {
            System.out.println("You have no orders!");
            return;
        }
        
        for (Order order : orders) {
            System.out.println("Order ID: " + order.getId());
            System.out.println("Date: " + order.getOrderDate());
            System.out.println("Status: " + order.getStatus());
            System.out.println("Total Amount: €" + order.getTotalAmount());
            System.out.println("Payment Method: " + order.getPaymentMethod());
            
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            System.out.println("Items:");
            for (OrderItem item : items) {
                Book book = bookService.getBookById(item.getBookId());
                String bookTitle = book != null ? book.getTitle() : "Unknown Book";
                System.out.println("  - " + bookTitle + " (Qty: " + item.getQuantity() + ", Price: €" + item.getPrice() + ")");
            }
            System.out.println("--------------------");
        }
    }
    
    private static void viewProfile() {
        System.out.println("\n=== Your Profile ===");
        System.out.println("Username: " + currentUser.getUsername());
        System.out.println("Full Name: " + currentUser.getFullName());
        System.out.println("Email: " + currentUser.getEmail());
        System.out.println("Address: " + (currentUser.getAddress() != null ? currentUser.getAddress() : "Not set"));
        System.out.println("Phone: " + (currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "Not set"));
        System.out.println("Order Count: " + currentUser.getOrderCount());
        
        if (currentUser.isPremiumMember()) {
            System.out.println("Status: Premium Member (15% discount)");
        } else if (currentUser.isRegularMember()) {
            System.out.println("Status: Regular Member (10% discount)");
        } else {
            System.out.println("Status: Standard Customer");
            int ordersToRegular = 5 - currentUser.getOrderCount();
            System.out.println("Place " + ordersToRegular + " more orders to become a Regular Member!");
        }
        
        System.out.println("\nOptions:");
        System.out.println("1. Update Profile");
        System.out.println("2. Change Password");
        System.out.println("3. Return to menu");
        
        int choice = getIntInput("Enter your choice: ");
        switch (choice) {
            case 1:
                updateProfile();
                break;
            case 2:
                changePassword();
                break;
            case 3:
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }
    
    private static void updateProfile() {
        System.out.println("\n=== Update Profile ===");

        System.out.println("Current Full Name: " + currentUser.getFullName());
        System.out.print("New Full Name (leave empty to keep current): ");
        String fullName = scanner.nextLine();
        if (!fullName.isEmpty()) {
            currentUser.setFullName(fullName);
        }

        System.out.println("Current Email: " + currentUser.getEmail());
        System.out.print("New Email (leave empty to keep current): ");
        String email = scanner.nextLine();
        if (!email.isEmpty()) {
            currentUser.setEmail(email);
        }

        System.out.println(
                "Current Address: " + (currentUser.getAddress() != null ? currentUser.getAddress() : "Not set"));
        System.out.print("New Address (leave empty to keep current): ");
        String address = scanner.nextLine();
        if (!address.isEmpty()) {
            currentUser.setAddress(address);
        }

        System.out.println(
                "Current Phone: " + (currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "Not set"));
        System.out.print("New Phone (leave empty to keep current): ");
        String phone = scanner.nextLine();
        if (!phone.isEmpty()) {
            currentUser.setPhoneNumber(phone);
        }

        try {
            boolean updated = userService.updateUserProfile(currentUser);
            if (updated) {
                System.out.println("Profile updated successfully!");
            } else {
                System.out.println("Failed to update profile!");
            }
        } catch (SQLException e) {
            System.err.println("Error updating profile: " + e.getMessage());
        }
    }
    
    private static void changePassword() {
        System.out.println("\n=== Change Password ===");
        
        System.out.print("Current Password: ");
        String currentPassword = scanner.nextLine();
        
        try {
            User user = userService.authenticateUser(currentUser.getUsername(), currentPassword);
            if (user == null) {
                System.out.println("Current password is incorrect!");
                return;
            }
            
            System.out.print("New Password: ");
            String newPassword = scanner.nextLine();
            System.out.print("Confirm New Password: ");
            String confirmPassword = scanner.nextLine();
            
            if (!newPassword.equals(confirmPassword)) {
                System.out.println("Passwords do not match!");
                return;
            }
            
            boolean updated = userService.changePassword(currentUser.getId(), newPassword);
            if (updated) {
                System.out.println("Password changed successfully!");
            } else {
                System.out.println("Failed to change password!");
            }
        } catch (SQLException e) {
            System.err.println("Error changing password: " + e.getMessage());
        }
    }
    
    private static int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }
    
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}