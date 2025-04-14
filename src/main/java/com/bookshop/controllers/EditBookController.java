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
            "Fiction", "Self-help", "Reference", "Biography", "Education"
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
        if (currentBook != null) {
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
    }
    
    @FXML
    private void handleSaveButton(ActionEvent event) {
        if (!validateForm()) {
            return;
        }
        
        try {
            String title = bookTitleField.getText() != null ? bookTitleField.getText().trim() : "";
            String author = authorField.getText() != null ? authorField.getText().trim() : "";
            String publisher = publisherField.getText() != null ? publisherField.getText().trim() : "";
            String priceText = priceField.getText() != null ? priceField.getText().trim() : "0";
            String category = categoryComboBox.getValue() != null ? categoryComboBox.getValue() : "";
            String isbn = isbnField.getText() != null ? isbnField.getText().trim() : "";
            String imageUrl = imageUrlField.getText() != null ? imageUrlField.getText().trim() : "";
            String description = descriptionArea.getText() != null ? descriptionArea.getText().trim() : "";
            
            currentBook.setTitle(title);
            currentBook.setAuthor(author);
            currentBook.setPublisher(publisher);
            currentBook.setPrice(new BigDecimal(priceText));
            currentBook.setCategory(category);
            currentBook.setIsbn(isbn);
            currentBook.setImageUrl(imageUrl);
            currentBook.setDescription(description);
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
        
        if (bookTitleField.getText() == null || bookTitleField.getText().trim().isEmpty()) {
            showError("Title is required");
            return false;
        }
        
        if (authorField.getText() == null || authorField.getText().trim().isEmpty()) {
            showError("Author is required");
            return false;
        }
        
        if (publisherField.getText() == null || publisherField.getText().trim().isEmpty()) {
            showError("Publisher is required");
            return false;
        }
        
        try {
            String priceText = priceField.getText() != null ? priceField.getText().trim() : "0";
            BigDecimal price = new BigDecimal(priceText);
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
        
        String isbn = isbnField.getText() != null ? isbnField.getText().trim() : "";
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
