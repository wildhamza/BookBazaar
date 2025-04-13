package com.bookshop.controllers;

import com.bookshop.models.Book;
import com.bookshop.models.User;
import com.bookshop.services.BookService;
import com.bookshop.utils.SessionManager;
import com.bookshop.utils.ViewNavigator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class EditBookController implements Initializable {
    
    @FXML private Label titleLabel;
    @FXML private TextField bookTitleField;
    @FXML private TextField authorField;
    @FXML private TextField publisherField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField isbnField;
    @FXML private TextField imageUrlField;
    @FXML private TextArea descriptionArea;
    @FXML private Spinner<Integer> stockSpinner;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label errorMessageLabel;
    
    private BookService bookService;
    private Book currentBook;
    private boolean isNewBook;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            ViewNavigator.getInstance().navigateTo("login.fxml");
            return;
        }
        
        bookService = new BookService();
        
        initializeCategoryComboBox();
        
        initializeStockSpinner();
        
        currentBook = SessionManager.getInstance().getSelectedBook();
        isNewBook = (currentBook == null);
        
        if (isNewBook) {
            titleLabel.setText("Add New Book");
            currentBook = new Book();
        } else {
            titleLabel.setText("Edit Book");
            populateForm();
        }
        
        errorMessageLabel.setVisible(false);
    }
    
    private void initializeCategoryComboBox() {
        categoryComboBox.setItems(FXCollections.observableArrayList(
            "Fiction", "Non-Fiction", "Science", "Technology", 
            "History", "Biography", "Fantasy", "Mystery", 
            "Thriller", "Romance", "Science Fiction", "Horror",
            "Self-Help", "Business", "Computer Science", "Reference"
        ));
    }
    
    private void initializeStockSpinner() {
        SpinnerValueFactory<Integer> valueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 0);
        stockSpinner.setValueFactory(valueFactory);
        
        stockSpinner.setEditable(true);
        
        TextFormatter<Integer> formatter = new TextFormatter<>(
            new StringConverter<Integer>() {
                @Override
                public String toString(Integer value) {
                    return value == null ? "0" : value.toString();
                }
                
                @Override
                public Integer fromString(String string) {
                    try {
                        return Integer.parseInt(string);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
            }
        );
        
        stockSpinner.getEditor().setTextFormatter(formatter);
    }
    
    private void populateForm() {
        bookTitleField.setText(currentBook.getTitle());
        authorField.setText(currentBook.getAuthor());
        publisherField.setText(currentBook.getPublisher());
        priceField.setText(currentBook.getPrice().toString());
        categoryComboBox.setValue(currentBook.getCategory());
        isbnField.setText(currentBook.getIsbn());
        imageUrlField.setText(currentBook.getImageUrl());
        descriptionArea.setText(currentBook.getDescription());
        stockSpinner.getValueFactory().setValue(currentBook.getStockQuantity());
    }
    
    @FXML
    private void handleSaveButton(ActionEvent event) {
        if (!validateForm()) {
            return;
        }
        
        try {
            currentBook.setTitle(bookTitleField.getText().trim());
            currentBook.setAuthor(authorField.getText().trim());
            currentBook.setPublisher(publisherField.getText().trim());
            currentBook.setPrice(new BigDecimal(priceField.getText().trim()));
            currentBook.setCategory(categoryComboBox.getValue());
            currentBook.setIsbn(isbnField.getText().trim());
            currentBook.setImageUrl(imageUrlField.getText().trim());
            currentBook.setDescription(descriptionArea.getText().trim());
            currentBook.setStockQuantity(stockSpinner.getValue());
            
            if (isNewBook) {
                bookService.addBook(currentBook);
                showAlert("Book added successfully");
            } else {
                bookService.updateBook(currentBook);
                showAlert("Book updated successfully");
            }
            
            ViewNavigator.getInstance().navigateTo("admin_dashboard.fxml");
            
        } catch (Exception e) {
            showError("Error saving book: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCancelButton(ActionEvent event) {
        ViewNavigator.getInstance().navigateTo("admin_dashboard.fxml");
    }
    
    private boolean validateForm() {
        errorMessageLabel.setVisible(false);
        
        if (bookTitleField.getText().trim().isEmpty()) {
            showError("Title is required");
            return false;
        }
        
        if (authorField.getText().trim().isEmpty()) {
            showError("Author is required");
            return false;
        }
        
        if (publisherField.getText().trim().isEmpty()) {
            showError("Publisher is required");
            return false;
        }
        
        try {
            BigDecimal price = new BigDecimal(priceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Price must be greater than zero");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Price must be a valid number");
            return false;
        }
        
        if (categoryComboBox.getValue() == null) {
            showError("Category is required");
            return false;
        }
        
        String isbn = isbnField.getText().trim();
        if (isbn.isEmpty()) {
            showError("ISBN is required");
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
