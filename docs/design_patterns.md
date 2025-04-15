# Design Patterns in Online Book Shop Application

## 1. Model-View-Controller (MVC)
**Purpose**: Separates the application into three interconnected components to manage the user interface, business logic, and data.

**Why Used**: 
- Provides clear separation of concerns
- Makes the code more maintainable and testable
- Allows for independent development of UI and business logic

**Relevant Code**:
- Views: FXML files in `src/main/resources/fxml/`
- Controllers: Java classes in `src/main/java/com/bookshop/controllers/`
- Models: Java classes in `src/main/java/com/bookshop/models/`

**Roles**:
- Model: `Book`, `User`, `Order` classes
- View: FXML files defining the UI
- Controller: Classes like `BookController`, `UserController`

## 2. Factory Pattern
**Purpose**: Creates objects without specifying the exact class of object that will be created.

**Why Used**:
- Centralizes object creation logic
- Makes the code more flexible and maintainable
- Simplifies adding new types of objects

**Relevant Code**:
```java
public class BookFactory {
    public static Book createBook(String title, String author, double price) {
        return new Book(title, author, price);
    }
}
```

**Roles**:
- Factory: `BookFactory` class
- Product: `Book` class
- Client: Classes that need to create `Book` objects

## 3. Singleton Pattern
**Purpose**: Ensures a class has only one instance and provides a global point of access to it.

**Why Used**:
- Manages shared resources like database connections
- Provides a single point of control
- Reduces memory usage by reusing instances

**Relevant Code**:
```java
public class DatabaseConnection {
    private static DatabaseConnection instance;
    
    private DatabaseConnection() {}
    
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
}
```

**Roles**:
- Singleton: `DatabaseConnection` class
- Client: Classes that need database access

## 4. Observer Pattern
**Purpose**: Defines a one-to-many dependency between objects so that when one object changes state, all its dependents are notified.

**Why Used**:
- Maintains consistency between related objects
- Supports loose coupling between objects
- Enables event handling in the UI

**Relevant Code**:
```java
public class ShoppingCart implements Observable {
    private List<Observer> observers = new ArrayList<>();
    
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(this);
        }
    }
}
```

**Roles**:
- Subject: `ShoppingCart` class
- Observer: UI components that need to update when cart changes
- Concrete Observer: `CartView` class

## 5. Strategy Pattern
**Purpose**: Defines a family of algorithms, encapsulates each one, and makes them interchangeable.

**Why Used**:
- Provides flexibility in sorting and filtering books
- Makes it easy to add new sorting/filtering strategies
- Separates the algorithm from the context that uses it

**Relevant Code**:
```java
public interface BookSortingStrategy {
    List<Book> sort(List<Book> books);
}

public class PriceSortingStrategy implements BookSortingStrategy {
    @Override
    public List<Book> sort(List<Book> books) {
        return books.stream()
            .sorted(Comparator.comparing(Book::getPrice))
            .collect(Collectors.toList());
    }
}
```

**Roles**:
- Strategy: `BookSortingStrategy` interface
- Concrete Strategy: `PriceSortingStrategy`, `TitleSortingStrategy` classes
- Context: `BookManager` class

## 6. Repository Pattern
**Purpose**: Mediates between the domain and data mapping layers, acting like an in-memory collection of domain objects.

**Why Used**:
- Abstracts data access logic
- Provides a clean API for data operations
- Makes testing easier by allowing mock repositories

**Relevant Code**:
```java
public interface BookRepository {
    List<Book> findAll();
    Book findById(int id);
    void save(Book book);
    void delete(Book book);
}

public class BookRepositoryImpl implements BookRepository {
    // Implementation details
}
```

**Roles**:
- Repository: `BookRepository` interface
- Concrete Repository: `BookRepositoryImpl` class
- Client: Service layer classes

## 7. Facade Pattern
**Purpose**: Provides a simplified interface to a complex subsystem.

**Why Used**:
- Simplifies complex operations like checkout
- Hides implementation details
- Provides a clean API for complex operations

**Relevant Code**:
```java
public class CheckoutFacade {
    private PaymentService paymentService;
    private OrderService orderService;
    private InventoryService inventoryService;
    
    public void processCheckout(ShoppingCart cart, PaymentInfo paymentInfo) {
        // Complex checkout logic
    }
}
```

**Roles**:
- Facade: `CheckoutFacade` class
- Subsystem Classes: `PaymentService`, `OrderService`, `InventoryService`
- Client: UI controllers 